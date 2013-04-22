package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dList extends ArrayList<String> implements dScriptArgument {

    @ObjectFetcher("f")
    public static dList valueOf(String string) {
        if (string == null) return null;

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.
        final Pattern flag_by_id =
                Pattern.compile("(f\\[((?:p@|n@)(.+?))\\]@|f@)(.+)",
                        Pattern.CASE_INSENSITIVE);

        Matcher m;
        m = flag_by_id.matcher(string);

        if (m.matches()) {
            FlagManager flag_manager = DenizenAPI.getCurrentInstance().flagManager();

            try {
                // Global
                if (m.group(1).equalsIgnoreCase("f@")) {
                    if (FlagManager.serverHasFlag(m.group(4)))
                        return new dList(flag_manager.getGlobalFlag(m.group(4)));

                } if (m.group(2).toLowerCase().startsWith("p@")) {
                    if (FlagManager.playerHasFlag(aH.getPlayerFrom(m.group(3)), m.group(4)))
                        return new dList(flag_manager.getPlayerFlag(m.group(3), m.group(4)));

                } if (m.group(2).toLowerCase().startsWith("n@")) {
                    if (FlagManager.npcHasFlag(aH.getdNPCFrom(m.group(3)), m.group(4)))
                        return new dList(flag_manager.getNPCFlag(Integer.valueOf(m.group(3)), m.group(4)));
                }

            } catch (Exception e) {
                dB.echoDebug("Flag '" + m.group() + "' could not be found!");
                return null;
            }
        }

        // Use value of string, which will seperate values by the use of a pipe (|)
        return new dList(string.replaceFirst("a@", ""));
    }


    /////////////
    // Instance Methods
    //////////


    private FlagManager.Flag flag = null;

    public dList(String items) {
        addAll(Arrays.asList(items.split("\\|")));
    }

    public dList(List<String> items) {
        addAll(items);
    }

    public dList(FlagManager.Flag flag) {
        this.flag = flag;
        addAll(flag.values());
    }

    private String getId() {
        if (flag != null)
            return flag.toString();

        if (isEmpty()) return "li@";
        StringBuilder dScriptArg = new StringBuilder();
        dScriptArg.append("li@");
        for (String item : this)
            dScriptArg.append(item + "|");
        return dScriptArg.toString().substring(0, dScriptArg.length() - 1);

    }


    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////


    private String prefix = "List";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dList setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + getId() + "<G>'  ";
    }

    @Override
    public String as_dScriptArgValue() {
        return getId();
    }

    @Override
    public String toString() {
        return "l@" + getId();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (attribute.startsWith("ascslist")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this)
                dScriptArg.append(item + ", ");
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("get")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            int index = attribute.getIntContext(1);
            if (index > size()) return null;
            String item;
            if (index > 0) item = get(index - 1);
            else item = get(0);
            return new Element(item).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(toString()).getAttribute(attribute);
    }

}

package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEllipsoid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.HashMap;
import java.util.List;

public class BlockPhysicsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block physics (in <notable cuboid>)
    // <material> physics (in <notable cuboid>)
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a block's physics update.
    //
    // @Context
    // <context.location> returns a dLocation of the block the physics is affecting.
    // <context.new_material> returns a dMaterial of what the block is becoming.
    //
    // -->

    public BlockPhysicsScriptEvent() {
        instance = this;
    }

    public static BlockPhysicsScriptEvent instance;
    public dLocation location;
    public dMaterial new_material;
    public dMaterial old_material;
    public BlockPhysicsEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains("physics");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (lower.equals("block physics")) {
            return true;
        }

        if (!lower.startsWith("block")) {
            dMaterial mat = dMaterial.valueOf(CoreUtilities.getXthArg(0, lower));
            if (mat == null) {
                dB.echoError("Invalid event material [BlockPhysics]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
            if (!old_material.matchesMaterialData(mat.getMaterialData())) {
                return false;
            }
        }

        if (CoreUtilities.xthArgEquals(2, lower, "in")) {
            String it = CoreUtilities.getXthArg(3, lower);
            if (dCuboid.matches(it)) {
                dCuboid cuboid = dCuboid.valueOf(it);
                if (!cuboid.isInsideCuboid(location)) {
                    dB.log(location + " not in " + cuboid);
                    return false;
                }
            }
            else if (dEllipsoid.matches(it)) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(it);
                if (!ellipsoid.contains(location)) {
                    return false;
                }
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [BlockPhysics]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "BlockPhysics";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockPhysicsEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("new_material", new_material);
        return context;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        new_material =  dMaterial.getMaterialFrom(event.getChangedType());
        old_material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

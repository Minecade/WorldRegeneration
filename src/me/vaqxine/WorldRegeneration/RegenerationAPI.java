package me.vaqxine.WorldRegeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

public class RegenerationAPI {
    /**
     * 
     * @param b Block object BEFORE it is destroyed / set to air.
     * @param seconds Delay in seconds until regeneration occurs.
     */
    public static void queueBlockForRegeneration(Block b, int seconds){
        Location l = b.getLocation();
        Material m = b.getType();
        short metadata = b.getData();
        BlockState bs = b.getState();
        WorldRegeneration.block_respawn_data.put(l, constructObjectArray(m, metadata, bs));
        WorldRegeneration.block_respawn_time.put(l, System.currentTimeMillis() + (seconds * 1000));
    }
    
    private static Object[] constructObjectArray(Material m, short metadata, BlockState bs){
        List<Object> o = new ArrayList<Object>(Arrays.asList(m,metadata,bs));
        return o.toArray(new Object[o.size()]);
    }
}

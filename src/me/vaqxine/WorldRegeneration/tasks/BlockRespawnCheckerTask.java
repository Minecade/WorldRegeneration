package me.vaqxine.WorldRegeneration.tasks;

import java.util.Map.Entry;

import me.vaqxine.WorldRegeneration.WorldRegeneration;

import org.bukkit.Location;

public class BlockRespawnCheckerTask implements Runnable {

    @Override
    public void run() {
        for(Entry<Location, Long> data : WorldRegeneration.block_respawn_time.entrySet()){
            long expiration = data.getValue();
            if(System.currentTimeMillis() >= expiration){
                // Time to mark for regeneration.
                Location l = data.getKey();
                if(WorldRegeneration.block_respawn_data.containsKey(l)){
                    // Queue for main thread setType()
                    WorldRegeneration.blocks_to_respawn.add(l);
                    // WorldRegeneration.log.debug("Queued location for respawn ; " + l.toString(), this.getClass());
                } else {
                    WorldRegeneration.log.error("No block_respawn_data found for location object " + l.toString(), this.getClass());
                    WorldRegeneration.block_respawn_time.remove(l);
                    continue;
                }
            } else {
                // Not time for regeneration, do nothing I guess.
            }
        }
    }

}

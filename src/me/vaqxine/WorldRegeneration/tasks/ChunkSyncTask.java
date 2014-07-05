package me.vaqxine.WorldRegeneration.tasks;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.vaqxine.WorldRegeneration.WorldRegeneration;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkSyncTask implements Runnable {
    
    protected static ConcurrentHashMap<World, Chunk[]> async_chunks = new ConcurrentHashMap<World, Chunk[]>();
    // A thread safe map of chunks that are loaded.
    
    public static HashSet<String> loaded_chunks = new HashSet<String>();
    // All loaded chunks across all worlds with format world_name,cx,cz
    
    @Override
    public void run() {
        for(World w : WorldRegeneration.getPlugin().getServer().getWorlds()){
            Chunk[] chunks = WorldRegeneration.getPlugin().getServer().getWorlds().get(0).getLoadedChunks().clone();
            async_chunks.put(w, chunks);
        }
        
        // This will iterate through every chunk in chunks[] for each world and populate a global Hashset full of serialized strings so we can easily tell if a chunk is loaded or not.
        Bukkit.getServer().getScheduler().runTaskAsynchronously(WorldRegeneration.getPlugin(), new LoadedChunkGetterTask());
    }
}

class LoadedChunkGetterTask implements Runnable {
    
    @Override
    public void run() {
        for(Entry<World, Chunk[]> data : ChunkSyncTask.async_chunks.entrySet()){
            String wn = data.getKey().getName();
            for(Chunk c : data.getValue()){
                ChunkSyncTask.loaded_chunks.add(wn + "," + c.getX() + "," + c.getZ());
            }
        }
    }

}

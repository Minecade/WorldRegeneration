package me.vaqxine.WorldRegeneration.utils;

import me.vaqxine.WorldRegeneration.tasks.ChunkSyncTask;

import org.bukkit.World;

public class ChunkUtils {
    public static boolean isAsyncChunkLoaded(World w, int cx, int cz){
        return ChunkSyncTask.loaded_chunks.contains(w.getName() + "," + cx + "," + cz);
    }
}

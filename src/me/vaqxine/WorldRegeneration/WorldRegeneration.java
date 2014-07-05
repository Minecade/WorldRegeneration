package me.vaqxine.WorldRegeneration;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import me.vaqxine.WorldRegeneration.tasks.BlockRespawnCheckerTask;
import me.vaqxine.WorldRegeneration.tasks.BlockRespawnTask;
import me.vaqxine.WorldRegeneration.tasks.ChunkSyncTask;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldRegeneration extends JavaPlugin {
    
    public static volatile ConcurrentHashMap<Location, Object[]> block_respawn_data = new ConcurrentHashMap<Location, Object[]>();
    // Location, Object[Material, Metadata, Materialdata]
    
    public static volatile ConcurrentHashMap<Location, Long> block_respawn_time = new ConcurrentHashMap<Location, Long>();
    // Epoch time that must pass before block may regen.
    
    public static volatile HashSet<Location> blocks_to_respawn = new HashSet<Location>();
    // Locations that are ready to be processed on main thread for .setType();
    
    public static Logger log = new Logger();
    private static WorldRegeneration plugin;
    
    public void onEnable(){
        plugin = this;
        
        // This is an example usage listener. Uncomment if you want every block broken to be tracked. This plugin is an API at heart, use RegenerationAPI to decide when to regen stuff.
        //this.getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
        
        // Update blocks ready for setType() on main thread every 10 seconds. (fat iteration)
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new BlockRespawnCheckerTask(), 20L, 10 * 20L);
        
        // Run a setType() method every second, we will limit how many blocks can be set / tick.
        this.getServer().getScheduler().runTaskTimer(this, new BlockRespawnTask(this), 20L, 20L).getTaskId();
        
        // Populates threadsafe list of all loaded chunks.
        this.getServer().getScheduler().runTaskTimer(this, new ChunkSyncTask(), 20L, 5 * 20L);
    }
    
    public void onDisable(){
        // Kill all current tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        for(Location l : block_respawn_time.keySet()){
            // Whitelist all blocks for regeneration.
            blocks_to_respawn.add(l);
        }
        
        // Get rid of throttle, process everything.
        BlockRespawnTask.max_blocks_per_iteration = Integer.MAX_VALUE;
        BlockRespawnTask.process_loaded_chunks_only = false;
        
        // Now run on main thread.
        log.debug("Processing " + blocks_to_respawn.size() + " pending blocks to respawn!", this.getClass());
        BlockRespawnTask brt = new BlockRespawnTask(this);
        brt.run();
    }
    
    public static WorldRegeneration getPlugin(){
        return plugin;
    }
}

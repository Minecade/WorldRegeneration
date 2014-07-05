package me.vaqxine.WorldRegeneration.listeners;

import me.vaqxine.WorldRegeneration.RegenerationAPI;
import me.vaqxine.WorldRegeneration.WorldRegeneration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldChangeListener implements Listener {
    WorldRegeneration plugin;
    public WorldChangeListener(WorldRegeneration wr){
        this.plugin = wr;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        RegenerationAPI.queueBlockForRegeneration(e.getBlock(), 10);
    }
    
}

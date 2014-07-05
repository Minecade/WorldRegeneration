package me.vaqxine.WorldRegeneration.tasks;

import java.util.ArrayList;

import me.vaqxine.WorldRegeneration.WorldRegeneration;
import me.vaqxine.WorldRegeneration.utils.ChunkUtils;
import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketPlayOutWorldEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

public class BlockRespawnTask implements Runnable {

    public static int max_blocks_per_iteration = 10;
    public static boolean process_loaded_chunks_only = true;

    WorldRegeneration plugin;
    public BlockRespawnTask(WorldRegeneration wr){
        this.plugin = wr;
    }
    
    @Override
    public void run() {
        int x = 0;

        for(final Location l : new ArrayList<Location>(WorldRegeneration.blocks_to_respawn)){
            if(x >= max_blocks_per_iteration){
                WorldRegeneration.log.debug("Regenerated " + x + " block(s), " + WorldRegeneration.blocks_to_respawn.size() + " left.", this.getClass());
                return;
            }

            if(!(WorldRegeneration.block_respawn_data.containsKey(l))){
                WorldRegeneration.log.error("No block_respawn_data found for location object " + l.toString(), this.getClass());
                continue;
            }

            if(process_loaded_chunks_only && !(ChunkUtils.isAsyncChunkLoaded(l.getWorld(), (int) (l.getX() / 16.0D), (int) (l.getZ() / 16.0D)))){
                continue;
            }

            x++;
            Object[] o = WorldRegeneration.block_respawn_data.get(l);
            // Material, Metadata, BlockState

            final Material m = (Material)o[0];
            final short metadata = (short)o[1];
            BlockState bs = (BlockState)o[2];

            Block b = l.getBlock();
            b.setTypeIdAndData(m.getId(), (byte)metadata, false);
            
            if(b.getType() == Material.MOB_SPAWNER){
                // Set mob type spawning.
                CreatureSpawner cs = ((CreatureSpawner)b.getState());
                cs.setSpawnedType(((CreatureSpawner)bs).getSpawnedType());
                cs.update(false);
            } else if(b.getType() == Material.BEACON){
                // Set buff types, "inventory"
                Beacon beacon = ((Beacon)b.getState());
                beacon.setData(bs.getData());
                beacon.update(false);
            } else if(b.getType() == Material.CHEST){
                // TODO: Should we regenerate inventory?
                Chest c = ((Chest)b.getState());
                c.setData(bs.getData());
                c.update(false);
            } else if(b.getType() == Material.COMMAND){
                // Set the command.
                CommandBlock cmd_block = ((CommandBlock)b.getState());
                CommandBlock bs_cmd_block = ((CommandBlock)bs);
                cmd_block.setData(bs_cmd_block.getData());
                cmd_block.setCommand(bs_cmd_block.getCommand());
                cmd_block.setName(bs_cmd_block.getName());
            } else if (b.getType() == Material.DISPENSER){ 
                // TODO: Should we regenerate inventory?
                Dispenser dp = ((Dispenser)b.getState());
                dp.setData(bs.getData());
                dp.update(false);
            } else if(b.getType() == Material.DROPPER){
                // TODO: Should we regenerate inventory?
                Dropper dp = ((Dropper)b.getState());
                dp.setData(bs.getData());
                dp.update(false);
            } else if(b.getType() == Material.FURNACE){
                // TODO: Should we regenerate inventory?
                Furnace fc = ((Furnace)b.getState());
                fc.setBurnTime(((Furnace)bs).getBurnTime());
                fc.setCookTime(((Furnace)bs).getCookTime());
                fc.setData(bs.getData());
                fc.update(false);
            } else if(b.getType() == Material.HOPPER){
                // TODO: Should we regenerate inventory?
                Hopper hp = ((Hopper)b.getState());
                hp.setData(bs.getData());
                hp.update(false);
            } else if(b.getType() == Material.JUKEBOX){
                // TODO: Should we regenerate inventory?
                Jukebox jb = ((Jukebox)b.getState());
                jb.setPlaying((((Jukebox)bs).getPlaying()));
                jb.setData(bs.getData());
                jb.update(false);
            } else if(b.getType() == Material.NOTE_BLOCK){
                NoteBlock nb = ((NoteBlock)b.getState());
                nb.setNote(((NoteBlock)bs).getNote());
                nb.setData(bs.getData());
                nb.update(false);
            } else if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.SIGN){
                // TODO: Even with (false), do we need to make sure these don't regen before their surface? Prevent block physics?
                Sign s = ((Sign)b.getState());
                Sign bs_s = (Sign)bs;
                s.setLine(0, bs_s.getLine(0));
                s.setLine(1, bs_s.getLine(1));
                s.setLine(2, bs_s.getLine(2));
                s.setLine(3, bs_s.getLine(3));
                s.setData(bs_s.getData());
                s.update(false);
            } else if(b.getType() == Material.SKULL){
                Skull sk = ((Skull)b.getState());
                Skull bs_sk = ((Skull)bs);
                sk.setSkullType(bs_sk.getSkullType());
                sk.setType(bs_sk.getType());
                sk.setRotation(bs_sk.getRotation());
                sk.setOwner(bs_sk.getOwner());
                sk.setData(bs_sk.getData());
                sk.update(false);
            } else {
                // Set MaterialData value for directional stuff.
                BlockState b_bs = b.getState();
                b_bs.setData(bs.getData());
                b_bs.update(false);
            }
            
            if(process_loaded_chunks_only){
                // Don't need to send these out of it's possibly unloaded.
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
                    @Override
                    public void run(){
                        int particle_id = m.getId();
                        if(particle_id == 31 && metadata > 0){
                            particle_id = 18; // Leaves for tall grass / tall fern.
                        }

                        Packet particles = new PacketPlayOutWorldEvent(2001, Math.round(l.getBlockX()), Math.round(l.getBlockY()), Math.round(l.getBlockZ()), particle_id, false);
                        ((CraftServer) plugin.getServer()).getServer().getPlayerList().sendPacketNearby(l.getBlockX(), l.getBlockY(), l.getBlockZ(), 16, ((CraftWorld) l.getWorld()).getHandle().dimension, particles);
                        
                    }
                });
            }

            WorldRegeneration.blocks_to_respawn.remove(l);
            WorldRegeneration.block_respawn_data.remove(l);
            WorldRegeneration.block_respawn_time.remove(l);
        }
    }

}

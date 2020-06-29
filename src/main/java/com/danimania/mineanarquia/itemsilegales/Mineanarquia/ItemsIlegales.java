package com.danimania.mineanarquia.itemsilegales.Mineanarquia;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;


import java.util.Random;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public final class ItemsIlegales extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;



    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();

    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run() {
                //Every tick the code put here will perform

                for (Player p: Bukkit.getServer().getOnlinePlayers()) {
                    checkPlayerSpeed(p);
                }
            }

        }, 0L, 1L);

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        // antithunder
        this.protocolManager.addPacketListener((PacketListener)new PacketAdapter((Plugin)this, new PacketType[] { PacketType.Play.Server.NAMED_SOUND_EFFECT }) {
            public void onPacketSending(PacketEvent event) {

                // Sonidos que esten a mas de 250 del jugador seran cancelados? quien sabe
                try {
                    final Player player = event.getPlayer();
                    PacketContainer packet = event.getPacket();
                    StructureModifier<Integer> ints = packet.getIntegers();
                    int xPacket =  ints.read(0) / 8;
                    int zPacket = ints.read(2) / 8;
                    double xPlayer = player.getLocation().getBlockX();
                    double zPlayer = player.getLocation().getBlockZ();

                    double xDif = Math.abs(xPlayer - xPacket);
                    double yDif = Math.abs(zPlayer - zPacket);
                    if(xDif > 250 | yDif > 250) {
                        event.setCancelled(true);
                    }
                }
                catch(Exception e) {}


            }
        });


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // cuando se suelta un objeto (romper cofre etc)
    @EventHandler
    public void dropear(BlockDropItemEvent e){
        for(Item i : e.getItems()){
            if(verificarIlegal(i.getItemStack())){
                i.getItemStack().setAmount(0);
            }
        }
    }
    @EventHandler
    public void alEntrar(PlayerJoinEvent e){
        for(ItemStack i : e.getPlayer().getInventory().getContents()){
            if(verificarIlegal(i)){
                i.setAmount(0);
            }
        }
    }

    @EventHandler
    public void alPortalUsar(PlayerPortalEvent e) {
        if(e.getTo().getWorld().getEnvironment().equals(World.Environment.THE_END)){
            if(e.getFrom().getBlockX() > 25000.0 || e.getFrom().getBlockZ() > 25000.0){
                e.setCancelled(true);
            } else {

                Location l = e.getTo().getWorld().getSpawnLocation();
                l.setX(100.0);
                l.setY(51.0);
                l.setZ(0.0);
                e.getPlayer().teleport(e.getTo().getWorld().getSpawnLocation());
            }
        }
    }


    @EventHandler
    public void romperEndPortal(BlockBreakEvent e){
        if(e.getBlock().getType() == Material.END_PORTAL){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void alAbrir(InventoryOpenEvent e){
        boolean ilegales = false;
        for(ItemStack i : e.getInventory().getContents()){
            if(verificarIlegal(i)){
                ilegales = true;
                i.setAmount(0);
            }
        }
        if(ilegales){
            Bukkit.getServer().getLogger().info("Items ilegales. Jugador: "+e.getPlayer().getName()+". X"+e.getPlayer().getLocation().getX()+", Y"+e.getPlayer().getLocation().getY()+", Z"+e.getPlayer().getLocation().getZ());
        }
    }

    @EventHandler
    public void hopperThing( InventoryMoveItemEvent e ) {
        if (verificarIlegal(e.getItem())) {
            e.getItem().setAmount(0);
        }
    }

    public boolean verificarIlegal(ItemStack item){
        if(item != null){
            if(item.getType() == Material.FIREWORK_ROCKET){
                FireworkMeta fwm = (FireworkMeta) item.getItemMeta();
                if(fwm.getPower()>3){
                    return true;
                }
            }else if(item.getType() == Material.END_PORTAL_FRAME || item.getType() == Material.BEDROCK){
                return true;
            }else if(item.getType() == Material.SPLASH_POTION){
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                for(PotionEffect pe : meta.getCustomEffects()){
                    if(pe.getAmplifier()>2){
                        return true;
                    }
                }
            }
        }
        return false;
    }



    public void checkPlayerSpeed( Player p) {
        Vector vectorVelocity = p.getVelocity();
        if(vectorVelocity.length() > 33.0) {
            p.kickPlayer("GABO SPEEDHACK SRHECK: YOUR SPEED IS MORE THAN 33");
        }
        Entity mount = p.getVehicle();


        if( mount != null ) {
            Vector mountSpeed = mount.getVelocity();
            if(mountSpeed.length() > 33.0) {

                p.kickPlayer("GABO SPEEDHACK SRHECK: YOUR SPEED IS MORE THAN 33");

            }
        }
    }



}

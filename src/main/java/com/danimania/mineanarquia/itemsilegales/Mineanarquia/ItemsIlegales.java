package com.danimania.mineanarquia.itemsilegales.Mineanarquia;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
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


import java.util.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class ItemsIlegales extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private Set<Material> materialesIlegales;
    private Set<Material> stacksIlegales;
    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();

    }

    @Override
    public void onEnable() {
        materialesIlegales = new HashSet<Material>();
        stacksIlegales = new HashSet<Material>();
        // creamos los materiales ilegales
        materialesIlegales.add(Material.BEDROCK);
        materialesIlegales.add(Material.BARRIER);
        materialesIlegales.add(Material.STRUCTURE_VOID);
        materialesIlegales.add(Material.SPAWNER);

        materialesIlegales.add(Material.END_PORTAL_FRAME);
        materialesIlegales.add(Material.END_PORTAL);

        // vamos a quitar los spawner egg no?

        List<Material> materials = Arrays.asList(Material.values());
        for (Material m: materials) {
            // bloques ilegales. su existencia debe ser destruida
            if (m.name().contains("SPAWN_EGG") || m.name().contains("COMMAND")
                || m.name().contains("STRUCTURE")) {
                materialesIlegales.add(m);
            }

            if (m.name().contains("SWORD") || m.name().contains("BOOTS") ||
                    m.name().contains("LEGGIN") || m.name().contains("CHESTPLATE") ||
                    m.name().contains("HELMET") || m.name().contains("AXE") ||
                    m.name().contains("HOE") || m.name().contains("SHIELD") ||
                    m.name().contains("TOTEM") ) {
                stacksIlegales.add(m);
            }

            // stacks ilegales. si hay mas de uno deben ser destruidos

        }

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
    public void moveObjectFromInventory (InventoryMoveItemEvent e) {
        if(verificarIlegal(e.getItem())) {
            e.getItem().setAmount(0);
        } else {
            if (
                    e.getSource().getType() == InventoryType.SHULKER_BOX && e.getDestination().getType() == InventoryType.HOPPER
            ) {
              e.setCancelled(true);
            }
        }


    }


    @EventHandler
    public void alEntrar(PlayerJoinEvent e){
        // verificarPocionesIlegales(e.getPlayer());
        for(ItemStack i : e.getPlayer().getInventory().getContents()){
            if(verificarIlegal(i)){
                e.getPlayer().setHealth(1.0);
                kickPlayer(e.getPlayer());

            }
        }

        //verificar EQUIPO
        for(ItemStack i : e.getPlayer().getInventory().getArmorContents()){
            if(verificarIlegal(i)){
                e.getPlayer().setHealth(1.0);
                // i.setAmount(0);
                kickPlayer(e.getPlayer());
            }
        }

        if(verificarIlegal(e.getPlayer().getInventory().getItemInOffHand())){
            e.getPlayer().setHealth(1.0);
            // i.setAmount(0);
            kickPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void alPortalUsar(PlayerPortalEvent e) {
        if(e.getTo().getWorld().getEnvironment().equals(World.Environment.THE_END)){
            if(e.getFrom().getWorld().getEnvironment().equals(World.Environment.NETHER)){
                e.setCancelled(true);
                e.getPlayer().setHealth(0);
            }
            if(Math.abs(e.getFrom().getBlockX()) > 25000.0 || Math.abs(e.getFrom().getBlockZ()) > 25000.0){
                e.setCancelled(true);
                e.getPlayer().setHealth(1);
                // e.getPlayer().kickPlayer("tu portal no me gusta. En el futruo esto te matara");
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
    public void alColocarBloque(BlockPlaceEvent e) {
        ItemStack items = new ItemStack(e.getBlock().getType());
        if(verificarIlegal(items)) {
            e.setCancelled(true);
            e.getPlayer().setHealth(1);
        }
    }
    @EventHandler
    public void alAbrir(InventoryOpenEvent e){

        boolean ilegales = false;
        if(e.getInventory().getType() == InventoryType.HOPPER){
            for(ItemStack it : e.getPlayer().getInventory().getContents()){
                if(verificarIlegal(it)){
                    it.setAmount((0));
                    ilegales = true;
                }
            }
        }


        for(ItemStack i : e.getInventory().getContents()){
            if(verificarIlegal(i)){
                ilegales = true;
                i.setAmount(0);
            }


        }
        if(ilegales){
            Bukkit.getServer().getLogger().info("Items ilegales. Jugador: "+e.getPlayer().getName()+". X"+e.getPlayer().getLocation().getX()+", Y"+e.getPlayer().getLocation().getY()+", Z"+e.getPlayer().getLocation().getZ());
            e.getPlayer().setHealth(1);
        }

    }

    public boolean verificarIlegal(ItemStack item){
        if(item != null){
            if(item.getType() == Material.FIREWORK_ROCKET){
                FireworkMeta fwm = (FireworkMeta) item.getItemMeta();
                if(fwm.getPower()>3){
                    return true;
                }
            }else if(
                    materialesIlegales.contains(item.getType())
            ){
                return true;
            }else if(item.getType() == Material.SPLASH_POTION){
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                for(PotionEffect pe : meta.getCustomEffects()){
                    if(pe.getAmplifier()>4){
                        return true;
                    }
                }
            }

            if( stacksIlegales.contains(item.getType())){
                if(item.getAmount()>1){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean ilegalStack(ItemStack item){
        Boolean res = false;

        return res;
    }

    public void checkPlayerSpeed( Player p) {

    }

    public void kickPlayer( Player p) {
    p.getInventory().clear();
    }



}

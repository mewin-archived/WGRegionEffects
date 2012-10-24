/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEffects;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.ConcurrentModificationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author mewin
 */
public class WGRegionEffectsListener implements Listener {

    private WorldGuardPlugin wgPlugin;
    private WGRegionEffectsPlugin plugin;
    
    WGRegionEffectsListener(WorldGuardPlugin wgPlugin, WGRegionEffectsPlugin plugin)
    {
        this.wgPlugin = wgPlugin;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        updatePlayerEffects(e.getPlayer());
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
        updatePlayerEffects(e.getPlayer());
    }
    
    @EventHandler
    public synchronized void onPlayerDeath(PlayerDeathEvent e)
    {
        WGRegionEffectsPlugin.playerEffects.remove(e.getEntity());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        updatePlayerEffects(e.getPlayer());
    }
    
    
    private synchronized void updatePlayerEffects(Player p)
    {
        WGRegionEffectsPlugin.playerEffects.put(p, Util.getEffectsForLocation(wgPlugin, p.getLocation())); 
    }
}
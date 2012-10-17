/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEffects;

import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        WGRegionEffectsPlugin.playerEffects.remove(e.getEntity());
    }
    
    
    private void updatePlayerEffects(Player p)
    {
        RegionManager rm = wgPlugin.getRegionManager(p.getWorld());
        if (rm == null)
        {
            return ;
        }
        ApplicableRegionSet regions = rm.getApplicableRegions(p.getLocation());
        
        List<PotionEffectDesc> effects = new ArrayList<>();
        Iterator<ProtectedRegion> itr = regions.iterator();
        
        try
        {
            Field f = regions.getClass().getDeclaredField("globalRegion");
            f.setAccessible(true);
            ProtectedRegion region = (ProtectedRegion) f.get(regions);
            
            Set<PotionEffectDesc> flags = (Set<PotionEffectDesc>) region.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
            Iterator<PotionEffectDesc> itr2 = flags.iterator();
            
            while(itr2.hasNext())
            {
                PotionEffectDesc effect = itr2.next();
                
                effects.add(effect);
            }
        }
        catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NullPointerException ex)
        {
            
        }
        if (!p.isDead())
        {
            while(itr.hasNext())
            {
                ProtectedRegion region = itr.next();

                Set<PotionEffectDesc> flags = (Set<PotionEffectDesc>) region.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
                if (flags == null)
                {
                    continue;
                }
                Iterator<PotionEffectDesc> itr2 = flags.iterator();

                while(itr2.hasNext())
                {
                    PotionEffectDesc effect = itr2.next();

                    effects.add(effect);
                }
            }
        }
        WGRegionEffectsPlugin.playerEffects.put(p, effects);
    }
}

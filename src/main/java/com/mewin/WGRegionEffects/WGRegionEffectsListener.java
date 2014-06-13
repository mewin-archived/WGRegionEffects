/*
 * Copyright (C) 2012 mewin <mewin001@hotmail.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mewin.WGRegionEffects;

import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
import com.mewin.WGRegionEvents.MovementWay;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author mewin <mewin001@hotmail.de>
 */
public class WGRegionEffectsListener implements Listener {

    private WorldGuardPlugin wgPlugin;
    private WGRegionEffectsPlugin plugin;
    
    WGRegionEffectsListener(WorldGuardPlugin wgPlugin, WGRegionEffectsPlugin plugin)
    {
        this.wgPlugin = wgPlugin;
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEnterRegion(RegionEnterEvent e)
    {
        ProtectedRegion region = e.getRegion();
        Set<PotionEffectDesc> effects = (Set<PotionEffectDesc>) region.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
        if (effects != null)
        {
            synchronized (WGRegionEffectsPlugin.playerEffects)
            {
                for (PotionEffectDesc effect : effects)
                {
                    if (!WGRegionEffectsPlugin.playerEffects.containsKey(e.getPlayer().getName()))
                    {
                        WGRegionEffectsPlugin.playerEffects.put(e.getPlayer().getName(), new HashSet<PotionEffectDesc>());
                    }
                    WGRegionEffectsPlugin.playerEffects.get(e.getPlayer().getName()).add(effect);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeaveRegion(RegionLeaveEvent e)
    {
        if (e.getMovementWay() == MovementWay.DISCONNECT) // he didnt really leave the region, on login he will be back
        {
            return;
        }
        ProtectedRegion region = e.getRegion();
        Set<PotionEffectDesc> effects = (Set<PotionEffectDesc>) region.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
        if (effects != null)
        {
            synchronized (WGRegionEffectsPlugin.playerEffects)
            {
                for (PotionEffectDesc effect : effects)
                {
                    if (!WGRegionEffectsPlugin.playerEffects.containsKey(e.getPlayer().getName()))
                    {
                        return;
                    }
                    WGRegionEffectsPlugin.playerEffects.get(e.getPlayer().getName()).remove(effect);
                }
            }
        }
    }
}

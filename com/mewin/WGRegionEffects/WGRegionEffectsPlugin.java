/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEffects;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
import com.mewin.WGRegionEffects.flags.PotionEffectFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author mewin
 */



public class WGRegionEffectsPlugin extends JavaPlugin {
    public static final CustomSetFlag EFFECT_FLAG = new CustomSetFlag("effects", new PotionEffectFlag("effect", RegionGroup.ALL));
    
    private WGCustomFlagsPlugin custPlugin;
    private WorldGuardPlugin wgPlugin;
    private WGRegionEffectsListener listener;
    
    public static final Map<Player, List<PotionEffectDesc>> playerEffects = new HashMap<Player, List<PotionEffectDesc>>();
    public static List<Player> ignoredPlayers = new ArrayList<Player>();
    
    @Override
    public void onEnable()
    {
        Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
        
        if (plug == null || !(plug instanceof WGCustomFlagsPlugin) || !plug.isEnabled())
        {
            getLogger().warning("Could not load WorldGuard Custom Flags Plugin, disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        {
            custPlugin = (WGCustomFlagsPlugin) plug;
        }
        
        plug = getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (plug == null || !(plug instanceof WorldGuardPlugin) || !plug.isEnabled())
        {
            getLogger().warning("Could not load WorldGuard Plugin, disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        {
            wgPlugin = (WorldGuardPlugin) plug;
        }
        
        listener = new WGRegionEffectsListener(wgPlugin, this);
        
        getServer().getPluginManager().registerEvents(listener, plug);
        
        custPlugin.addCustomFlag(EFFECT_FLAG);
        
        scheduleTask();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("toggleeffects")
                || cmd.getName().equalsIgnoreCase("te")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!player.hasPermission("effects.toggle")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission for that.");
                }
                else if (WGRegionEffectsPlugin.ignoredPlayers.contains(player)) {
                    WGRegionEffectsPlugin.ignoredPlayers.remove(player);
                    player.sendMessage(ChatColor.GOLD + "Region effects toggled on.");
                } else {
                    WGRegionEffectsPlugin.ignoredPlayers.add(player);
                    player.sendMessage(ChatColor.GOLD + "Region effects toggled off.");
                }
            } else {
                sender.sendMessage("How could a console be affected by effects?");
            }
            return true;
        }
        return false;
    }

    private void scheduleTask()
    {
        getServer().getScheduler().scheduleSyncRepeatingTask(wgPlugin, new Runnable()
        {

            @Override
            public synchronized void run() {
                for(Player p : getServer().getOnlinePlayers())
                {
                    if (ignoredPlayers.contains(p))
                    {
                        continue;
                    }
                    List<PotionEffectDesc> effects = new ArrayList<PotionEffectDesc>(playerEffects.get(p));
                    if (effects == null) {
                        continue;
                    }
                    Iterator<PotionEffectDesc> itr = effects.iterator();
                    {
                        CUR_POTION:
                        while(itr.hasNext())
                        {
                            PotionEffect effect = itr.next().createEffect();
                            Iterator<PotionEffect> itr2 = p.getActivePotionEffects().iterator();

                            while(itr2.hasNext())
                            {
                                PotionEffect pe = itr2.next();

                                if (pe.getType() == effect.getType() && pe.getDuration() > effect.getDuration())
                                {
                                    continue CUR_POTION;
                                }
                            }
                            if (effect.getAmplifier() != -1)
                            {
                                p.addPotionEffect(effect, true);
                            }
                            else
                            {
                                p.removePotionEffect(effect.getType());
                            }
                        }
                    }
                }
            }
            
        }, 20L, 5L);
    }
}

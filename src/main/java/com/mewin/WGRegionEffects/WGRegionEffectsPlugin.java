/*
 * Copyright (C) 2014 mewin <mewin001@hotmail.de>
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

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
import com.mewin.WGRegionEffects.flags.PotionEffectFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author mewin <mewin001@hotmail.de>
 */
public class WGRegionEffectsPlugin extends JavaPlugin
{
    public static final CustomSetFlag EFFECT_FLAG = new CustomSetFlag("effects", new PotionEffectFlag("effect", RegionGroup.ALL));
    
    //private WGCustomFlagsPlugin custPlugin;
    private WorldGuardPlugin wgPlugin;
    private WGRegionEffectsListener listener;
    private File confFile, cacheFile;
    private int tickDelay = 20;
    private BukkitTask task;
    
    public static final Map<String, Set<PotionEffectDesc>> playerEffects = new HashMap<String, Set<PotionEffectDesc>>();
    public static final Map<String, Set<PotionEffectDesc>> activeEffects = new HashMap<String, Set<PotionEffectDesc>>();
    public static final Map<String, Map<PotionEffectDesc, Integer>> cachedEffects = new HashMap<String, Map<PotionEffectDesc, Integer>>();
    
    public static List<Player> ignoredPlayers = new ArrayList<Player>();
    
    @Override
    public void onEnable()
    {
        Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
     
        confFile = new File(this.getDataFolder(), "config.yml");
        cacheFile = new File(this.getDataFolder(), "cache.yml");
        
        if (plug == null || !(plug instanceof WGCustomFlagsPlugin) || !plug.isEnabled())
        {
            getLogger().warning("Could not load WorldGuard Custom Flags Plugin, disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        /*
        else
        {
            custPlugin = (WGCustomFlagsPlugin) plug;
        }
        */
        
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
        loadConfig();
        loadCache();
        
        listener = new WGRegionEffectsListener(wgPlugin, this);
        
        getServer().getPluginManager().registerEvents(listener, plug);
        
        //custPlugin.addCustomFlag(EFFECT_FLAG); //use flags.yml now
        
        scheduleTask();
    }

    @Override
    public void onDisable()
    {
        task.cancel();
        saveCache();
    }
    
    private void loadConfig()
    {
        confFile.getParentFile().mkdirs();
        getConfig().set("effect-duration", 2000);
        getConfig().set("effect-tick-delay", 1000);
        if (!confFile.exists())
        {
            try
            {
                if (!confFile.createNewFile())
                {
                    throw new IOException("Could not create configuration file.");
                }
                getLogger().log(Level.INFO, "Configuration does not exist. Creating default config.yml.");
                getConfig().save(confFile);
            }
            catch(IOException ex)
            {
                getLogger().log(Level.WARNING, "Could not write default configuration: ", ex);
            }
        }
        else
        {
            try
            {
                getConfig().load(confFile);
            }
            catch(Exception ex)
            {
                getLogger().log(Level.WARNING, "Could not load configuration:", ex);
            }
        }
        
        PotionEffectDesc.defaultLength = getConfig().getInt("effect-duration", 2000) / 50;
        tickDelay = getConfig().getInt("effect-tick-delay", 1000) / 50;
    }
    
    private void loadCache()
    {
        if (cacheFile.exists())
        {
            try
            {
                FileConfiguration cachedConf = new YamlConfiguration();
                cachedConf.load(confFile);
                if (cachedConf.contains("playerEffects") && cachedConf.isConfigurationSection("playerEffects"))
                {
                    ConfigurationSection section = cachedConf.getConfigurationSection("playerEffects");
                    for (String player : section.getKeys(false))
                    {
                        if (section.isList(player))
                        {
                            for (String name : section.getStringList(player))
                            {
                                if (!playerEffects.containsKey(player))
                                {
                                    playerEffects.put(player, new HashSet<PotionEffectDesc>());
                                }
                                String[] split = name.split(":", 2);
                                int amplifier = 0;
                                if (split.length > 1)
                                {
                                    try
                                    {
                                        amplifier = Integer.valueOf(split[1]);
                                    }
                                    catch(NumberFormatException ex)
                                    {
                                        getLogger().log(Level.WARNING, "Invalid amplifier: {0}", split[1]);
                                    }
                                }
                                PotionEffectType type = PotionEffectType.getByName(name);
                                if (type != null)
                                {
                                    playerEffects.get(player).add(new PotionEffectDesc(type, amplifier));
                                }
                                else
                                {
                                    getLogger().log(Level.WARNING, "Invalid effect type: {0}", name);
                                }
                            }
                        }
                    }
                }
                if (cachedConf.contains("activeEffects") && cachedConf.isConfigurationSection("activeEffects"))
                {
                    ConfigurationSection section = cachedConf.getConfigurationSection("activeEffects");
                    for (String player : section.getKeys(false))
                    {
                        if (section.isList(player))
                        {
                            for (String name : section.getStringList(player))
                            {
                                if (!activeEffects.containsKey(player))
                                {
                                    activeEffects.put(player, new HashSet<PotionEffectDesc>());
                                }
                                String[] split = name.split(":", 2);
                                int amplifier = 0;
                                if (split.length > 1)
                                {
                                    try
                                    {
                                        amplifier = Integer.valueOf(split[1]);
                                    }
                                    catch(NumberFormatException ex)
                                    {
                                        getLogger().log(Level.WARNING, "Invalid amplifier: {0}", split[1]);
                                    }
                                }
                                PotionEffectType type = PotionEffectType.getByName(name);
                                if (type != null)
                                {
                                    activeEffects.get(player).add(new PotionEffectDesc(type, amplifier));
                                }
                                else
                                {
                                    getLogger().log(Level.WARNING, "Invalid effect type: {0}", name);
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception ex)
            {
                getLogger().log(Level.WARNING, "Could not load effects cache: ", ex);
            }
        }
    }
    
    private void saveCache()
    {
        getLogger().log(Level.INFO, "Saving effect cache.");
        try
        {
            if (!cacheFile.exists())
            {
                if (!cacheFile.createNewFile())
                {
                    getLogger().log(Level.WARNING, "Failed to create cache file.");
                    return;
                }
            }
            FileConfiguration cachedConf = new YamlConfiguration();
            ConfigurationSection peSection = new MemoryConfiguration();
            for (String player : playerEffects.keySet())
            {
                Set<PotionEffectDesc> effects = playerEffects.get(player);
                if (effects.size() > 0)
                {
                    ArrayList<String> list = new ArrayList<String>();
                    for (PotionEffectDesc effect : effects)
                    {
                        list.add(effect.getType().getName() + ":" + effect.getAmplifier());
                    }
                    peSection.set(player, list);
                }
            }
            cachedConf.set("playerEffects", peSection);
            ConfigurationSection acSection = new MemoryConfiguration();
            for (String player : playerEffects.keySet())
            {
                Set<PotionEffectDesc> effects = activeEffects.get(player);
                if (effects.size() > 0)
                {
                    ArrayList<String> list = new ArrayList<String>();
                    for (PotionEffectDesc effect : effects)
                    {
                        list.add(effect.getType().getName() + ":" + effect.getAmplifier());
                    }
                    acSection.set(player, list);
                }
            }
            cachedConf.set("activeEffects", acSection);
            cachedConf.save(confFile);
            getLogger().log(Level.FINE, "Effect cache saved.");
        }
        catch(Exception ex)
        {
            getLogger().log(Level.WARNING, "Failed to save cache: ", ex);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("toggleeffects")
                || cmd.getName().equalsIgnoreCase("te"))
        {
            if (sender instanceof Player)
            {
                Player player = (Player) sender;

                if (!player.hasPermission("effects.toggle"))
                {
                    player.sendMessage(ChatColor.RED + "You don't have permission for that.");
                }
                else if (WGRegionEffectsPlugin.ignoredPlayers.contains(player))
                {
                    WGRegionEffectsPlugin.ignoredPlayers.remove(player);
                    player.sendMessage(ChatColor.GOLD + "Region effects toggled on.");
                }
                else
                {
                    WGRegionEffectsPlugin.ignoredPlayers.add(player);
                    player.sendMessage(ChatColor.GOLD + "Region effects toggled off.");
                }
            }
            else
            {
                sender.sendMessage("How could a console be affected by effects?");
            }
            return true;
        }
        return false;
    }

    private void scheduleTask()
    {
        task = getServer().getScheduler().runTaskTimer(this, new Runnable()
        {

            @Override
            public void run() 
            {
                
                synchronized(playerEffects)
                {
                    for (Player player : getServer().getOnlinePlayers())
                    {
                        if (ignoredPlayers.contains(player))
                        {
                            continue;
                        }
                        if (!playerEffects.containsKey(player.getName()))
                        {
                            playerEffects.put(player.getName(), new HashSet<PotionEffectDesc>());
                        }
                        Set<PotionEffectDesc> effects = playerEffects.get(player.getName());
                        if (!activeEffects.containsKey(player.getName()))
                        {
                            activeEffects.put(player.getName(), new HashSet<PotionEffectDesc>());
                        }
                        Set<PotionEffectDesc> curEffects = activeEffects.get(player.getName());
                        Collection<PotionEffect> actPots = player.getActivePotionEffects();
                        
                        APPLY_EFFECTS:
                        for (PotionEffectDesc effect : effects)
                        {
                            if (!curEffects.contains(effect))
                            {
                                for (PotionEffect eff : actPots)
                                {
                                    if (eff.getType() == effect.getType())
                                    {
                                        if (eff.getAmplifier() >= effect.getAmplifier())
                                        {
                                            continue APPLY_EFFECTS;
                                        }
                                        else
                                        {
                                            if (!cachedEffects.containsKey(player.getName()))
                                            {
                                                cachedEffects.put(player.getName(), new HashMap<PotionEffectDesc, Integer>());
                                            }
                                            Map<PotionEffectDesc, Integer> cEffects = cachedEffects.get(player.getName());
                                            cEffects.put(new PotionEffectDesc(eff.getType(), eff.getAmplifier()), eff.getDuration());
                                            player.removePotionEffect(eff.getType());
                                        }
                                    }
                                }
                                curEffects.add(effect);
                                effect.createEffect().apply(player);
                            }
                        }
                        Iterator<PotionEffectDesc> itr = curEffects.iterator();
                        while (itr.hasNext())
                        {
                            PotionEffectDesc effect = itr.next();
                            if (!effects.contains(effect))
                            {
                                player.removePotionEffect(effect.getType());
                                itr.remove();
                                if (cachedEffects.containsKey(player.getName()))
                                {
                                    HashSet<PotionEffectDesc> removeThis = new HashSet<PotionEffectDesc>();
                                    for (PotionEffectDesc cached : cachedEffects.get(player.getName()).keySet())
                                    {
                                        if (cached.getType() == effect.getType())
                                        {
                                            cached.createEffect(cachedEffects.get(player.getName()).get(cached)).apply(player);
                                            removeThis.add(cached);
                                        }
                                    }
                                    for (PotionEffectDesc desc : removeThis)
                                    {
                                        cachedEffects.get(player.getName()).remove(desc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }, 5L, tickDelay);
    }
}

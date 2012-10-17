/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEffects.flags;

import com.mewin.WGCustomFlags.flags.CustomFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author patrick
 */
public class PotionEffectFlag extends CustomFlag<PotionEffectDesc>{

    public PotionEffectFlag(String name, RegionGroup rg)
    {
        super(name, rg);
    }
    
    public PotionEffectFlag(String name)
    {
        super(name);
    }
    
    @Override
    public PotionEffectDesc loadFromDb(String str) {
        String[] split = str.split("\\|");
        
        return new PotionEffectDesc(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]));
    }

    @Override
    public String saveToDb(PotionEffectDesc o) {
        return o.getType().getName() + "|" + o.getAmplifier();
    }

    @Override
    public PotionEffectDesc parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
        String[] split = input.split(" ");
        
        int amplifier = 0;
        
        if (split.length > 1)
        {
            try
            {
                amplifier = Integer.parseInt(split[1]) - 1;
            }
            catch(NumberFormatException ex)
            {
                amplifier = getAmplifier(split[1]);
            }
        }
        
        PotionEffectType type = PotionEffectType.getByName(split[0]);
        
        if (type == null)
        {
            throw new InvalidFlagFormat("Unknown potion type: " + split[0]);
        }
        
        return new PotionEffectDesc(type, amplifier);
    }

    @Override
    public PotionEffectDesc unmarshal(Object o) {
        String[] split = ((String) o).split("\\|");
        
        return new PotionEffectDesc(PotionEffectType.getByName(split[0]), Integer.parseInt(split[1]));
    }

    @Override
    public Object marshal(PotionEffectDesc o) {
        return         o.getType().getName() + "|" + o.getAmplifier();
    }
    
    private int getAmplifier(String is) throws InvalidFlagFormat
    {
        switch(is)
        {
            case "I":
                return 0;
            case "II":
                return 1;
            case "III":
                return 2;
            case "IV":
                return 3;
            case "V":
                return 4;
            case "VI":
                return 5;
            default:
                throw new InvalidFlagFormat("Number expected, string found: " + is);
        }
    }
    
}

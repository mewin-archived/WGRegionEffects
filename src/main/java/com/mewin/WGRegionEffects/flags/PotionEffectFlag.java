/*
 * Copyright (C) 2012 mewin
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
package com.mewin.WGRegionEffects.flags;

import com.mewin.WGCustomFlags.flags.CustomFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author mewin
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
        if (is.equalsIgnoreCase("I"))
        {
            return 0;
        }
        else if (is.equalsIgnoreCase("II"))
        {
            return 1;
        }
        else if (is.equalsIgnoreCase("III"))
        {
            return 2;
        }
        else if (is.equalsIgnoreCase("IV"))
        {
            return 3;
        }
        else if (is.equalsIgnoreCase("V"))
        {
            return 4;
        }
        else if (is.equalsIgnoreCase("VI"))
        {
            return 5;
        }
        else {
            throw new InvalidFlagFormat("Number expected, string found: " + is);
        }
    }
}

/*
 * Copyright (C) 2012 patrick
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

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author patrick
 */
public class PotionEffectDesc {
    private PotionEffectType type;
    private int amplifier;
    
    public PotionEffectDesc(PotionEffectType type, int amplifier)
    {
        this.type = type;
        this.amplifier = amplifier;
    }
    
    public PotionEffectType getType()
    {
        return type;
    }
    
    public int getAmplifier()
    {
        return amplifier;
    }
    
    public PotionEffect createEffect()
    {
        return type.createEffect(40, amplifier);
    }
    
    @Override
    public String toString()
    {
        return type.getName() + " " + amplifierString();
    }
    
    private String amplifierString()
    {
        switch(amplifier)
        {
            case 0:
                return "I";
            case 1:
                return "II";
            case 2:
                return "III";
            case 3:
                return "IV";
            case 4:
                return "V";
            case 5:
                return "VI";
            default:
                return String.valueOf(amplifier);
        }
    }
}

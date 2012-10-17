/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

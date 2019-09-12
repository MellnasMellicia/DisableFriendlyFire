package com.github.siloneco.listeners;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DamageCancelListener implements Listener {

    private final List<PotionEffectType> negativePotionEffectTypes = Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.GLOWING,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        Entity attacker = e.getDamager();

        if ( !(ent instanceof Player) || !(attacker instanceof Player) ) {
            return;
        }

        Player p = (Player) ent;

        if ( !p.hasPermission("disablefriendlyfire.apply") ) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageByArrowOr(EntityDamageByEntityEvent e) {
        Entity ent = e.getEntity();
        Entity attacker = e.getDamager();

        if ( !(ent instanceof Player) ) {
            return;
        }

        if ( attacker instanceof Arrow ) {

            Arrow arrow = (Arrow) attacker;

            if ( !(arrow.getShooter() instanceof Player) ) {
                return;
            }
        } else if ( attacker instanceof Trident ) {
            Trident trident = (Trident) attacker;

            if ( !(trident.getShooter() instanceof Player) ) {
                return;
            }
        } else {
            return;
        }

        Player p = (Player) ent;
        if ( !p.hasPermission("disablefriendlyfire.apply") ) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGetPotionEffect(PotionSplashEvent e) {

        ThrownPotion pot = e.getPotion();
        if ( !(pot.getShooter() instanceof Player) ) {
            return;
        }
        Player shooter = (Player) pot.getShooter();

        boolean includeNegativePotionEffect = false;
        for ( PotionEffect eff : e.getPotion().getEffects() ) {
            if ( isNegativePotionEffect(eff.getType()) ) {
                includeNegativePotionEffect = true;
                break;
            }
        }
        if ( !includeNegativePotionEffect ) {
            return;
        }

        for ( LivingEntity ent : e.getAffectedEntities() ) {
            if ( !(ent instanceof Player) ) {
                continue;
            }

            Player p = (Player) ent;

            if ( p == shooter ) {
                continue;
            }
            if ( !p.hasPermission("disablefriendlyfire.apply") ) {
                continue;
            }

            e.setIntensity(ent, 0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGetPotionEffectFromAreaClouds(AreaEffectCloudApplyEvent e) {
        AreaEffectCloud potion = e.getEntity();
        if ( !(potion.getSource() instanceof Player) ) {
            return;
        }

        Player shooter = (Player) potion.getSource();

        boolean includeNegativePotionEffect = false;

        if ( isNegativePotionEffect(potion.getBasePotionData().getType().getEffectType()) ) {
            includeNegativePotionEffect = true;
        }
        if ( !includeNegativePotionEffect ) {
            for ( PotionEffect eff : potion.getCustomEffects() ) {
                if ( isNegativePotionEffect(eff.getType()) ) {
                    includeNegativePotionEffect = true;
                    break;
                }
            }
        }
        if ( !includeNegativePotionEffect ) {
            return;
        }

        for ( LivingEntity entity : e.getAffectedEntities() ) {
            if ( !(entity instanceof Player) ) {
                continue;
            }

            Player p = (Player) entity;

            if ( p == shooter ) {
                continue;
            }
            if ( !p.hasPermission("disablefriendlyfire.apply") ) {
                continue;
            }

            e.setCancelled(true);
        }
    }

    private boolean isNegativePotionEffect(PotionEffectType type) {
        return negativePotionEffectTypes.contains(type);
    }
}

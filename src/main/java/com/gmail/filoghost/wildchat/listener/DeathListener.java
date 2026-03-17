/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.wildchat.listener;

import static com.gmail.filoghost.wildchat.WildChat.*;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import wild.api.translation.Translation;

public class DeathListener implements Listener {
	
	@EventHandler (priority = EventPriority.LOW)
	public void playerDeath(PlayerDeathEvent event) {
		
		if (mainConfig.hideDeathMessages) {
			event.setDeathMessage(null);
			return;
		}
		
		Player dead = event.getEntity();
		LivingEntity killerFormat = null;
		boolean foundCause = false;
		EntityDamageEvent damageEvent = dead.getLastDamageCause();
		
		if (damageEvent == null) {
			event.setDeathMessage(mainConfig.death_generic);
			foundCause = true;
		}
		
		if (!foundCause && damageEvent instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) damageEvent;
			Entity killer = damageByEntity.getDamager();
			
			if (killer instanceof Projectile) {
				Projectile projectile = (Projectile) killer;
				ProjectileSource shooter = projectile.getShooter();
				
				if (projectile instanceof Arrow) {
					if (shooter instanceof LivingEntity) {
						killerFormat = (LivingEntity) shooter;
						event.setDeathMessage(mainConfig.death_byEntity_arrow);
					} else {
						event.setDeathMessage(mainConfig.death_arrow);
					}
					
				} else if (projectile instanceof LargeFireball || projectile instanceof SmallFireball) {
					if (shooter instanceof LivingEntity) {
						killerFormat = (LivingEntity) shooter;
						event.setDeathMessage(mainConfig.death_byEntity_fireball);
					} else {
						event.setDeathMessage(mainConfig.death_fireball);
					}
					
				} else if (projectile instanceof ThrownPotion) {
					if (shooter instanceof LivingEntity) {
						killerFormat = (LivingEntity) shooter;
						event.setDeathMessage(mainConfig.death_byEntity_magic);
					} else {
						event.setDeathMessage(mainConfig.death_magic);
					}
					
				} else { // Altri tipi di proiettile
					if (shooter instanceof LivingEntity) {
						killerFormat = (LivingEntity) shooter;
						event.setDeathMessage(mainConfig.death_byEntity_projectile);
					} else {
						event.setDeathMessage(mainConfig.death_projectile);
					}
				}
				foundCause = true;
				
			} else if (killer instanceof LivingEntity) {
				
				killerFormat = (LivingEntity) killer;
				event.setDeathMessage(mainConfig.death_byEntity_melee);
				foundCause = true;
				
			}
		}
		
		if (!foundCause) {
			switch (damageEvent.getCause()) {
				case BLOCK_EXPLOSION:
					event.setDeathMessage(mainConfig.death_explosion);
					break;
				case CONTACT:
					event.setDeathMessage(mainConfig.death_cactus);
					break;
				case DROWNING:
					event.setDeathMessage(mainConfig.death_water);
					break;
				case ENTITY_EXPLOSION:
					event.setDeathMessage(mainConfig.death_explosion);
					break;
				case FALL:
					event.setDeathMessage(mainConfig.death_fall);
					break;
				case FALLING_BLOCK:
					event.setDeathMessage(mainConfig.death_anvil);
					break;
				case FIRE:
					event.setDeathMessage(mainConfig.death_inFire);
					break;
				case FIRE_TICK:
					event.setDeathMessage(mainConfig.death_onFire);
					break;
				case LAVA:
					event.setDeathMessage(mainConfig.death_lava);
					break;
				case LIGHTNING:
					event.setDeathMessage(mainConfig.death_lightning);
					break;
				case MAGIC:
					event.setDeathMessage(mainConfig.death_magic);
					break;
				case POISON:
					event.setDeathMessage(mainConfig.death_poison);
					break;
				case STARVATION:
					event.setDeathMessage(mainConfig.death_starve);
					break;
				case SUFFOCATION:
					event.setDeathMessage(mainConfig.death_wall);
					break;
				case SUICIDE:
					event.setDeathMessage(mainConfig.death_suicide);
					break;
				case THORNS:
					event.setDeathMessage(mainConfig.death_thorns);
					break;
				case VOID:
					event.setDeathMessage(mainConfig.death_void);
					break;
				case WITHER:
					event.setDeathMessage(mainConfig.death_wither);
					break;
					
				case CUSTOM:
				case ENTITY_ATTACK:
				case PROJECTILE:
				case MELTING:
				default:
					event.setDeathMessage(mainConfig.death_generic);
					break;
			}
			foundCause = true;
		}
		
		event.setDeathMessage(event.getDeathMessage().replace("{PLAYER}", dead.getName()));
		
		if (killerFormat != null) {
			if (killerFormat.getType() == EntityType.PLAYER) {
				event.setDeathMessage(event.getDeathMessage().replace("{KILLER}", ((Player) killerFormat).getName()));
			} else {
				event.setDeathMessage(event.getDeathMessage().replace("{KILLER}", Translation.of(killerFormat.getType())));
			}
		}
	}
	
}

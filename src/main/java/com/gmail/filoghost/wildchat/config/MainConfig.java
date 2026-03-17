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
package com.gmail.filoghost.wildchat.config;

import java.lang.reflect.Field;
import net.cubespace.yamler.YamlerConfig;
import net.cubespace.yamler.YamlerConfigurationException;

import org.bukkit.plugin.Plugin;

import wild.api.WildCommons;

public class MainConfig extends YamlerConfig {

	public MainConfig(Plugin plugin) {
		super(plugin, "config.yml");
	}

	public String chatFormat = "{PREFIX}{NAME}{SUFFIX}: ";
	public boolean chatFormatEnable = true;
	
	@Colors public String message_join = "{PLAYER} è entrato";
	@Colors public String message_leave = "{PLAYER} è uscito";
	@Colors public String message_kick = "{PLAYER} è stato kickato";
	
	@Colors public String death_generic = "{PLAYER} è morto";
	@Colors public String death_inFire = "{PLAYER} è morto";
	@Colors public String death_onFire = "{PLAYER} è morto";
	@Colors public String death_lava = "{PLAYER} è morto";
	@Colors public String death_wall = "{PLAYER} è morto";
	@Colors public String death_water = "{PLAYER} è morto";
	@Colors public String death_starve = "{PLAYER} è morto";
	@Colors public String death_cactus = "{PLAYER} è morto";
	@Colors public String death_fall = "{PLAYER} è morto";
	@Colors public String death_void = "{PLAYER} è morto";
	@Colors public String death_explosion = "{PLAYER} è morto";
	@Colors public String death_arrow = "{PLAYER} è morto";
	@Colors public String death_magic = "{PLAYER} è morto";
	@Colors public String death_fireball = "{PLAYER} è morto";
	@Colors public String death_projectile = "{PLAYER} è morto";
	@Colors public String death_poison = "{PLAYER} è morto";
	@Colors public String death_wither = "{PLAYER} è morto";
	@Colors public String death_lightning = "{PLAYER} è morto";
	@Colors public String death_suicide = "{PLAYER} è morto";
	@Colors public String death_thorns = "{PLAYER} è morto";
	@Colors public String death_anvil = "{PLAYER} è morto";
	
	@Colors public String death_byEntity_arrow = "{PLAYER} è morto ucciso da {KILLER}";
	@Colors public String death_byEntity_fireball = "{PLAYER} è morto ucciso da {KILLER}";
	@Colors public String death_byEntity_melee = "{PLAYER} è morto ucciso da {KILLER}";
	@Colors public String death_byEntity_magic = "{PLAYER} è morto ucciso da {KILLER}";
	@Colors public String death_byEntity_projectile = "{PLAYER} è morto ucciso da {KILLER}";
	
	public boolean hideDeathMessages = false;

	
	@Override
	public void init() throws YamlerConfigurationException {
		super.init();
		
		try {
			addColors();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addColors() throws Exception {
		for (Field field : getClass().getDeclaredFields()) {
			
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			
			if (field.isAnnotationPresent(Colors.class)) {
				Object value = field.get(this);
				
				if (value != null && value instanceof String) {
					field.set(this, WildCommons.color((String) value));
				}
				
			}
		}
	}
}

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
package com.gmail.filoghost.wildchat;

import net.cubespace.yamler.YamlerConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.wildchat.censor.CensorConfig;
import com.gmail.filoghost.wildchat.censor.CensorConfigDownloader;
import com.gmail.filoghost.wildchat.command.ChatCommand;
import com.gmail.filoghost.wildchat.command.ChatConfirmCommand;
import com.gmail.filoghost.wildchat.config.MainConfig;
import com.gmail.filoghost.wildchat.listener.ChatListener;
import com.gmail.filoghost.wildchat.listener.DeathListener;
import com.gmail.filoghost.wildchat.listener.JoinQuitListener;


public class WildChat extends JavaPlugin {
	
	public static final String ADMIN_PERMISSION = "wildchat.admin";

	public static Plugin plugin;
	public static MainConfig mainConfig;
	public static CensorConfig censorConfig;
	public static boolean debug;
	
	@Override
	public void onEnable() {
		plugin = this;
		mainConfig = new MainConfig(this);
		censorConfig = new CensorConfig(this);
		CensorConfigDownloader.register();
		
		new ChatCommand(this);
		new ChatConfirmCommand(this);
		
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "ChatCallback");
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		load();
	}
	
	public static void load() {
		try {
			mainConfig.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			plugin.getLogger().severe("Impossibile leggere config.yml!");
		}
		
		try {
			censorConfig.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			plugin.getLogger().severe("Impossibile leggere censor.yml!");
		}
		
		ChatListener.duplicates.clear();
		ChatListener.disabledChat.clear();
	}

}

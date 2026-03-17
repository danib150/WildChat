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
package com.gmail.filoghost.wildchat.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.wildchat.WildChat;
import com.gmail.filoghost.wildchat.listener.ChatListener;

import wild.api.command.SubCommandFramework;

public class ChatCommand extends SubCommandFramework {

	public ChatCommand(JavaPlugin plugin) {
		super(plugin, "chat");
	}
	
	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "Comandi chat:");
		sender.sendMessage(ChatColor.YELLOW + "/chat on|off" + ChatColor.GRAY + " - Abilita o disabilita la chat.");
		if (sender.hasPermission(WildChat.ADMIN_PERMISSION)) {
			sender.sendMessage(ChatColor.YELLOW + "/chat reload" + ChatColor.GRAY + " - Ricarica il plugin.");
			sender.sendMessage(ChatColor.YELLOW + "/chat clear" + ChatColor.GRAY + " - Pulisci la chat.");
		}
	}
	
	
	@SubCommand("debug")
	public void debug(CommandSender sender, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			WildChat.debug = !WildChat.debug;
			sender.sendMessage("Debug ora " + (WildChat.debug ? "abilitato" : "disabilitato"));
		}
	}
	
	@SubCommand("on")
	public void on(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		
		if (!ChatListener.disabledChat.contains(player)) {
			player.sendMessage(ChatColor.YELLOW + "La chat è già attivata.");
			return;
		}
		
		ChatListener.disabledChat.remove(player);
		player.sendMessage(ChatColor.YELLOW + "Hai abilitato la chat.");
	}
	
	@SubCommand("off")
	public void off(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);

		if (ChatListener.disabledChat.contains(player)) {
			player.sendMessage(ChatColor.YELLOW + "La chat è già disattivata.");
			return;
		}
		
		ChatListener.disabledChat.add(player);
		player.sendMessage(ChatColor.YELLOW + "Hai disabilitato la chat.");
	}

	@SubCommand("reload")
	@SubCommandPermission(WildChat.ADMIN_PERMISSION)
	public void reload(CommandSender sender, String label, String[] args) {
		WildChat.load();
		sender.sendMessage(ChatColor.GREEN + "Reload completato!");
	}

	@SubCommand("clean")
	@SubCommandPermission(WildChat.ADMIN_PERMISSION)
	public void clean(CommandSender sender, String label, String[] args) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(StringUtils.repeat(" \n", 102));
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "La chat è stata cancellata." + ChatColor.GRAY + "]");
		}
	}
}

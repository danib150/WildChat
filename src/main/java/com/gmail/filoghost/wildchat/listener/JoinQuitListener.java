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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.wildchat.command.ChatConfirmCommand;

import static com.gmail.filoghost.wildchat.WildChat.mainConfig;

public class JoinQuitListener implements Listener {
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void playerJoin(PlayerJoinEvent event) {
		
		final Player player = event.getPlayer();
		String name = player.getName();
		
		if (mainConfig.message_join != null && !mainConfig.message_join.isEmpty()) {
			event.setJoinMessage(mainConfig.message_join.replace("{PLAYER}", name));
		} else {
			event.setJoinMessage(null);
		}
	}
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		ChatListener.duplicates.remove(event.getPlayer());
		ChatListener.disabledChat.remove(event.getPlayer());
		ChatConfirmCommand.lastWarnMessage.remove(event.getPlayer());
		
		if (mainConfig.message_leave != null && !mainConfig.message_leave.isEmpty()) {
			event.setQuitMessage(mainConfig.message_leave.replace("{PLAYER}", event.getPlayer().getName()));
		} else {
			event.setQuitMessage(null);
		}
	}
	
	@EventHandler
	public void playerKick(PlayerKickEvent event) {
		if (mainConfig.message_kick != null && !mainConfig.message_kick.isEmpty()) {
			event.setLeaveMessage(mainConfig.message_kick.replace("{PLAYER}", event.getPlayer().getName()));
		} else {
			event.setLeaveMessage(null);
		}
	}
	
}

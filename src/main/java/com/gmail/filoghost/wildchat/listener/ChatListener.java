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

import static com.gmail.filoghost.wildchat.WildChat.mainConfig;
import static com.gmail.filoghost.wildchat.WildChat.censorConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import wild.api.WildCommons;
import wild.api.bridges.PexBridge;
import wild.api.bridges.PexBridge.PrefixSuffix;

import com.gmail.filoghost.wildchat.censor.Replacement;
import com.gmail.filoghost.wildchat.censor.matches.Match;
import com.gmail.filoghost.wildchat.command.ChatConfirmCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ChatListener implements Listener {

	private static final int MAX_CAPS = 5;
	private static final int MAX_REPEAT = 5;
	
	public static final Pattern NON_WORDS_PATTERN = Pattern.compile("[\\P{IsLatin}]+");
	
	private static final Pattern DOMAIN_PATTERN = Pattern.compile(
			"(" +
				"https?:\\/\\/\\S+" + // Un qualsiasi url con http(s) davanti e qualcosa di non vuoto dopo
			"|" +
				"(([a-zA-Z0-9\\-]{1,}\\.)+)([a-zA-Z0-9\\-]{2,}\\.)+[a-zA-Z]{2,8}" + // Dominio con 2 punti, tipo ex.sito.com, qualunque TLD (anche quelli nuovi strani)
			"|" +
				"([a-zA-Z0-9\\-]{2,}\\.)(it|com|net|org|eu|mc|es|uk|us)" + // Dominio con un punto solo, e i TLD più comuni tipo wildadventure.eu
			")" +
			"\\S*" // Tutti i caratteri non vuoti dopo
			);
	
	private static final Pattern IP_PATTERN = Pattern.compile("[0-9]{1,5}\\.[0-9]{1,5}\\.[0-9]{1,5}\\.[0-9]{1,5}(:[0-9]{1,10})?");

	public static Map<Player, PastMessages> duplicates = Maps.newHashMap();
	public static List<Player> disabledChat = Lists.newArrayList();
	public static List<Player> ignoreWarns = Lists.newArrayList();


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // Normal così la formattazione è visibile ad altri plugin
	public void chatNormal(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		if (disabledChat.contains(player)) {
			player.sendMessage(ChatColor.YELLOW + "Hai la chat disabilitata. Per attivarla scrivi /chat on.");
			event.setCancelled(true);
			return;
		}

		// Formattazione chat
		if (mainConfig.chatFormatEnable) {
			String format = mainConfig.chatFormat;

			PrefixSuffix prefixSuffix = PexBridge.getCachedPrefixSuffix(player);

			format = format.replace("{NAME}", "%1$2s");
			format = format.replace("{PREFIX}", prefixSuffix.getPrefix());
			format = format.replace("{SUFFIX}", prefixSuffix.getSuffix());
			
			event.setFormat(WildCommons.color(format) + "%2$2s");
		}
		
		event.getRecipients().removeAll(disabledChat);
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Highest perché altri plugin potrebbero cancellare l'evento prima
	public void chatHighest(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		final String originalMsg = event.getMessage();
		
		// Questo è per tutti, anche per lo staff
		StringBuilder replacedMessageBuilder = new StringBuilder(originalMsg.toLowerCase());
		for (Replacement beforeCheckReplacement : censorConfig.replaceBeforeCheckList) {
			String find = beforeCheckReplacement.getFind();
			String replace = beforeCheckReplacement.getReplace();
			
			int index = 0;
			while ((index = replacedMessageBuilder.indexOf(find, index)) >= 0) {
				replacedMessageBuilder.replace(index, index + find.length(), replace);
			}
		}
		
		String replacedMessage = replacedMessageBuilder.toString();
		String[] singleWords = NON_WORDS_PATTERN.split(replacedMessage);
		
		for (Match blockMatch : censorConfig.blockMatches) {
			if (blockMatch.matches(replacedMessage, singleWords)) {
				event.setCancelled(true);
				player.sendMessage("");
				player.sendMessage(ChatColor.RED + "Il messaggio contiene volgarità o spam ed è stato bloccato.");
				WildCommons.fancyMessage("Per evitare sanzioni, consulta il ").color(ChatColor.RED)
					.then("regolamento \u2197").color(ChatColor.AQUA)
						.link("https://wildadventure.it/regole")
						.tooltip(ChatColor.GRAY + "Clicca per aprire il link")
					.send(player);
				player.sendMessage(ChatColor.YELLOW + "Aggirare questo filtro comporta sanzioni più severe.");
				player.sendMessage("");
				return;
			}
		}
		
		// Il giocatore viene aggiunto alla lista solo temporaneamente quando si esegue il comando per ignorare il warn.
		if (!ignoreWarns.contains(player)) {
			for (Match warnMatch : censorConfig.warnMatches) {
				if (warnMatch.matches(replacedMessage, singleWords)) {
					event.setCancelled(true);
					
					ChatConfirmCommand.lastWarnMessage.put(player, originalMsg);
					
					player.sendMessage("");
					player.sendMessage(ChatColor.RED + "Il messaggio potrebbe contenere volgarità o spam.");
					WildCommons.fancyMessage("Per evitare sanzioni, consulta il ").color(ChatColor.RED)
						.then("regolamento \u2197").color(ChatColor.AQUA)
							.link("https://wildadventure.it/regole")
							.tooltip(ChatColor.GRAY + "Clicca per aprire il link")
						.send(player);
					WildCommons.fancyMessage("")
						.then("CONFERMA").color(ChatColor.YELLOW).style(ChatColor.UNDERLINE)
							.command("/chatconfirm")
							.tooltip(ChatColor.GRAY + "Clicca per confermare l'invio del messaggio")
						.then(" l'invio se pensi che si tratti di un errore.").color(ChatColor.RED)
						.send(player);
					player.sendMessage("");
					return;
				}
			}
		}
		
		boolean canBypass = player.hasPermission("wildchat.bypass");
		
		if (!canBypass) {
			// Conteggio CAPS
			int capsCount = 0;
			for (char c : originalMsg.toCharArray()) {
				if (Character.isUpperCase(c)) {
					capsCount += 1;
				}
			}
			if (capsCount > MAX_CAPS) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "Per favore non usare troppe maiuscole!");
				return;
			}

			// Controllo messaggi doppi
			String msgWithoutSymbols = originalMsg.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			PastMessages pastMessages = duplicates.get(player);
			if (pastMessages != null) {
				if (pastMessages.getPastMessages().contains(msgWithoutSymbols)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Hai già mandato questo messaggio recentemente!");
					return;
				}
			} else {
				pastMessages = new PastMessages(TimeUnit.SECONDS, 30); //TODO controllare lunghezza dall'ultimo messaggio, per vedere se ha scritto troppo in fretta
				duplicates.put(player, pastMessages);
			}
			
			pastMessages.add(msgWithoutSymbols);
		}
		
		
		// Modificabile
		String msg = originalMsg;
		
		if (!canBypass) {
			
			// Accorcia i caratteri ripetuti
			msg = cutRepeatedCharacters(msg);
			
			// Censura domini
			Matcher domainMatcher = DOMAIN_PATTERN.matcher(msg);
			while (domainMatcher.find()) {
				String domainToReplace = domainMatcher.group(0);
				String lowercaseDomain = domainToReplace.toLowerCase();
				boolean replace = true;
				
				for (String safeDomain : censorConfig.allowedDomains) {
					if (lowercaseDomain.contains(safeDomain)) {
						replace = false;
						break;
					}
				}
				
				if (replace) {
					msg = msg.replace(domainToReplace, domainToReplace.replaceAll("[^\\.\\/]", "*")); // Lascia solo punti e slash
				}

			}
			
			// Censura IP
			Matcher ipMatcher = IP_PATTERN.matcher(msg);
			while (ipMatcher.find()) {
				String ipToReplace = ipMatcher.group();
				
				msg = msg.replace(ipToReplace, ipToReplace.replaceAll("[0-9]", "*"));
			}
		}
		
		// Colori alla fine perché il messaggio deve essere integro prima
		if (player.hasPermission("wildchat.colors")) {
			msg = WildCommons.color(msg);
		}
		
		event.setMessage(msg);
	}
	

	private static String cutRepeatedCharacters(String msg) {
		StringBuilder output = new StringBuilder();
		
		char lastChar = 0;
		int count = 0;
		
		for (char c : msg.toCharArray()) {
			if (lastChar != c) {
				// Ogni volta che il carattere cambia, lo salva e azzera il conto
				lastChar = c;
				count = 0;
			}
			
			count++;
			
			if (count <= MAX_REPEAT) {
				// Altrimenti non viene aggiunto
				output.append(c);
			}
		}

		return output.toString();
	}

}

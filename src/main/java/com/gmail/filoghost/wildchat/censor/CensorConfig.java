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
package com.gmail.filoghost.wildchat.censor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.wildchat.WildChat;
import com.gmail.filoghost.wildchat.censor.matches.ContainsMatch;
import com.gmail.filoghost.wildchat.censor.matches.FullWordMatch;
import com.gmail.filoghost.wildchat.censor.matches.Match;
import com.gmail.filoghost.wildchat.censor.matches.RegexMatch;
import com.gmail.filoghost.wildchat.censor.matches.WordStartMatch;
import com.google.common.collect.Lists;

import net.cubespace.yamler.Path;
import net.cubespace.yamler.YamlerConfig;
import net.cubespace.yamler.YamlerConfigurationException;

public class CensorConfig extends YamlerConfig {
	
	public CensorConfig(Plugin plugin) {
		this.configFile = getFile();
		this.configHeader = new String[] {
			"Non modificare direttamente questo file, viene aggiornato automaticamente."
		};
	}
	
	public static File getFile() {
		return new File(WildChat.plugin.getDataFolder(), "censor.yml");
	}
	
	@Path("block.full-words")
	private List<String> block_fullWords;
	
	@Path("block.word-start")
	private List<String> block_wordStart;
	
	@Path("block.contains")
	private List<String> block_contains;
	
	@Path("block.regular-expressions")
	private List<String> block_regularExpressions;
	
	@Path("warn.full-words")
	private List<String> warn_fullWords;
	
	@Path("warn.word-start")
	private List<String> warn_wordStart;
	
	@Path("warn.contains")
	private List<String> warn_contains;
	
	@Path("warn.regular-expressions")
	private List<String> warn_regularExpressions;
	
	
	@Path("replace-before-check")
	private List<String> replaceBeforeCheck;
	
	
	@Path("allowed-domains")
	public List<String> allowedDomains;
	
	
	// Generate, non caricare dal config
	public transient List<Match> blockMatches;
	public transient List<Match> warnMatches;
	public transient List<Replacement> replaceBeforeCheckList;
	
	
	@Override
	public void init() throws YamlerConfigurationException {
		super.init();
		
		blockMatches = Lists.newArrayList();
		warnMatches = Lists.newArrayList();
		replaceBeforeCheckList = Lists.newArrayList();
		
		for (Field field : getClass().getDeclaredFields()) {
			Path pathAnnotation = field.getAnnotation(Path.class);
			if (pathAnnotation != null) {
				try {
					if (field.get(this) == null) {
						throw new YamlerConfigurationException("path not set: " + pathAnnotation.value());
					}
				} catch (Exception e) {
					throw new YamlerConfigurationException(e);
				}
			}
		}
		
		for (String s : block_fullWords) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				blockMatches.add(new FullWordMatch(combination.toLowerCase()));
			}
		}
		for (String s : block_wordStart) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				blockMatches.add(new WordStartMatch(combination.toLowerCase()));
			}
		}
		for (String s : block_contains) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				blockMatches.add(new ContainsMatch(combination.toLowerCase()));
			}
		}
		for (String s : block_regularExpressions) {
			try {
				blockMatches.add(new RegexMatch(s));
			} catch (PatternSyntaxException ex) {
				// Non blocca tutto il resto
				ex.printStackTrace();
				WildChat.plugin.getLogger().warning("Pattern non valido: " + s);
			}
		}
		
		for (String s : warn_fullWords) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				warnMatches.add(new FullWordMatch(combination.toLowerCase()));
			}
		}
		for (String s : warn_wordStart) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				warnMatches.add(new WordStartMatch(combination.toLowerCase()));
			}
		}
		for (String s : warn_contains) {
			for (String combination : RegexExpander.expandCombinations(s)) {
				warnMatches.add(new ContainsMatch(combination.toLowerCase()));
			}
		}
		for (String s : warn_regularExpressions) {
			try {
				warnMatches.add(new RegexMatch(s));
			} catch (PatternSyntaxException ex) {
				ex.printStackTrace();
				WildChat.plugin.getLogger().warning("Pattern non valido: " + s);
			}
		}
		
		for (String s : replaceBeforeCheck) {
			String[] parts = s.split("=", 2);
			if (parts.length < 2) {
				WildChat.plugin.getLogger().warning("Replacement non valido, deve contenere '=': " + s);
				continue;
			}
			
			String[] finds = parts[0].split(",");
			String replacement = parts[1];
			
			for (String find : finds) {
				// Tutto lowercase anche qui
				replaceBeforeCheckList.add(new Replacement(find.trim().toLowerCase(), replacement.trim().toLowerCase()));
			}
		}

		// Tutto lowercase
		for (int i = 0; i < allowedDomains.size(); i++) {
			allowedDomains.set(i, allowedDomains.get(i).toLowerCase());
		}
		
	}

	@Override
	public void save() throws YamlerConfigurationException {
		throw new YamlerConfigurationException("This file shouldn't be saved");
	}

	@Override
	public void save(File file) throws YamlerConfigurationException {
		throw new YamlerConfigurationException("This file shouldn't be saved");
	}
	
	

}

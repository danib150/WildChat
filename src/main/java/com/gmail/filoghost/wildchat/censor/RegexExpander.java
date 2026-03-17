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

import java.util.List;

import com.google.common.collect.Lists;

import net.cubespace.yamler.YamlerConfigurationException;

public class RegexExpander {
	
	public static List<String> expandCombinations(String s) throws YamlerConfigurationException {
		List<Part> parts = Lists.newArrayList();
		Part lastPart = null;
		
		for (char c : s.toCharArray()) {
			if (c == '(') {
				if (lastPart instanceof MultiPart) {
					throw new YamlerConfigurationException(s + ": unexpected '(' alternatives cannot be nested");
				}
				
				// Start multi part
				parts.add(lastPart = new MultiPart());
				
			} else if (c == '|') {
				
				if (!(lastPart instanceof MultiPart)) {
					throw new YamlerConfigurationException(s + ": unexpected '|' cannot found opening parenthesis '('");
				}
				((MultiPart) lastPart).newPart();
				
			} else if (c == ')') {
				// Reset
				
				if (!(lastPart instanceof MultiPart)) {
					throw new YamlerConfigurationException(s + ": unexpected ')' cannot found opening parenthesis '('");
				}
				lastPart = null;
				
			} else {
				if (lastPart == null) {
					parts.add(lastPart = new FixedPart());
				}
				
				lastPart.append(c);
			}
		}
		
		if (lastPart instanceof MultiPart) {
			throw new YamlerConfigurationException(s + ": expected ')' at the end");
		}
		
		List<String> combinations = Lists.newArrayList();
		combinations.add("");
		
		for (Part part : parts) {
			List<String> previousCombinations = Lists.newArrayList(combinations);
			combinations = Lists.newArrayList();
			
			for (String previousPartialCombination : previousCombinations) {
				for (String newPartialCombination : part.getPartialCombinations()) {
					combinations.add(previousPartialCombination + newPartialCombination);
					
					if (combinations.size() > 200) {
						throw new YamlerConfigurationException(s + ": too many combinations (max 200), use regular expressions");
					}
				}
			}
		}
		
		return combinations;
	}
	
	
	private static abstract class Part {
		
		public abstract void append(char c);
		
		public abstract List<String> getPartialCombinations();
		
		
	}
	
	private static class FixedPart extends Part {
		
		private StringBuilder s = new StringBuilder();

		@Override
		public void append(char c) {
			s.append(c);
		}

		@Override
		public List<String> getPartialCombinations() {
			return Lists.newArrayList(s.toString());
		}

		
	}
	
	private static class MultiPart extends Part {
		
		private List<Part> children = Lists.newArrayList();
		private Part lastChildren;

		@Override
		public void append(char c) {
			if (lastChildren == null) {
				children.add(lastChildren = new FixedPart());
			}
			
			lastChildren.append(c);
		}
		
		public void newPart() {
			lastChildren = null;
		}

		@Override
		public List<String> getPartialCombinations() {
			List<String> partialCombinations = Lists.newArrayList();
			for (Part child : children) {
				partialCombinations.addAll(child.getPartialCombinations());
			}
			return partialCombinations;
		}
		
	}

}

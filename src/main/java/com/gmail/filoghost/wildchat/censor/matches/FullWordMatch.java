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
package com.gmail.filoghost.wildchat.censor.matches;

import com.gmail.filoghost.wildchat.listener.ChatListener;

public class FullWordMatch extends Match {
	
	private String[] words;
	
	public FullWordMatch(String configString) {
		super(configString);
		this.words = ChatListener.NON_WORDS_PATTERN.split(configString);
	}

	@Override
	public boolean matches(String message, String[] singleWords) {
		if (words.length == 1) {
			// Match semplice
			for (String messageWord : singleWords) {
				if (messageWord.equals(words[0])) {
					return true;
				}
			}
		} else {
			outerLoop: for (int i = 0; i < singleWords.length; i++) {
				for (int j = 0; j < words.length; j++) {
					if (i + j < singleWords.length && singleWords[i + j].equals(words[j])) {
						// Ok
					} else {
						// Almeno una parola non corrisponde o manca, vai avanti con il loop principale
						continue outerLoop;
					}
				}
				
				// Se siamo arrivati qui, c'è stato un match di tutte le parole
				return true;
				
			}
		}
		
		return false;
	}

	@Override
	public String getTypeName() {
		return "word";
	}

}

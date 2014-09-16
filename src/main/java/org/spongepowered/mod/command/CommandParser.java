/** This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.spongepowered.mod.command;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.api.command.CommandLine;
import org.spongepowered.api.command.InteractiveCommandParser;

public class CommandParser implements InteractiveCommandParser<CommandLine> {

	private CommandLine _cmdLine;
	private Lexer<CommandParser> _lexer;
	
	protected class Lexer<T extends CommandParser> implements InteractiveLexicalAnalyzer, Iterator<ParsedUnit> {

		T _parser;
		List<ParsedUnit> _tokens;
		
		@SuppressWarnings("unchecked")
		protected Lexer(CommandParser parser)
		{
			if (parser instanceof CommandParser) {
				_parser = (T) parser;

			}
		}
		

		@Override
		public <T extends CommandLine> Boolean scan(T rawCommand) {
			// TODO Auto-generated method stub
			return null;
		}

		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public ParsedUnit next() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}


	}

	
	
	public CommandParser(CommandLine cmd) {
		_cmdLine = cmd;
		_lexer = new Lexer<CommandParser>(this);
		
	}
	
	@SuppressWarnings("unused")
	private CommandParser() {
		// no c'tor without a CommandLine
	}
	
	

	@Override
	public Boolean parse() {

		if (_cmdLine == null) {
			return false;
		}
		
		Boolean bScanResult = _lexer.scan(_cmdLine);
		if (bScanResult == false)
			return bScanResult;
		
		while (_lexer.hasNext()) {
			
			ParsedUnit punit =  _lexer.next();
			System.out.println(punit);
			
		}
		
		
		return null;
	}

}

package mud.commands;

import mud.Command;
import mud.misc.ProgramInterpreter;
import mud.misc.Script;
import mud.net.Client;
import mud.objects.Player;

/*
 * Copyright (c) 2015 Jeremy N. Harton
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class ScriptedCommand extends Command {
	private int accessLevel = 0;
	
	private ProgramInterpreter pgmi;
	private Script script;
	
	public ScriptedCommand(final String description, final ProgramInterpreter pgmi, final Script script) {
		super(description);
		
		this.pgmi = pgmi;
		this.script = script;
	}

	public void execute(final String arg, final Client client) {
		String result = "";
		
		// make sure we have exclusive control of the interpreter?
		synchronized(this.pgmi) {
			System.out.println("Argument: " + arg);
			
			this.pgmi.addVar("arg", arg);
			
			System.out.println("PGMI('arg'): " + this.pgmi.getVar("arg"));
			
			final Player player = getPlayer(client);
			
			// invoke the interpreter, providing the player, object context to execute within
			result = this.pgmi.interpret(script, player, player);

			
			this.pgmi.delVar("arg");
		}

		send(result, client);
	}
	
	public void setAccessLevel(int newAccessLevel) {
		this.accessLevel = newAccessLevel;
	}

	public int getAccessLevel() {
		return accessLevel;
	}
}
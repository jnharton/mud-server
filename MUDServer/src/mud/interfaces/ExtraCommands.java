package mud.interfaces;

import java.util.Map;

import mud.commands.Command;

public interface ExtraCommands {
	public abstract Map<String, Command> getCommands();
}
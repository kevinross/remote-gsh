package safrain.remotegsh.shell.commands;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.Ansi;

public class HelpCommand implements GshCommand {
	private List<GshCommand> commands = new ArrayList<GshCommand>();

	// Available commands:
	// help (\h ) Display this help message
	// ? (\? ) Alias to: help
	// exit (\x ) Exit the shell
	// quit (\q ) Alias to: exit
	// import (\i ) Import a class into the namespace
	// display (\d ) Display the current buffer
	// clear (\c ) Clear the buffer and reset the prompt counter.
	// show (\S ) Show variables, classes or imports
	// inspect (\n ) Inspect a variable or the last result with the GUI object
	// browser
	// purge (\p ) Purge variables, classes, imports or preferences
	// edit (\e ) Edit the current buffer
	// load (\l ) Load a file or URL into the buffer
	// . (\. ) Alias to: load
	// save (\s ) Save the current buffer to a file
	// record (\r ) Record the current session to a file
	// history (\H ) Display, manage and recall edit-line history
	// alias (\a ) Create an alias
	// set (\= ) Set (or list) preferences
	// register (\rc) Registers a new command with the shell
	//
	// For help on a specific command type:
	// help command
	//
	public HelpCommand(List<GshCommand> commands) {
		this.commands = commands;
	}

	@Override
	public boolean accept(String cmd) {
		return "help".equals(cmd) || "?".equals(cmd);
	}

	@Override
	public void execute(String[] args) {
		if (args.length > 0) {
			for (GshCommand command : commands) {
				if (command.accept(args[0])) {
					System.out.println(command.getDetailInfo());
				}
			}
		} else {
			System.out.println("Available commands:");
			for (GshCommand command : commands) {
				System.out.println("    " + command.getCommandTitle() + "\t\t" + command.getBriefInfo() + Ansi.ansi().newline().toString());
			}
		}
	}

	public List<GshCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<GshCommand> commands) {
		this.commands = commands;
	}

	@Override
	public String getCommandTitle() {
		return "help";
	}

	@Override
	public String getBriefInfo() {
		return "Display this help message";
	}

	@Override
	public String getDetailInfo() {
		return "\nDisplay the list of commands or the help text for command.\n\nusage: help [<command>]\n";
	}

}

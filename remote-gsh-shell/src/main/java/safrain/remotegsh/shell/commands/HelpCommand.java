package safrain.remotegsh.shell.commands;

import java.util.ArrayList;
import java.util.List;

import safrain.remotegsh.shell.AnsiUtil;

public class HelpCommand implements GshCommand {
	private List<GshCommand> commands = new ArrayList<GshCommand>();

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
					System.out.println();
					System.out.println(command.getUsage());
					System.out.println(command.getDetailInfo());
					System.out.println();
				}
			}
		} else {
			System.out.println();
			System.out.println("Available commands:");
			for (GshCommand command : commands) {
				System.out.println("\t" + AnsiUtil.bold(command.getCommandTitle()) + "\t\t" + command.getBriefInfo());
			}
			System.out.println();
			System.out.println("For help on a specific command type:");
			System.out.println("\t" + AnsiUtil.bold("help") + " command");
			System.out.println();
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
		return "Display the list of commands or the help text for command.";
	}

	@Override
	public String getUsage() {
		return "usage: " + AnsiUtil.bold("help") + " [<command>]";
	}
}

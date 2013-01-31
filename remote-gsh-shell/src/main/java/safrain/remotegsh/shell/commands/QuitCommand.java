package safrain.remotegsh.shell.commands;

import safrain.remotegsh.shell.AnsiUtil;

/**
 * Quit shell
 * 
 * @author safrain
 */
public class QuitCommand implements GshCommand {

	@Override
	public boolean accept(String cmd) {
		return "quit".equals(cmd) || "exit".equals(cmd);
	}

	@Override
	public void execute(String[] args) {
		System.out.println("Bye~");
		System.exit(0);
	}

	@Override
	public String getCommandTitle() {
		return "exit";
	}

	@Override
	public String getBriefInfo() {
		return "Exit the shell";
	}

	@Override
	public String getDetailInfo() {
		return "Exit the shell.";
	}

	@Override
	public String getUsage() {
		return "usage: " + AnsiUtil.bold("exit");
	}

}

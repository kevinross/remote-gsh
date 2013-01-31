package safrain.remotegsh.shell.commands;

import java.io.IOException;

import safrain.remotegsh.shell.AnsiUtil;
import safrain.remotegsh.shell.GshShell;

/**
 * Show or set server adderss
 * 
 * @author safrain
 */
public class ServerCommand implements GshCommand {

	private GshShell shell;

	public ServerCommand(GshShell shell) {
		this.shell = shell;
	}

	@Override
	public boolean accept(String cmd) {
		return "server".equals(cmd);
	}

	@Override
	public void execute(String[] args) {
		if (args.length == 0) {// no arg,show current server
			System.out.println();
			System.out.println("Current server url: " + shell.getConfig().getServer());
			System.out.println();
		} else if (args.length == 1) {// set server
			String host = args[0];
			shell.getConfig().setServer(host);
			try {
				shell.getConfig().save(shell.getConfigFile());
				System.out.println();
				System.out.println("Current server url has been set to '" + shell.getConfig().getServer() + "'");
				System.out.println();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else if (args.length == 2) {

		}
	}

	@Override
	public String getCommandTitle() {
		return "server";
	}

	@Override
	public String getBriefInfo() {
		return "Set server service url.";
	}

	@Override
	public String getDetailInfo() {
		return "Show or set ";
	}

	@Override
	public String getUsage() {
		return AnsiUtil.bold("server") + " [<server url>]";
	}

}

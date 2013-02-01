package safrain.remotegsh.shell;

import java.util.regex.Pattern;

public enum GshCommand {
	HELP("help|\\?"), //
	EXIT("quit|exit"), //
	RUN("run"), //
	SERVER("server");

	private Pattern pattern;

	private GshCommand(String p) {
		pattern = Pattern.compile(p);
	}

	public boolean accept(String command) {
		return pattern.matcher(command).matches();
	}

	public static GshCommand getCommand(String command) {
		for (GshCommand c : GshCommand.values()) {
			if (c.accept(command)) {
				return c;
			}
		}
		return null;
	}

}

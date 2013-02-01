package safrain.remotegsh.shell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;

import org.apache.commons.io.IOUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderWriter;
import org.fusesource.jansi.AnsiRenderer;

/**
 * @author safrain
 */
public class GshShell {
	private GshConfig config = new GshConfig();

	private static final File defaultConfigFile = new File("gsh.properties");

	private File configFile = defaultConfigFile;

	private ConsoleReader consoleReader;

	private PrintWriter out = new AnsiRenderWriter(System.out, true);

	public GshShell() {
		TerminalFactory.configure(TerminalFactory.Type.AUTO);
		AnsiConsole.systemInstall();
		Ansi.setDetector(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return TerminalFactory.get().isAnsiSupported();
			}
		});
		try {
			consoleReader = new ConsoleReader();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		StringsCompleter allCommands = new StringsCompleter("help", "quit", "exit", "run", "server");
		consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("help"), allCommands));
		consoleReader.addCompleter(allCommands);
		consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("run"), new FileNameCompleter()));
	}

	public void start() throws IOException {
		config.load(configFile);
		out.println(getResourceString("welcome.txt"));
		String input = null;
		while (true) {
			input = consoleReader.readLine(AnsiRenderer.render(String.format("@|bold rgsh@%s>|@ ", config.getServer()))).trim();
			String cmd = parseCommand(input);
			if (cmd != null && !cmd.isEmpty()) {
				GshCommand c = GshCommand.getCommand(cmd);
				if (c != null) {
					switch (c) {
					case HELP:
						cmdHelp(parseArgs(input));
						break;
					case EXIT:
						cmdExit();
						break;
					case RUN:
						cmdRun(parseArgs(input));
						break;
					case SERVER:
						cmdServer(parseArgs(input));
						break;
					default:
						break;
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void cmdHelp(String[] args) {
		if (args.length == 1) {
			GshCommand c = GshCommand.getCommand(args[0]);
			if (c != null) {
				switch (c) {
				case HELP:
					out.println(getResourceString("help/help.txt"));
					break;
				case EXIT:
					out.println(getResourceString("help/exit.txt"));
					break;
				case RUN:
					out.println(getResourceString("help/run.txt"));
					break;
				case SERVER:
					out.println(getResourceString("help/server.txt"));
					break;
				default:
					break;
				}
			}
		} else {
			out.println(getResourceString("help.txt"));
		}
	}

	private void cmdServer(String[] args) {
		if (args.length == 1) {
			getConfig().setServer(args[0]);
			try {
				getConfig().save(getConfigFile());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		out.println(String.format("Current server url is '%s'", getConfig().getServer()));
	}

	private void cmdRun(String[] arg) {
	}

	private void cmdExit() {
		out.println("Bye~");
		System.exit(0);
	}

	private String getResourceString(String name) {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("safrain/remotegsh/shell/" + name);
			if (is == null) {
				return null;
			}
			return IOUtils.toString(is, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}

	private String parseCommand(String input) {
		StringTokenizer t = new StringTokenizer(input.trim(), " ");
		if (!t.hasMoreElements())
			return null;
		return t.nextToken();
	}

	private String[] parseArgs(String input) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(input.trim(), " ");
		while (t.hasMoreElements()) {
			String w = t.nextToken();
			tokens.add(w);
		}
		if (tokens.size() > 1) {
			return (String[]) tokens.subList(1, tokens.size()).toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public GshConfig getConfig() {
		return config;
	}

	public void setConfig(GshConfig config) {
		this.config = config;
	}

}

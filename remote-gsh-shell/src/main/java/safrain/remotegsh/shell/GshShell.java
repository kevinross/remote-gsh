package safrain.remotegsh.shell;

import static org.fusesource.jansi.Ansi.ansi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.NullCompleter;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import safrain.remotegsh.shell.commands.GshCommand;
import safrain.remotegsh.shell.commands.HelpCommand;
import safrain.remotegsh.shell.commands.ServerCommand;
import safrain.remotegsh.shell.commands.QuitCommand;

/**
 * @author safrain
 */
public class GshShell {
	/**
	 * 
	 */

	private GshConfig config = new GshConfig();

	private static final File defaultConfigFile = new File("gsh.properties");

	private File configFile = defaultConfigFile;
	private List<GshCommand> commands = new ArrayList<GshCommand>();

	private ConsoleReader consoleReader;

	public GshShell() {
		TerminalFactory.configure(TerminalFactory.Type.AUTO);
		AnsiConsole.systemInstall();
		Ansi.setDetector(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return TerminalFactory.get().isAnsiSupported();
			}
		});

	}

	public void init() throws IOException {
		consoleReader = new ConsoleReader();
		List<Completer> completors = new ArrayList<Completer>();
		completors.add(new FileNameCompleter());
		completors.add(new NullCompleter());
		consoleReader.addCompleter(new ArgumentCompleter(completors));
		commands.add(new QuitCommand());
		commands.add(new HelpCommand(commands));
		commands.add(new ServerCommand(this));
	}

	public void start() throws IOException {
		System.out.println("@|bold >|@");
		System.out.println("Remote Groovy Shell");
		System.out.println("Type '" + AnsiUtil.bold("help") + "' for help.");
		config.load(configFile);
		String input = null;
		while (true) {
			input = consoleReader.readLine(ansi().bold().a("rgsh@").a(config.getServer()).a(">").reset().toString()).trim();
			for (GshCommand command : commands) {
				String cmd = parseCommand(input);
				if (cmd != null && !cmd.isEmpty()) {
					if (command.accept(cmd)) {
						String[] args = parseArgs(input);
						command.execute(args);
						break;
					}
				}
			}
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

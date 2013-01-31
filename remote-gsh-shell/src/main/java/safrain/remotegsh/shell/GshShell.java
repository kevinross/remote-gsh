package safrain.remotegsh.shell;

import static org.fusesource.jansi.Ansi.ansi;

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
import safrain.remotegsh.shell.commands.QuitCommand;

/**
 * @author safrain
 */
public class GshShell {
	/**
	 * 
	 */
	private String host;

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
	}

	public void start() throws IOException {
		String input = null;
		while (true) {
			input = consoleReader.readLine(ansi().bold().a("remote-gsh>").reset().toString());
			for (GshCommand command : commands) {
				List<String> inputList = parseInput(input);
				String cmd = inputList.get(0);
				String[] args;
				if (inputList.size() > 1) {
					args = (String[]) inputList.subList(1, inputList.size()).toArray(new String[0]);
				} else {
					args = new String[0];
				}
				if (command.accept(cmd)) {
					command.execute(args);

					if (command.getClass() == QuitCommand.class) {// exit
						return;
					}
					break;
				}
			}
		}
	}

	private List<String> parseInput(String input) {
		List<String> args = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(input.trim(), " ");
		while (t.hasMoreElements()) {
			String w = t.nextToken();
			args.add(w);
		}
		return args;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

}

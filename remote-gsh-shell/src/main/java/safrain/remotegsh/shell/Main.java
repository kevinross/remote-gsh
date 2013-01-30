package safrain.remotegsh.shell;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.NullCompleter;

import org.fusesource.jansi.AnsiConsole;

/**
 * Entry of remote groovy shell client.
 * 
 * @author safrain
 */
public class Main {
	public static void main(String[] args) throws IOException {
		AnsiConsole.systemInstall();
		TerminalFactory.configure("unix");
		ConsoleReader reader = new ConsoleReader();
		List<Completer> completors = new ArrayList<Completer>();
		completors.add(new FileNameCompleter());
		completors.add(new NullCompleter());
		reader.addCompleter(new ArgumentCompleter(completors));
		reader.readLine(ansi().fg(RED).a("HELLO").reset().toString());
	}
}

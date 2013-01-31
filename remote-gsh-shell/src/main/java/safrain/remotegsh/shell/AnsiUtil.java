package safrain.remotegsh.shell;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

public class AnsiUtil {

	public static String bold(String text) {
		return Ansi.ansi().bold().a(text).reset().toString();
	}

	public static String green(String text) {
		return Ansi.ansi().fg(Color.GREEN).a(text).reset().toString();
	}
}

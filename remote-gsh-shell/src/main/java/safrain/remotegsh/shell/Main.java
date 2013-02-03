package safrain.remotegsh.shell;

import java.io.File;
import java.io.IOException;

/**
 * Entry of remote groovy shell client.
 * 
 * @author safrain
 */
public class Main {
	public static void main(String[] args) throws IOException {
		GshShell shell = new GshShell();
		if (args.length > 0) {
			shell.setConfigFile(new File(args[0]));
		}
		shell.init();
		shell.start();
	}
}

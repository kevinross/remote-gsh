package safrain.remotegsh.shell;

import java.io.IOException;

/**
 * Entry of remote groovy shell client.
 * 
 * @author safrain
 */
public class Main {
	public static void main(String[] args) throws IOException {
		/*
		 * Get server and charset settings from system properties passed with -D
		 * switch
		 */
		String server = System.getProperty("server");
		String fileCharset = System.getProperty("file-charset");
		String requestCharset = System.getProperty("request-charset");
		GshShell shell = new GshShell();
		if (server != null) {
			shell.setServer(server);
		}
		if (fileCharset != null) {
			shell.setFileCharset(fileCharset);
		}
		if (requestCharset != null) {
			shell.setRequestCharset(requestCharset);
		}

		shell.initTerminal();
		/*
		 * Decide what to do with command line arguments
		 */
		if (args.length == 2 && args[0].equals("run")) {
			// Post a script file to server
			String filename = args[1];
			shell.cmdRun(new String[] { filename });
		} else {
			// Otherwise start shell
			if (!shell.connect()) {
				System.exit(0);
			}
			shell.start();
		}
	}
}

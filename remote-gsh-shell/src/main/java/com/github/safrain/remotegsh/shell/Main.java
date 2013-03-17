package com.github.safrain.remotegsh.shell;

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
		String charset = System.getProperty("charset");
		GshShell shell = new GshShell();
		if (server != null) {
			shell.setServer(server);
		}
		if (charset != null) {
			shell.setCharset(charset);
		}

		shell.initTerminal();
		if (!shell.connect()) {
			System.exit(0);
		}
		shell.start();
	}
}

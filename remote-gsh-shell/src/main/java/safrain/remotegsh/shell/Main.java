package safrain.remotegsh.shell;

import java.io.IOException;

/**
 * Entry of remote groovy shell client.
 * 
 * @author safrain
 */
public class Main {
	public static void main(String[] args) throws IOException {
		GshShell shell = new GshShell();
		shell.init();
		shell.start();
	}
}

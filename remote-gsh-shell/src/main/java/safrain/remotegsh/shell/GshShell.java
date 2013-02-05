package safrain.remotegsh.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderWriter;
import org.fusesource.jansi.AnsiRenderer;

/**
 * Remote Groovy Shell client
 * 
 * @author safrain
 */
public class GshShell {
	private GshConfig config = new GshConfig();

	private HttpClient client = new HttpClient();
	private static final File defaultConfigFile = new File("gsh.properties");

	private File configFile = defaultConfigFile;

	private ConsoleReader consoleReader;

	private PrintWriter out = new AnsiRenderWriter(System.out, true);

	private String sid;

	public void init() throws IOException {
		config.load(configFile);
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
		println(getResourceString("welcome.txt"));
		cmdHelp(new String[] {});
		connect();
		String input = null;
		while (true) {
			input = consoleReader.readLine(AnsiRenderer.render(String.format("@|bold rgsh@%s>|@ ", config.getServer()))).trim();
			if (input.isEmpty()) {
				continue;
			}

			String cmd = parseCommand(input);
			if (cmd == null || cmd.isEmpty()) {
				continue;
			}
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
					ensureConnection();
					cmdRun(parseArgs(input));
					break;
				case SERVER:
					cmdServer(parseArgs(input));
					break;
				case CHARSET:
					cmdCharset(parseArgs(input));
					break;
				}
			} else {
				ensureConnection();
				shellExecute(input);
			}
		}
	}

	// ==========Connection==========
	private boolean ensureConnection() {
		if (sid == null) {
			connect();
		}
		return sid != null;// in case of connection error
	}

	private void connect() {
		ServerResponse response = null;
		try {
			response = httpGet(getConfig().getServer() + "?r=shell");
		} catch (ConnectionException e) {
			reportConnectionError(e);
			return;
		}

		if (response.statusCode == 200) {
			// TODO:get auto complete hints
			sid = response.responseString;
			println("@|green CONNECTED|@: Connected to server, SessionId = @|blue %s|@.", sid);
		} else {
			sid = null;
			reportError("Could not start shell session(@|red %s|@).", response.statusCode);
			println(response.responseString);
			return;
		}
	}

	// ==========Shell input parsing==========
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

	// ==========Shell commands==========
	/**
	 * Execute groovy script from shell input on server
	 */
	private void shellExecute(String input) {
		ServerResponse response;
		try {
			response = post(getConfig().getServer() + "?sid=" + sid, input);
		} catch (ConnectionException e) {
			reportConnectionError(e);
			return;
		}
		dealExecuteResponse(response);
	}

	/**
	 * Upload a groovy script file and execute on server
	 */
	private void cmdRun(String[] args) {
		if (args.length != 1) {
			reportError("Must specify script file name.");
			return;
		}
		String filename = args[0];
		try {
			FileInputStream fis = new FileInputStream(filename);
			String content = IOUtils.toString(fis, getConfig().getCharset());
			ServerResponse response = post(getConfig().getServer(), content);
			dealExecuteResponse(response);
		} catch (FileNotFoundException e) {
			println("File '%s' not found.", filename);
		} catch (IOException e) {
			reportError(e);
		} catch (ConnectionException e) {
			sid = null;
			reportConnectionError(e);
		}
	}

	/**
	 * Response handler for {@link #shellExecute(String)} and
	 * {@link #cmdRun(String[])}
	 */
	private void dealExecuteResponse(ServerResponse response) {
		if (response.statusCode == 200) {
			println(response.responseString);
		} else if (response.statusCode == 500) {
			sid = null;
			reportError("Server exception while executing script");
			println("@|red Stack Trace:|@");
			println(response.responseString);
		} else if (response.statusCode == 410) {
			sid = null;
			reportError("Shell session timeout(@|red %s|@).", response.statusCode);
		} else {
			sid = null;
			reportError("Unexpected server error(@|red %s|@).", response.statusCode);
			println(response.responseString);
		}
	}

	/**
	 * Show help info
	 */
	private void cmdHelp(String[] args) {
		if (args.length == 1) {
			GshCommand c = GshCommand.getCommand(args[0]);
			if (c != null) {
				switch (c) {
				case HELP:
					println(getResourceString("help/help.txt"));
					break;
				case EXIT:
					println(getResourceString("help/exit.txt"));
					break;
				case RUN:
					println(getResourceString("help/run.txt"));
					break;
				case SERVER:
					println(getResourceString("help/server.txt"));
					break;
				case CHARSET:
					println(getResourceString("help/charset.txt"));
					break;
				default:
					break;
				}
			}
		} else {
			println(getResourceString("help.txt"));
		}
	}

	/**
	 * Show or modify server address
	 */
	private void cmdServer(String[] args) {
		if (args.length == 1) {
			getConfig().setServer(args[0]);
			try {
				getConfig().save(getConfigFile());
			} catch (IOException e) {
				reportError(e);
				return;
			}
		}
		println("Current server url is '%s'", getConfig().getServer());
	}

	/**
	 * Show or modify charset
	 */
	private void cmdCharset(String[] args) {
		if (args.length == 1) {
			try {
				Charset.forName(args[0]);
			} catch (Exception e) {
				reportError("Illegal charset '%s'.", args[0]);
				return;
			}
			getConfig().setCharset(args[0]);
			try {
				getConfig().save(getConfigFile());
			} catch (IOException e) {
				reportError(e);
				return;
			}
		}
		println("Current charset is '%s'", getConfig().getCharset());
	}

	/**
	 * Just exit the shell
	 */
	private void cmdExit() {
		println("Bye~");
		System.exit(0);
	}

	// ==========Utilities==========
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

	// ==========Http utilities==========
	public ServerResponse httpGet(String uri) throws ConnectionException {
		GetMethod get = new GetMethod(uri);
		ServerResponse r = new ServerResponse();
		try {
			int statusCode = client.executeMethod(get);
			String responseString = new String(get.getResponseBody(), getConfig().getCharset());
			r.statusCode = statusCode;
			r.responseString = responseString;
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
		return r;
	}

	public ServerResponse post(String uri, String content) throws ConnectionException {
		ServerResponse r = new ServerResponse();
		try {
			PostMethod post = new PostMethod(uri);
			post.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			post.setRequestEntity(new StringRequestEntity(content, "text", getConfig().getCharset()));
			int statusCode = client.executeMethod(post);
			String responseString = new String(post.getResponseBody(), getConfig().getCharset());
			r.statusCode = statusCode;
			r.responseString = responseString;
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
		return r;
	}

	// ==========Console output utilities==========
	private void reportConnectionError(ConnectionException e) {
		reportError("Failed to connect to '%s'", getConfig().getServer());
		e.getCause().printStackTrace(out);
	}

	private void println(String format, Object... args) {
		out.println(String.format(format, args));
	}

	private void reportError(Exception e) {
		reportError("Uncaught exception:");
		e.printStackTrace(out);
	}

	private void reportError(String format, Object... args) {
		println("@|red ERROR:|@ " + format, args);
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

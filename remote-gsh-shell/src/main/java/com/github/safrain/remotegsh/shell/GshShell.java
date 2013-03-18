package com.github.safrain.remotegsh.shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
	private HttpClient client = new HttpClient();

	private ConsoleReader consoleReader;

	private PrintWriter out = new AnsiRenderWriter(System.out, true);

	private String sid;

	// ==========Configs==========
	/**
	 * Server address, with 'http://'
	 */
	public static final String DEFAULT_CHARSET = "utf-8";
	public static final String DEFAULT_SERVER = "http://localhost/admin/rgsh";
	private String server = DEFAULT_SERVER;
	/**
	 * Request & response charset
	 */
	private String charset = DEFAULT_CHARSET;

	public void initTerminal() throws IOException {
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
		StringsCompleter allCommands = new StringsCompleter("help", "quit", "exit", "run");
		consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("help"), allCommands));
		consoleReader.addCompleter(allCommands);
		consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("run"), new FileNameCompleter()));
	}

	public void start() throws IOException {
		println(getResourceString("welcome.txt"));
		println("@|yellow Server|@: %s", server);
		println("@|yellow Request charset|@: %s", charset);
		cmdHelp(new String[] {});
		ensureConnection();
		String input;
		while (true) {
			input = consoleReader.readLine(AnsiRenderer.render(String.format("@|bold rgsh@%s>|@ ", server))).trim();
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
				}
			} else {
				ensureConnection();
				shellExecute(input);
			}
		}
	}

	// ==========Connection==========
	private void ensureConnection() {
		if (sid == null) {
			connect();
		}
		// return sid != null;// in case of connection error
	}

	public boolean connect() {
		ServerResponse response;
		try {
			response = httpGet(server + "?r=shell");
		} catch (ConnectionException e) {
			reportConnectionError(e);
			return false;
		}

		if (response.statusCode == 200) {
			// TODO:get auto complete hints
			sid = response.responseString;
			println("@|green CONNECTED|@: Connected to @|blue %s|@, SessionId = @|blue %s|@.", server, sid);
			return true;
		} else {
			sid = null;
			reportError("Could not start shell session(@|red %s|@).", response.statusCode);
			println(response.responseString);
			return false;
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
			return tokens.subList(1, tokens.size()).toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	// ==========Shell commands==========
	/**
	 * Execute groovy script from shell input on server
	 */
	public void shellExecute(String input) {
		ServerResponse response;
		try {
			response = httpPost(server + "?sid=" + sid, input);
		} catch (ConnectionException e) {
			reportConnectionError(e);
			return;
		}
		Properties p = new Properties();
		if (response.responseString != null) {
			try {
				p.load(new StringReader(response.responseString));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		String result = p.getProperty("result");
		String error = p.getProperty("error");
		String r = p.getProperty("response");


		if (response.statusCode == 200) {
			println(String.format("@|bold ===>%s|@",result));
			if (r!=null){
				println(r);
			}
		} else if (response.statusCode == 500) {
			if (r!=null){
				println(r);
			}
			reportError("Server exception(@|red %s|@).",response.statusCode);
			if(error!=null){
				println("@|red Stack Trace:|@");
				println(error);
			}
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
	public void cmdHelp(String[] args) {
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
				}
			}
		} else {
			println(getResourceString("help.txt"));
		}
	}

	/**
	 * Just exit the shell
	 */
	public void cmdExit() {
		println("Bye~");
		System.exit(0);
	}

	// ==========Resource Utilities==========
	private String getResourceString(String name) {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("com/github/safrain/remotegsh/shell/" + name);
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
			String responseString = new String(get.getResponseBody(), charset);
			r.statusCode = statusCode;
			r.responseString = responseString;
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
		return r;
	}

	public ServerResponse httpPost(String uri, String content) throws ConnectionException {
		ServerResponse r = new ServerResponse();
		try {
			PostMethod post = new PostMethod(uri);
			post.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			post.setRequestEntity(new StringRequestEntity(content, "text", charset));
			int statusCode = client.executeMethod(post);
			String responseString = new String(post.getResponseBody(), charset);
			r.statusCode = statusCode;
			r.responseString = responseString;
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
		return r;
	}

	// ==========Console output utilities==========
	private void reportConnectionError(ConnectionException e) {
		reportError("Failed to connect to '%s'", server);
		e.getCause().printStackTrace(out);
	}

	private void println(String format, Object... args) {
		out.println(String.format(format, args));
	}

	private void reportError(String format, Object... args) {
		println("@|red ERROR:|@ " + format, args);
	}

	// ==========Getter/Setter==========
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}

package safrain.remotegsh.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RgshFilter implements Filter {
	private static final Logger log = Logger.getLogger(RgshFilter.class.getName());
	private String charset = "utf-8";
	public static final String RESOURCE_PATH = "/com/taobao/geek/gan/";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		charset = filterConfig.getInitParameter("charset");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest r = (HttpServletRequest) request;
		String action = r.getParameter("action");

		if ("GET".equals(r.getMethod())) {
			if (action == null || action.isEmpty()) {
				// print
			} else if ("install".equals(action)) {
				// download client
			} else if ("shell".equals(action)) {
				// shell token
			}
		} else if ("POST".equals(r.getMethod())) {
			if (action == null || action.isEmpty()) {
				// execute
			} else if ("shell".equals(action)) {
				// shell execute
			}
		}

	}

	public static Object run(ScriptEngine engine, String script, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter writer = resp.getWriter();
		engine.getContext().setWriter(writer);
		engine.getContext().setErrorWriter(writer);
		engine.put("_request", req);
		engine.put("_response", resp);
		try {
			return engine.eval(script);
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Error while running script:" + script, e);
			e.printStackTrace(writer);
			return null;
		}
	}

	public ScriptEngine createEngine(HttpServletRequest req, HttpServletResponse resp, String charset) throws IOException, ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
		if (engine == null) {
			throw new IllegalArgumentException("Groovy engine not found.");
		}
		engine.put("_charset", charset);
		engine.put("_engine", engine);
		engine.put("_request", req);
		engine.put("_response", resp);
		engine.eval(getResource("init.groovy"));
		return engine;
	}

	@Override
	public void destroy() {
	}

	private String getResource(String name) throws IOException {
		return toString(getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH + name));
	}

	private String toString(InputStream inputStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		StringWriter sw = new StringWriter();
		char[] buffer = new char[4096];
		int readed;
		while ((readed = reader.read(buffer)) == -1) {
			sw.write(buffer, 0, readed);
		}
		return sw.toString();
	}
}

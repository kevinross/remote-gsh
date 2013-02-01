package safrain.remotegsh.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * GAN Servlet<br>
 * Access spring context and do something CRUEL with Groovy at runtime<br>
 * Usage: curl -s -T {Groovy File} -X POST {Hostname}
 * 
 * @author safrain
 * 
 */
public class RgshServlet extends GenericServlet {
	private static final long serialVersionUID = -6733689412328003957L;
	private static final Logger log = Logger.getLogger(RgshServlet.class.getName());
	private String charset = "utf-8";
	public static final String RESOURCE_PATH = "/com/taobao/geek/gan/";

	@Override
	public void init() throws ServletException {
		charset = getInitParameter("charset");
	}

	public static String resolvePath(HttpServletRequest req) {
		String path = req.getPathInfo();
		if (path == null) {
			path = "";
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.isEmpty()) {
			path = "_root";
		}
		return path;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		try {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			req.setCharacterEncoding(charset);
			res.setCharacterEncoding(charset);
			String path = resolvePath(request);
			String resource;
			while ((resource = getResource(path + "." + request.getMethod().toLowerCase() + ".groovy")) == null) {
				if (path.indexOf('/') == -1) {
					break;
				}
				path = path.substring(0, path.lastIndexOf('/'));
			}
			if (resource == null) {
				throw new IllegalArgumentException();
			}
			run(createEngine(request, response, charset), resource, request, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static String getResource(String name) throws IOException {
		InputStream is = RgshServlet.class.getResourceAsStream(RESOURCE_PATH + name);
		if (is == null) {
			return null;
		}
		return IOUtils.toString(is);
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

	public static ScriptEngine createEngine(HttpServletRequest req, HttpServletResponse resp, String charset) throws IOException,
			ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
		if (engine == null) {
			throw new IllegalArgumentException("Groovy engine not found.");
		}
		String initScript = getResource("_init.groovy");
		engine.put("_charset", charset);
		engine.put("_engine", engine);
		engine.put("_request", req);
		engine.put("_response", resp);
		engine.eval(initScript);
		return engine;
	}

}

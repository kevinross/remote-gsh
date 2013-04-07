/*
 * Remote Groovy Shell    A servlet web application management tool
 * Copyright (c)          2013 Safrain <z.safrain@gmail.com>
 *                        All Rights Reserved
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://www.wtfpl.net/ for more details.
 */

package com.github.safrain.remotegsh.server;

import org.json.JSONException;
import org.json.JSONObject;

import javax.script.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remote Groovy Shell server side <br>
 * <h2>Supported entries</h2>
 * Assume that requests with URI '/admin/rgsh' will be handled with this filter:
 * <ul>
 * <li><b>GET /admin/rgsh</b> Show welcome screen</li>
 * <li><b>GET /admin/rgsh?r=install</b> Download shell client install script</li>
 * <li><b>GET /admin/rgsh?r=shell</b> Request to start a shell session,session id will be returned</li>
 * <li><b>GET /admin/rgsh?r=rgsh</b> Download bootstrap script</li>
 * <li><b>GET /admin/rgsh?r=jar</b> Download client jar file</li>
 * <li><b>POST /admin/rgsh</b> Run script in request body</li>
 * <li><b>POST /admin/rgsh?shell=[sid]</b> Execute shell command</li>
 * </ul>
 * <br>
 * <h2>Init parameters:</h2>
 * Init parameters configured in you web.xml
 * <ul>
 * <li><b>charset</b> Request & response charset</li>
 * <li><b>shellSessionTimeout</b> Shell sessions with idle time greater than this will be dropped, in millisecond</li>
 * <li><b>scriptExtensions</b> Script extension classpath, separated with comma</li>
 * <li><b>scriptExtensionCharset</b> Script extension charset</li>
 * </ul>
 *
 * @author safrain
 */
public class RgshFilter implements Filter {
	private static final Logger log = Logger.getLogger(RgshFilter.class.getName());
	private static final String RESOURCE_PATH = "com/github/safrain/remotegsh/server/";
	private static final String DEFAULT_CHARSET = "utf-8";
	private static final long SESSION_PURGE_INTERVAL = 1000 * 60 * 5L;// 5 min
	private static final String PLACEHOLDER_SERVER = "\\{\\{server\\}\\}";
	private static final String PLACEHOLDER_CHARSET = "\\{\\{charset\\}\\}";
	private static final String JAR_NAME = "rgsh.jar";
	/**
	 * Request & response charset
	 */
	private String charset;

	/**
	 * Shell sessions with idle time greater than this will be dropped
	 */
	private long shellSessionTimeout;

	/**
	 * Script extension classpath, some framework support or initialization work will be done in these script,
	 * these scripts will be evaluated first after a script engine is created, add spring support as default
	 */
	private Map<String, CompiledScript> scriptExtensions;

	/**
	 * Shell sessions
	 */
	private Map<String, ShellSession> shellSessions = new HashMap<String, ShellSession>();

	// ==========Filter implementation==========
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (filterConfig.getInitParameter("charset") != null) {
			charset = filterConfig.getInitParameter("charset");
		} else {
			charset = DEFAULT_CHARSET;
		}

		if (filterConfig.getInitParameter("shellSessionTimeout") != null) {
			shellSessionTimeout = Long.valueOf(filterConfig.getInitParameter("shellSessionTimeout"));
		} else {
			shellSessionTimeout = SESSION_PURGE_INTERVAL;
		}

		String scriptExtensionCharset;
		if (filterConfig.getInitParameter("scriptExtensionCharset") != null) {
			scriptExtensionCharset = filterConfig.getInitParameter("scriptExtensionCharset");
		} else {
			scriptExtensionCharset = DEFAULT_CHARSET;
		}


		//Compile script extensions
		List<String> scriptExtensionPaths = new ArrayList<String>();
		if (filterConfig.getInitParameter("scriptExtensions") != null) {
			Collections.addAll(scriptExtensionPaths, filterConfig.getInitParameter("scriptExtensions").split(","));
		} else {
			scriptExtensionPaths.add(RESOURCE_PATH + "extension/spring.groovy");
		}

		scriptExtensions = new HashMap<String, CompiledScript>();
		for (String path : scriptExtensionPaths) {
			String scriptContent;
			try {
				scriptContent = getResource(path, scriptExtensionCharset);
			} catch (IOException e) {
				throw new ServletException(e);
			}

			Compilable compilable = (Compilable) createGroovyEngine();
			try {
				CompiledScript compiledScript = compilable.compile(scriptContent);
				scriptExtensions.put(path, compiledScript);
			} catch (ScriptException e) {
				//Ignore exceptions while compiling script extensions,there may be compilation errors due to missing dependency
				log.log(Level.WARNING, String.format("Error compiling script extension '%s'", path), e);
			}
		}

		// Setup a timer to purge timeout shell sessions
		Timer timer = new Timer("Remote Groovy Shell session purge daemon", true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				purgeTimeOutSessions();
			}
		}, 0, SESSION_PURGE_INTERVAL);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		req.setCharacterEncoding(charset);
		resp.setCharacterEncoding(charset);

		if ("GET".equals(request.getMethod())) {
			String res = request.getParameter("r");
			if (res != null && !res.isEmpty()) {
				if ("install".equals(res)) {
					performInstall(request, response);
				} else if ("shell".equals(res)) {
					performStartShell(request, response);
				} else if ("jar".equals(res)) {
					performJar(response);
				} else if ("rgsh".equals(res)) {
					performRgsh(request, response);
				}

			} else {
				performWelcomeScreen(request, response);
			}
		} else if ("POST".equals(request.getMethod())) {
			String sid = request.getParameter("sid");
			if (sid != null && !sid.isEmpty()) {
				performShellExecute(request, response);
			} else {
				performRunScript(request, response);
			}
		}
	}

	@Override
	public void destroy() {
	}

	// ==========Filter entries==========
	/*
	 * Welcome Screen
	 */
	private void performWelcomeScreen(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println(
				getResource(RESOURCE_PATH + "welcome.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER,
						request.getRequestURL().toString()));
		response.setStatus(200);
	}

	/*
	 * Download client jar file
	 */
	private void performJar(HttpServletResponse response) throws IOException {
		ServletOutputStream os = response.getOutputStream();
		os.write(toBytes(RgshFilter.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + JAR_NAME)));
		response.setStatus(200);
	}

	/*
	 * Download bootstrap script
	 */
	private void performRgsh(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println(
				getResource(RESOURCE_PATH + "rgsh.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER, request.getRequestURL().toString())
						.replaceAll(PLACEHOLDER_CHARSET, charset));
		response.setStatus(200);
	}

	/*
	 * Download install script
	 */
	private void performInstall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().println(
				getResource(RESOURCE_PATH + "install.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER,
						request.getRequestURL().toString()).replaceAll(PLACEHOLDER_CHARSET, charset));
		response.setStatus(200);
	}

	/*
	 * Start new shell session
	 */
	private synchronized void performStartShell(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		ShellSession session = new ShellSession();
		session.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		session.setLastAccessTime(System.currentTimeMillis());
		session.setEngine(prepareEngine(request, response));
		shellSessions.put(session.getId(), session);
		writer.print(session.getId());
		response.setStatus(200);
	}

	/*
	 * Shell command execute
	 */
	private void performShellExecute(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ShellSession session = getSession(request.getParameter("sid"));
		if (session == null) {
			response.setStatus(410);// Http status GONE
			return;
		}
		ScriptEngine engine = session.getEngine();

		String action = request.getParameter("action");
		if (action == null) {
			StringWriter responseWriter = new StringWriter();
			engine.getContext().setWriter(responseWriter);
			engine.getContext().setErrorWriter(response.getWriter());
			String script = toString(request.getInputStream(), charset);
			JSONObject json = new JSONObject();
			try {
				try {
					Object result = engine.eval(script);
					json.put("result", String.valueOf(result));
					response.setStatus(200);
					json.put("response", responseWriter.getBuffer().toString());
				} catch (ScriptException e) {
					log.log(Level.SEVERE, "Error while running shell command:" + script, e);
					response.setStatus(500);
					e.getCause().printStackTrace(response.getWriter());
					return;
				}
			} catch (JSONException e) {
				log.log(Level.SEVERE, "Error while running shell command:" + script, e);
				response.setStatus(500);
				e.printStackTrace(response.getWriter());
				return;
			}
			response.getWriter().write(json.toString());
		} else {
			Invocable invocable = (Invocable) engine;
			try {
				invocable.invokeFunction("shellAction", action);
			} catch (ScriptException e) {
				response.setStatus(500);
				e.printStackTrace(response.getWriter());
			} catch (NoSuchMethodException e) {
				response.setStatus(500);
				response.getWriter().println("Action not supported");
			} catch (Exception e) {
				response.setStatus(500);
				e.printStackTrace(response.getWriter());
			}
		}

	}

	/*
	 * Run script
	 */
	private void performRunScript(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String script = toString(request.getInputStream(), charset);
		try {
			ScriptEngine engine = prepareEngine(request, response);
			engine.eval(script);
			response.setStatus(200);
			// Post and run won't return evaluate result to client
		} catch (ScriptException e) {
			log.log(Level.SEVERE, "Error while running script:" + script, e);
			response.setStatus(500);
			e.getCause().printStackTrace(response.getWriter());
		}
	}

	// ==========Engine creation==========

	/**
	 * Create a script engine with stdout and stderr replaced by {@link ServletResponse#getWriter()}.
	 * Script extensions in {@link #scriptExtensions} will be evaluated after the engine creation.
	 */
	private ScriptEngine prepareEngine(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ScriptEngine engine = createGroovyEngine();
		PrintWriter writer = response.getWriter();
		engine.getContext().setWriter(writer);
		engine.getContext().setErrorWriter(writer);
		engine.put("_request", request);
		engine.put("_response", response);
		engine.put("_charset", charset);

		try {
			for (Entry<String, CompiledScript> entry : scriptExtensions.entrySet()) {
				try {
					entry.getValue().eval(engine.getContext());
				} catch (ScriptException e) {
					//Ignore script extension evaluation errors
					log.log(Level.WARNING, String.format("Error evaluating script extension '%s'", entry.getKey()), e);
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while creating engine.", e);
			throw new RuntimeException(e);
		}
		return engine;
	}

	private ScriptEngine createGroovyEngine() {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
		if (engine == null) {
			log.log(Level.SEVERE, "Groovy engine not found.");
			throw new UnsupportedOperationException("Groovy engine not found.");
		}
		return engine;
	}


	// ==========Shell session management==========

	/**
	 * Get shell session by sid,and purge timeout sessions
	 */
	private ShellSession getSession(String sid) {
		purgeTimeOutSessions();
		ShellSession session = shellSessions.get(sid);
		if (session != null) {
			session.setLastAccessTime(System.currentTimeMillis());
		}
		return session;
	}

	/**
	 * Remove all timeout sessions
	 */
	private synchronized void purgeTimeOutSessions() {
		long now = System.currentTimeMillis();
		for (Iterator<Entry<String, ShellSession>> iterator = shellSessions.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<String, ShellSession> entry = iterator.next();
			if (now - entry.getValue().getLastAccessTime() > shellSessionTimeout) {
				iterator.remove();
			}
		}
	}

	// ==========Some utilities==========

	/**
	 * Get classpath resource as string
	 */
	public static String getResource(String path, String charset) throws IOException {
		return toString(RgshFilter.class.getClassLoader().getResourceAsStream(path), charset);
	}

	/**
	 * Input stream to byte array
	 */
	public static byte[] toBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int readed;
		while ((readed = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, readed);
		}
		return bos.toByteArray();
	}

	/**
	 * Input stream to string
	 */
	public static String toString(InputStream inputStream, String charset) throws IOException {
		return new String(toBytes(inputStream), charset);
	}

}

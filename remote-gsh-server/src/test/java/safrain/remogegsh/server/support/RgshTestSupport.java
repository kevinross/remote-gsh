package safrain.remogegsh.server.support;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.SessionHandler;

import safrain.remotegsh.server.RgshFilter;

public abstract class RgshTestSupport {
	protected HttpClient client = new HttpClient();
	protected String charset = "utf-8";
	protected int port = 9527;
	protected String pattern = "/admin/rgsh/*";
	protected String resroucePath = "safrain/remotegsh/server/";
	protected Server server;

	public void startServer() {
		server = new Server(port);
		Context context = new Context();
		context.setContextPath("/");
		context.setSessionHandler(new SessionHandler());
		context.addServlet(DefaultServlet.class, "/*");
		FilterHolder filterHolder = context.addFilter(RgshFilter.class, pattern, Handler.DEFAULT);
		Map<String, String> params = new HashMap<String, String>();
		filterHolder.setInitParameters(params);
		server.addHandler(context);
		server.addHandler(new RequestLogHandler());
		try {
			server.start();
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	protected void stopServer() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	public ServerResponse get(String uri) {
		GetMethod get = new GetMethod(uri);
		ServerResponse r = new ServerResponse();
		try {
			int statusCode = client.executeMethod(get);
			String responseString = new String(get.getResponseBody(), charset);
			r.statusCode = statusCode;
			r.responseString = responseString;
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
		return r;
	}

	public ServerResponse post(String uri, String content) {
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
			throw new UndeclaredThrowableException(e);
		}
		return r;
	}

	protected String getResource(String name) {
		InputStream is = RgshTestSupport.class.getClassLoader().getResourceAsStream(resroucePath + name);
		if (is == null) {
			return null;
		}
		try {
			return IOUtils.toString(is, charset);
		} catch (IOException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	public static class ServerResponse {
		public int statusCode;
		public String responseString;
	}

}

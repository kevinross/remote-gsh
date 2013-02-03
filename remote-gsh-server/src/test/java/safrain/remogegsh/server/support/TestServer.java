package safrain.remogegsh.server.support;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.SessionHandler;

import safrain.remotegsh.server.RgshFilter;

public class TestServer {
	public Server server;

	public void startServer() {
		server = new Server(9527);

		Context context = new Context();
		context.setContextPath("/");
		context.setSessionHandler(new SessionHandler());
		context.addServlet(DefaultServlet.class, "/*");
		FilterHolder filterHolder = context.addFilter(RgshFilter.class, "/admin/rgsh/*", Handler.DEFAULT);
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

	public void stopServer() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		TestServer server = new TestServer();
		server.startServer();
		server.server.join();
	}
}

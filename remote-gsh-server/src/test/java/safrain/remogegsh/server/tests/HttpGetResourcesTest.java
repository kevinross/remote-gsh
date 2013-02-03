package safrain.remogegsh.server.tests;

import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import safrain.remogegsh.server.support.RgshTestSupport;

/**
 * <p>
 * Including:
 * <li>Welcome Screen</li>
 * <li>Install client</li>
 * </p>
 */
public class HttpGetResourcesTest extends RgshTestSupport {

	@Before
	public void setup() {
		startServer();
	}

	@Test
	public void testGetHelpScreen() {
		ServerResponse r = get("http://localhost:9527/admin/rgsh");
		assertEquals(200, r.statusCode);
		assertEquals(getResource("welcome.txt").replaceAll("\\$host", "http://localhost:9527/admin/rgsh") + "\n", r.responseString);
	}

	@Test
	public void testGetStartShellSession() {
		ServerResponse r = get("http://localhost:9527/admin/rgsh?r=shell");
		assertEquals(200, r.statusCode);
		assertEquals(32, r.responseString.length());
	}

	// TODO:install client
	@After
	public void cleanup() {
		stopServer();
	}
}

package safrain.remotegsh.server.tests;

import org.junit.Test;
import safrain.remotegsh.server.support.RgshTestSupport;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * Including:
 * <li>Welcome Screen</li>
 * <li>Install client</li>
 * </p>
 */
public class HttpGetResourcesTest extends RgshTestSupport {

	@Test
	public void testGetHelpScreen() {
		ServerResponse r = get("http://localhost/admin/rgsh");
		assertEquals(200, r.statusCode);
		assertEquals(getResource("welcome.txt").replaceAll("\\{\\{server\\}\\}", "http://localhost:9527/admin/rgsh") + "\n", r.responseString);
	}

	@Test
	public void testGetStartShellSession() {
		ServerResponse r = get("http://localhost/admin/rgsh?r=shell");
		assertEquals(200, r.statusCode);
		assertEquals(32, r.responseString.length());
	}
}

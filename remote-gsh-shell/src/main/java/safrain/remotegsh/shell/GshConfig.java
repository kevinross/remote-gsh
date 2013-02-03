package safrain.remotegsh.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Config of the shell,stored in a properties file
 * 
 * @author safrain
 * 
 */
public class GshConfig {
	public static final String DEFAULT_SERVER = "http://localhost/admin/rgsh";
	public static final String DEFAULT_CHARSET = "utf-8";
	/**
	 * Server address, with 'http://'
	 */
	private String server = DEFAULT_SERVER;
	/**
	 * Request & response charset
	 */
	private String charset = DEFAULT_CHARSET;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * Save to file
	 */
	public void save(File file) throws IOException {
		Properties p = new Properties();
		p.setProperty("server", server);
		p.setProperty("charset", charset);
		p.store(new FileWriter(file), null);
	}

	/**
	 * Load from file ,if given file does not exists,create it using default
	 * configs
	 */
	public void load(File file) throws IOException {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(file));
			String s = p.getProperty("server");
			String c = p.getProperty("charset");
			if (s != null) {
				server = s;
			}
			if (c != null) {
				charset = c;
			}
			if (s == null || c == null) {
				save(file);
			}
		} catch (FileNotFoundException e) {
			save(file);
		}
	}

}

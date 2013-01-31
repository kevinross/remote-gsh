package safrain.remotegsh.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class GshConfig {
	private String server = "localhost/admin/rgsh";

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void save(File file) throws IOException {
		Properties p = new Properties();
		p.setProperty("server", server);
		p.store(new FileWriter(file), null);
	}

	public void load(File file) throws IOException {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(file));
			String s = p.getProperty("server");
			if (s != null) {
				server = s;
			} else {
				save(file);
			}
		} catch (FileNotFoundException e) {
			save(file);
		}
	}

}

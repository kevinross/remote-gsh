package safrain.remotegsh.shell.commands;

/**
 * Quit shell
 * 
 * @author safrain
 */
public class QuitCommand implements GshCommand {

	@Override
	public boolean accept(String cmd) {
		return "quit".equals(cmd) || "exit".equals(cmd);
	}

	@Override
	public void execute(String[] args) {
		System.out.println("Bye~");
	}

	@Override
	public String getCommandTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBriefInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetailInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}

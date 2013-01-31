package safrain.remotegsh.shell.commands;

/**
 * Post a local groovy file to execute at serverside
 * 
 * @author safrain
 */
public class RunCommand implements GshCommand {

	@Override
	public boolean accept(String cmd) {
		return "run".equals(cmd);
	}

	@Override
	public void execute(String[] args) {

	}

	@Override
	public String getCommandTitle() {
		return "run";
	}

	@Override
	public String getBriefInfo() {
		return "Run script";
	}

	@Override
	public String getDetailInfo() {
		return "Run script";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

}

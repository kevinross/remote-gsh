package safrain.remotegsh.shell.commands;

public interface GshCommand {

	boolean accept(String cmd);

	void execute(String[] args);

	String getCommandTitle();

	String getBriefInfo();

	String getDetailInfo();

	String getUsage();

}

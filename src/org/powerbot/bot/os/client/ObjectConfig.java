package org.powerbot.bot.os.client;

public interface ObjectConfig {
	public String getName();

	public String[] getActions();

	public int[] getConfigs();

	public int getSettingsIndex();

	public int getVarBit();
}
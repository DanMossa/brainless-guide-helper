package com.brainlessguidehelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("brainlessguidehelper")
public interface BrainlessGuideHelperConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigSection(
		name = "Debug",
		description = "Debug settings",
		position = 100,
		closedByDefault = true
	)
	String debugSection = "debug";

	@ConfigItem(
		keyName = "deleteBankCache",
		name = "Delete Bank Cache",
		description = "Toggle on to delete the local and in-memory bank cache. Resets automatically.",
		section = "debug",
		position = 101
	)
	default boolean deleteBankCache()
	{
		return false;
	}
}

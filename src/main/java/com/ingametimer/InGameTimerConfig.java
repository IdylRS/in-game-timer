package com.ingametimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("ingametimer")
public interface InGameTimerConfig extends Config
{
	@ConfigItem(
		keyName = "countdown",
		name = "Countdown",
		description = "The time to countdown from based on in-game time"
	)
	@Units(Units.MINUTES)
	default int countdown()
	{
		return 0;
	}
}

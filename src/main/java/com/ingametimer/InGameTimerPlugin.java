package com.ingametimer;

import com.google.inject.Provides;
import javax.inject.Inject;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


@Slf4j
@PluginDescriptor(
	name = "In Game Timer"
)
public class InGameTimerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "ingametimer";
	public static final String CONFIG_KEY_SECONDS_ELAPSED = "secondsElapsed";

	@Inject
	private Client client;

	@Inject
	private InGameTimerConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TimerOverlay timerOverlay;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(timerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(timerOverlay);
	}

	public void saveSecondsElapsed(long secondsElapsed) {
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_SECONDS_ELAPSED, secondsElapsed);
	}

	public String getSavedSecondsElapsed() {
		return configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_SECONDS_ELAPSED);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged) {
		if(gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			timerOverlay.setLoggedIn(true);
		}

		if(gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			timerOverlay.setLoggedIn(false);
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event)
	{
		if (event.getEntry() == TimerOverlay.PAUSE_ENTRY) {
			timerOverlay.pauseTimer();
		}

		if(event.getEntry() == TimerOverlay.START_ENTRY) {
			timerOverlay.resumeTimer();
		}

		if(event.getEntry() == TimerOverlay.RESET_ENTRY) {
			timerOverlay.reset();
		}
	}

	@Provides
	InGameTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InGameTimerConfig.class);
	}
}

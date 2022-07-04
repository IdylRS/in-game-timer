package com.ingametimer;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "In Game Timer"
)
public class InGameTimerPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private InGameTimerConfig config;

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

	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged) {
		if(gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			timerOverlay.setLoggedIn(true);
		}

		if(gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			timerOverlay.setLoggedIn(false);
		}
	}

	@Provides
	InGameTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InGameTimerConfig.class);
	}
}

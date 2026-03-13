package com.brainlessguidehelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Brainless Guide Helper"
)
public class BrainlessGuideHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BrainlessGuideHelperConfig config;

	@Inject
	private GuideManager guideManager;

	@Inject
	private PlayerStateTracker playerStateTracker;

	@Inject
	private EventBus eventBus;

	@Override
	protected void startUp() throws Exception
	{
		guideManager.loadGuide();
		eventBus.register(playerStateTracker);
		log.debug("Brainless Guide Helper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(playerStateTracker);
		log.debug("Brainless Guide Helper stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
    BrainlessGuideHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BrainlessGuideHelperConfig.class);
	}
}

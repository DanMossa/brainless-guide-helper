package com.brainlessguidehelper;


import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

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
	private GuideProgressManager guideProgressManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	private GuidePanel guidePanel;
	private NavigationButton navigationButton;

	@Override
	protected void startUp() throws Exception
	{
		guideManager.loadGuide();
		eventBus.register(playerStateTracker);

		guidePanel = new GuidePanel(guideProgressManager, guideManager, playerStateTracker);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/com/brainlessguidehelper/icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip("Brainless Guide Helper")
			.icon(icon != null ? icon : new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))
			.priority(5)
			.panel(guidePanel)
			.build();

		clientToolbar.addNavigation(navigationButton);

		guidePanel.updateDataOnClientThread();
		guidePanel.rebuild();
		log.debug("Brainless Guide Helper started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navigationButton);
		eventBus.unregister(playerStateTracker);
		log.debug("Brainless Guide Helper stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			guidePanel.updateDataOnClientThread();
			guidePanel.rebuild();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		guidePanel.updateDataOnClientThread();
		guidePanel.rebuild();
	}

	@Provides
	BrainlessGuideHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BrainlessGuideHelperConfig.class);
	}
}

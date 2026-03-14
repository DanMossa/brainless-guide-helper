package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Requirement;
import com.brainlessguidehelper.requirements.DiaryRequirementChecker;
import com.brainlessguidehelper.requirements.ItemRequirementChecker;
import com.brainlessguidehelper.requirements.QuestRequirementChecker;
import com.brainlessguidehelper.requirements.RequirementChecker;
import com.brainlessguidehelper.requirements.SkillRequirementChecker;
import com.brainlessguidehelper.requirements.VarbitRequirementChecker;
import com.brainlessguidehelper.requirements.VarpRequirementChecker;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

/**
 * Tracks the current player state by subscribing to RuneLite events.
 * Provides methods to evaluate requirements against the live player state.
 */
@Slf4j
@Singleton
public class PlayerStateTracker
{
	private final Client client;
	private final ConfigManager configManager;
	private final Map<Requirement.RequirementType, RequirementChecker> checkers;
	private final ItemRequirementChecker itemRequirementChecker;

	// Cached player state
	private final int[] skillLevels = new int[Skill.values().length];
	private final int[] skillExperience = new int[Skill.values().length];
	private Item[] inventoryItems;
	private Item[] bankItems;
	private Item[] equipmentItems;
	private boolean loggedIn;

	@Inject
	public PlayerStateTracker(Client client, ConfigManager configManager)
	{
		this.client = client;
		this.configManager = configManager;
		this.checkers = new EnumMap<>(Requirement.RequirementType.class);

		checkers.put(Requirement.RequirementType.QUEST, new QuestRequirementChecker());
		checkers.put(Requirement.RequirementType.SKILL, new SkillRequirementChecker());
		ItemRequirementChecker itemChecker = new ItemRequirementChecker();
		checkers.put(Requirement.RequirementType.ITEM, itemChecker);
		this.itemRequirementChecker = itemChecker;
		checkers.put(Requirement.RequirementType.DIARY, new DiaryRequirementChecker());
		checkers.put(Requirement.RequirementType.VARBIT, new VarbitRequirementChecker());
		checkers.put(Requirement.RequirementType.VARP, new VarpRequirementChecker());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			loggedIn = true;
			itemRequirementChecker.loadBankCache(configManager);
			refreshSkills();
			refreshContainers();
			log.debug("Player state initialized on login");
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			loggedIn = false;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		Skill skill = event.getSkill();
		skillLevels[skill.ordinal()] = event.getLevel();
		skillExperience[skill.ordinal()] = event.getXp();
		log.debug("Stat changed: {} -> level {}", skill.getName(), event.getLevel());
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();
		ItemContainer container = event.getItemContainer();

		if (containerId == InventoryID.INVENTORY.getId())
		{
			inventoryItems = container.getItems();
			log.debug("Inventory updated: {} items", inventoryItems.length);
		}
		else if (containerId == InventoryID.BANK.getId())
		{
			bankItems = container.getItems();
			itemRequirementChecker.updateBankCache(bankItems);
			itemRequirementChecker.saveBankCache(configManager);
			log.debug("Bank updated: {} items", bankItems.length);
		}
		else if (containerId == InventoryID.EQUIPMENT.getId())
		{
			equipmentItems = container.getItems();
			log.debug("Equipment updated: {} items", equipmentItems.length);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		log.debug("Varbit changed: varbitId={} varpId={} value={}",
			event.getVarbitId(), event.getVarpId(), event.getValue());
	}

	/**
	 * Check if a single requirement is met.
	 */
	public boolean isRequirementMet(Requirement requirement)
	{
		if (requirement == null || requirement.getType() == null)
		{
			return false;
		}

		RequirementChecker checker = checkers.get(requirement.getType());
		if (checker == null)
		{
			log.warn("No checker for requirement type: {}", requirement.getType());
			return false;
		}

		return checker.isMet(requirement, client);
	}

	/**
	 * Check if all requirements in a list are met.
	 */
	public boolean areAllRequirementsMet(List<Requirement> requirements)
	{
		if (requirements == null || requirements.isEmpty())
		{
			return true;
		}

		for (Requirement requirement : requirements)
		{
			if (!isRequirementMet(requirement))
			{
				return false;
			}
		}
		return true;
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public int getSkillLevel(Skill skill)
	{
		return skillLevels[skill.ordinal()];
	}

	public int getSkillExperience(Skill skill)
	{
		return skillExperience[skill.ordinal()];
	}

	private void refreshSkills()
	{
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			skillLevels[skill.ordinal()] = client.getRealSkillLevel(skill);
			skillExperience[skill.ordinal()] = client.getSkillExperience(skill);
		}
	}

	private void refreshContainers()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			inventoryItems = inventory.getItems();
		}

		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null)
		{
			bankItems = bank.getItems();
			itemRequirementChecker.updateBankCache(bankItems);
		}

		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			equipmentItems = equipment.getItems();
		}
	}
}

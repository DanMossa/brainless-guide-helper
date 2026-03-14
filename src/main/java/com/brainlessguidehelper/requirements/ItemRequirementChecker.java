package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;

@Slf4j
public class ItemRequirementChecker implements RequirementChecker
{
	private static final Map<String, Integer> ITEM_NAME_TO_ID = buildItemNameMap();

	private final Map<Integer, Integer> bankCache = new HashMap<>();

	private static Map<String, Integer> buildItemNameMap()
	{
		Map<String, Integer> map = new HashMap<>();
		for (Field field : ItemID.class.getDeclaredFields())
		{
			if (Modifier.isPublic(field.getModifiers())
				&& Modifier.isStatic(field.getModifiers())
				&& Modifier.isFinal(field.getModifiers())
				&& field.getType() == int.class)
			{
				try
				{
					map.put(field.getName(), field.getInt(null));
				}
				catch (IllegalAccessException e)
				{
					log.warn("Failed to read ItemID field: {}", field.getName(), e);
				}
			}
		}
		return map;
	}

	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		Object idObj = requirement.getId();
		int itemId;

		if (idObj instanceof Number)
		{
			itemId = ((Number) idObj).intValue();
		}
		else if (idObj instanceof String)
		{
			String name = (String) idObj;
			Integer resolved = ITEM_NAME_TO_ID.get(name);
			if (resolved == null)
			{
				log.warn("Unknown item name: {} — no matching constant in ItemID", name);
				return false;
			}
			itemId = resolved;
		}
		else
		{
			log.warn("Item requirement has unsupported id type: {}", idObj);
			return false;
		}

		int requiredAmount = requirement.getAmount() != null ? requirement.getAmount() : 1;
		Requirement.ItemLocation location = requirement.getLocation();

		if (location == null)
		{
			location = Requirement.ItemLocation.ANY;
		}

		switch (location)
		{
			case INVENTORY:
				return countInContainer(client, InventoryID.INVENTORY, itemId) >= requiredAmount;
			case BANK:
				return countInBank(client, itemId) >= requiredAmount;
			case EQUIPPED:
				return countInContainer(client, InventoryID.EQUIPMENT, itemId) >= requiredAmount;
			case ANY:
			default:
				int total = countInContainer(client, InventoryID.INVENTORY, itemId)
					+ countInBank(client, itemId)
					+ countInContainer(client, InventoryID.EQUIPMENT, itemId);
				return total >= requiredAmount;
		}
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.ITEM;
	}

	/**
	 * Updates the bank cache from the given items array.
	 * Called by PlayerStateTracker when a bank ItemContainerChanged event fires.
	 */
	public void updateBankCache(Item[] items)
	{
		bankCache.clear();
		if (items == null)
		{
			return;
		}
		for (Item item : items)
		{
			if (item.getId() >= 0)
			{
				bankCache.merge(item.getId(), item.getQuantity(), Integer::sum);
			}
		}
		log.debug("Bank cache updated: {} distinct items", bankCache.size());
	}

	private int countInBank(Client client, int itemId)
	{
		ItemContainer container = client.getItemContainer(InventoryID.BANK);
		if (container != null)
		{
			return container.count(itemId);
		}
		return countFromBankCache(itemId);
	}

	/**
	 * Returns the cached bank count for the given item ID.
	 * Package-private for testability.
	 */
	int countFromBankCache(int itemId)
	{
		return bankCache.getOrDefault(itemId, 0);
	}

	private int countInContainer(Client client, InventoryID inventoryID, int itemId)
	{
		ItemContainer container = client.getItemContainer(inventoryID);
		if (container == null)
		{
			return 0;
		}
		return container.count(itemId);
	}
}

package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;

@Slf4j
public class ItemRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		Object idObj = requirement.getId();
		if (!(idObj instanceof Number))
		{
			log.warn("Item requirement has non-numeric id: {} — item name lookup not yet supported", idObj);
			return false;
		}

		int itemId = ((Number) idObj).intValue();
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
				return countInContainer(client, InventoryID.BANK, itemId) >= requiredAmount;
			case EQUIPPED:
				return countInContainer(client, InventoryID.EQUIPMENT, itemId) >= requiredAmount;
			case ANY:
			default:
				int total = countInContainer(client, InventoryID.INVENTORY, itemId)
					+ countInContainer(client, InventoryID.BANK, itemId)
					+ countInContainer(client, InventoryID.EQUIPMENT, itemId);
				return total >= requiredAmount;
		}
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.ITEM;
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

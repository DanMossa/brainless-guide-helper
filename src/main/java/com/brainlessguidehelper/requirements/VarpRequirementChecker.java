package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

/**
 * Checks requirements based on varp (player variable) values.
 * The requirement's id is the varp index and level is the expected minimum value.
 */
@Slf4j
public class VarpRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		Object idObj = requirement.getId();
		if (!(idObj instanceof Number))
		{
			log.warn("Varp requirement has non-numeric id: {}", idObj);
			return false;
		}

		int varpId = ((Number) idObj).intValue();
		int expectedValue = requirement.getLevel() != null ? requirement.getLevel() : 1;
		int currentValue = client.getVarpValue(varpId);

		return currentValue >= expectedValue;
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.VARP;
	}
}

package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

/**
 * Checks requirements based on varbit values.
 * The requirement's id is the varbit index and level is the expected minimum value.
 */
@Slf4j
public class VarbitRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		Object idObj = requirement.getId();
		if (!(idObj instanceof Number))
		{
			log.warn("Varbit requirement has non-numeric id: {}", idObj);
			return false;
		}

		int varbitId = ((Number) idObj).intValue();
		int expectedValue = requirement.getLevel() != null ? requirement.getLevel() : 1;
		int currentValue = client.getVarbitValue(varbitId);

		return currentValue >= expectedValue;
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.VARBIT;
	}
}

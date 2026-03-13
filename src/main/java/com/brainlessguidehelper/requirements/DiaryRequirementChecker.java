package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

/**
 * Checks Achievement Diary completion via varbit/varp values.
 * Diary progress in OSRS is typically tracked through varbits.
 * The requirement's id should be the varbit id, and level the expected value.
 */
@Slf4j
public class DiaryRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		String diaryId = String.valueOf(requirement.getId());
		String requiredStatus = requirement.getStatus();

		// Diary requirements are checked via varbits
		// For now, we log that diary checking requires varbit mapping
		log.debug("Checking diary requirement: {} status: {}", diaryId, requiredStatus);

		// Diary completion is typically checked via specific varbits
		// This will be expanded as diary varbit mappings are added
		return false;
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.DIARY;
	}
}

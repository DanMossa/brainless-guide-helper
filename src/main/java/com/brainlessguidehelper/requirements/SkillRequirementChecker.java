package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;

@Slf4j
public class SkillRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		String skillName = String.valueOf(requirement.getId());
		Integer requiredLevel = requirement.getLevel();

		if (requiredLevel == null)
		{
			log.warn("Skill requirement missing level for: {}", skillName);
			return false;
		}

		Skill skill = findSkill(skillName);
		if (skill == null)
		{
			log.warn("Unknown skill: {}", skillName);
			return false;
		}

		int currentLevel = client.getRealSkillLevel(skill);
		return currentLevel >= requiredLevel;
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.SKILL;
	}

	private Skill findSkill(String name)
	{
		try
		{
			return Skill.valueOf(name.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}

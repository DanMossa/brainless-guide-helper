package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

@Slf4j
public class QuestRequirementChecker implements RequirementChecker
{
	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		String questName = String.valueOf(requirement.getId());
		String requiredStatus = requirement.getStatus();

		Quest quest = findQuest(questName);
		if (quest == null)
		{
			log.warn("Unknown quest: {}", questName);
			return false;
		}

		QuestState currentState = quest.getState(client);
		if (currentState == null)
		{
			return false;
		}

		switch (requiredStatus != null ? requiredStatus.toUpperCase() : "COMPLETED")
		{
			case "COMPLETED":
				return currentState == QuestState.FINISHED;
			case "STARTED":
				return currentState == QuestState.IN_PROGRESS || currentState == QuestState.FINISHED;
			case "NOT_STARTED":
				return currentState == QuestState.NOT_STARTED;
			default:
				log.warn("Unknown quest status: {}", requiredStatus);
				return false;
		}
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.QUEST;
	}

	private Quest findQuest(String name)
	{
		// Normalize the quest name to match the enum format
		String normalized = name.toUpperCase()
			.replace("'", "")
			.replace("'", "")
			.replace(" ", "_")
			.replace("-", "_")
			.replace("&", "AND");

		for (Quest quest : Quest.values())
		{
			if (quest.name().equals(normalized))
			{
				return quest;
			}
		}

		// Fallback: try matching by the quest's display name
		for (Quest quest : Quest.values())
		{
			if (quest.getName().equalsIgnoreCase(name))
			{
				return quest;
			}
		}

		return null;
	}
}

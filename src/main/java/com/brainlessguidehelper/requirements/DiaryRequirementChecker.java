package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import java.util.HashMap;
import java.util.Map;

/**
 * Checks Achievement Diary requirements via varbit/varp values.
 *
 * Supports two modes:
 * 1. Full diary tier completion: status is null or "COMPLETED" — checks the reward varbit (value >= 1).
 *    Example JSON: { "type": "DIARY", "id": "ARDOUGNE_EASY", "status": "COMPLETED" }
 *
 * 2. Individual task completion: status is a task name — checks a specific bit in the diary's VarPlayer.
 *    Example JSON: { "type": "DIARY", "id": "ARDOUGNE_EASY", "status": "PULL_LEVER" }
 */
@Slf4j
public class DiaryRequirementChecker implements RequirementChecker
{
	// Maps diary tier name -> reward varbit ID (for full completion checks)
	private static final Map<String, Integer> DIARY_VARBIT_MAP = new HashMap<>();

	// Maps "REGION_TIER.TASK_NAME" -> VarPlayer bit info (for individual task checks)
	private static final Map<String, TaskBit> DIARY_TASK_MAP = new HashMap<>();

	/**
	 * Holds the VarPlayer ID and bit position for an individual diary task.
	 */
	static class TaskBit
	{
		final int varpId;
		final int bitPosition;

		TaskBit(int varpId, int bitPosition)
		{
			this.varpId = varpId;
			this.bitPosition = bitPosition;
		}
	}

	static
	{
		// =====================================================================
		// Reward varbits for full diary tier completion (value >= 1 = completed)
		// =====================================================================

		// Ardougne
		DIARY_VARBIT_MAP.put("ARDOUGNE_EASY", VarbitID.ARDOUGNE_EASY_REWARD);
		DIARY_VARBIT_MAP.put("ARDOUGNE_MEDIUM", VarbitID.ARDOUGNE_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("ARDOUGNE_HARD", VarbitID.ARDOUGNE_HARD_REWARD);
		DIARY_VARBIT_MAP.put("ARDOUGNE_ELITE", VarbitID.ARDOUGNE_ELITE_REWARD);

		// Desert
		DIARY_VARBIT_MAP.put("DESERT_EASY", VarbitID.DESERT_EASY_REWARD);
		DIARY_VARBIT_MAP.put("DESERT_MEDIUM", VarbitID.DESERT_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("DESERT_HARD", VarbitID.DESERT_HARD_REWARD);
		DIARY_VARBIT_MAP.put("DESERT_ELITE", VarbitID.DESERT_ELITE_REWARD);

		// Falador
		DIARY_VARBIT_MAP.put("FALADOR_EASY", VarbitID.FALADOR_EASY_REWARD);
		DIARY_VARBIT_MAP.put("FALADOR_MEDIUM", VarbitID.FALADOR_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("FALADOR_HARD", VarbitID.FALADOR_HARD_REWARD);
		DIARY_VARBIT_MAP.put("FALADOR_ELITE", VarbitID.FALADOR_ELITE_REWARD);

		// Fremennik
		DIARY_VARBIT_MAP.put("FREMENNIK_EASY", VarbitID.FREMENNIK_EASY_REWARD);
		DIARY_VARBIT_MAP.put("FREMENNIK_MEDIUM", VarbitID.FREMENNIK_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("FREMENNIK_HARD", VarbitID.FREMENNIK_HARD_REWARD);
		DIARY_VARBIT_MAP.put("FREMENNIK_ELITE", VarbitID.FREMENNIK_ELITE_REWARD);

		// Kandarin
		DIARY_VARBIT_MAP.put("KANDARIN_EASY", VarbitID.KANDARIN_EASY_REWARD);
		DIARY_VARBIT_MAP.put("KANDARIN_MEDIUM", VarbitID.KANDARIN_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("KANDARIN_HARD", VarbitID.KANDARIN_HARD_REWARD);
		DIARY_VARBIT_MAP.put("KANDARIN_ELITE", VarbitID.KANDARIN_ELITE_REWARD);

		// Karamja
		DIARY_VARBIT_MAP.put("KARAMJA_EASY", VarbitID.ATJUN_EASY_REWARD);
		DIARY_VARBIT_MAP.put("KARAMJA_MEDIUM", VarbitID.ATJUN_MED_REWARD);
		DIARY_VARBIT_MAP.put("KARAMJA_HARD", VarbitID.ATJUN_HARD_REWARD);
		DIARY_VARBIT_MAP.put("KARAMJA_ELITE", VarbitID.KARAMJA_ELITE_REWARD);

		// Kourend & Kebos
		DIARY_VARBIT_MAP.put("KOUREND_EASY", VarbitID.KOUREND_EASY_REWARD);
		DIARY_VARBIT_MAP.put("KOUREND_MEDIUM", VarbitID.KOUREND_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("KOUREND_HARD", VarbitID.KOUREND_HARD_REWARD);
		DIARY_VARBIT_MAP.put("KOUREND_ELITE", VarbitID.KOUREND_ELITE_REWARD);

		// Lumbridge & Draynor
		DIARY_VARBIT_MAP.put("LUMBRIDGE_EASY", VarbitID.LUMBRIDGE_EASY_REWARD);
		DIARY_VARBIT_MAP.put("LUMBRIDGE_MEDIUM", VarbitID.LUMBRIDGE_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("LUMBRIDGE_HARD", VarbitID.LUMBRIDGE_HARD_REWARD);
		DIARY_VARBIT_MAP.put("LUMBRIDGE_ELITE", VarbitID.LUMBRIDGE_ELITE_REWARD);

		// Morytania
		DIARY_VARBIT_MAP.put("MORYTANIA_EASY", VarbitID.MORYTANIA_EASY_REWARD);
		DIARY_VARBIT_MAP.put("MORYTANIA_MEDIUM", VarbitID.MORYTANIA_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("MORYTANIA_HARD", VarbitID.MORYTANIA_HARD_REWARD);
		DIARY_VARBIT_MAP.put("MORYTANIA_ELITE", VarbitID.MORYTANIA_ELITE_REWARD);

		// Varrock
		DIARY_VARBIT_MAP.put("VARROCK_EASY", VarbitID.VARROCK_EASY_REWARD);
		DIARY_VARBIT_MAP.put("VARROCK_MEDIUM", VarbitID.VARROCK_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("VARROCK_HARD", VarbitID.VARROCK_HARD_REWARD);
		DIARY_VARBIT_MAP.put("VARROCK_ELITE", VarbitID.VARROCK_ELITE_REWARD);

		// Western Provinces
		DIARY_VARBIT_MAP.put("WESTERN_EASY", VarbitID.WESTERN_EASY_REWARD);
		DIARY_VARBIT_MAP.put("WESTERN_MEDIUM", VarbitID.WESTERN_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("WESTERN_HARD", VarbitID.WESTERN_HARD_REWARD);
		DIARY_VARBIT_MAP.put("WESTERN_ELITE", VarbitID.WESTERN_ELITE_REWARD);

		// Wilderness
		DIARY_VARBIT_MAP.put("WILDERNESS_EASY", VarbitID.WILDERNESS_EASY_REWARD);
		DIARY_VARBIT_MAP.put("WILDERNESS_MEDIUM", VarbitID.WILDERNESS_MEDIUM_REWARD);
		DIARY_VARBIT_MAP.put("WILDERNESS_HARD", VarbitID.WILDERNESS_HARD_REWARD);
		DIARY_VARBIT_MAP.put("WILDERNESS_ELITE", VarbitID.WILDERNESS_ELITE_REWARD);

		// =====================================================================
		// Individual diary task bit mappings
		// Format: "REGION_TIER.TASK_NAME" -> TaskBit(VarPlayerID, bitPosition)
		//
		// Bit positions sourced from quest-helper plugin:
		// https://github.com/Zoinkwiz/quest-helper/tree/master/src/main/java/com/questhelper/helpers/achievementdiaries
		//
		// To find a new task's bit position:
		// 1. Open RuneLite Var Inspector, do the task, note which VarPlayer bit flips
		// 2. OR check the quest-helper source for the region's diary class
		// =====================================================================

		// Ardougne Easy tasks (VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY = 1196)
		// Source: ArdougneEasy.java from quest-helper
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.ESS_MINE", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 0));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.STEAL_CAKE", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 1));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.SELL_SILK", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 2));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.EAST_ARDY_ALTAR", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 4));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.FISHING_TRAWLER", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 5));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.ENTER_COMBAT_CAMP", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 6));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.IDENTIFY_SWORD", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 7));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.PULL_LEVER", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 9));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.ALECKS_EMPORIUM", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 11));
		DIARY_TASK_MAP.put("ARDOUGNE_EASY.PROBITA_PET", new TaskBit(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, 12));

		// TODO: Add more diary tasks as needed. Use the quest-helper source or Var Inspector to find
		// the VarPlayerID and bit position for each task. Each region's VarPlayerIDs are:
		//   Ardougne:   VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY (1196), ARDOUNGE_ACHIEVEMENT_DIARY2 (1197)
		//   Desert:     VarPlayerID.DESERT_ACHIEVEMENT_DIARY (1198), DESERT_ACHIEVEMENT_DIARY2 (1199)
		//   Falador:    VarPlayerID.FALADOR_ACHIEVEMENT_DIARY (1186), FALADOR_ACHIEVEMENT_DIARY2 (1187)
		//   Fremennik:  VarPlayerID.FREMENNIK_ACHIEVEMENT_DIARY (1184), FREMENNIK_ACHIEVEMENT_DIARY2 (1185)
		//   Kandarin:   VarPlayerID.KANDARIN_ACHIEVEMENT_DIARY (1178), KANDARIN_ACHIEVEMENT_DIARY2 (1179)
		//   Karamja:    VarPlayerID.ACHIEVEMENT_DIARY (1188), ACHIEVEMENT_DIARY2 (1189)
		//   Kourend:    VarPlayerID.KOUREND_ACHIEVEMENT_DIARY (2085), KOUREND_ACHIEVEMENT_DIARY2 (2086)
		//   Lumbridge:  VarPlayerID.LUMB_DRAY_ACHIEVEMENT_DIARY (1194), LUMB_DRAY_ACHIEVEMENT_DIARY2 (1195)
		//   Morytania:  VarPlayerID.MORYTANIA_ACHIEVEMENT_DIARY (1180), MORYTANIA_ACHIEVEMENT_DIARY2 (1181)
		//   Varrock:    VarPlayerID.VARROCK_ACHIEVEMENT_DIARY (1176), VARROCK_ACHIEVEMENT_DIARY2 (1177)
		//   Western:    VarPlayerID.WESTERN_ACHIEVEMENT_DIARY (1182), WESTERN_ACHIEVEMENT_DIARY2 (1183)
		//   Wilderness: VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY (1192), WILDERNESS_ACHIEVEMENT_DIARY2 (1193)
	}

	@Override
	public boolean isMet(Requirement requirement, Client client)
	{
		String diaryId = String.valueOf(requirement.getId()).toUpperCase();
		String requiredStatus = requirement.getStatus();

		// Check the reward varbit exists for this diary tier
		Integer varbitId = DIARY_VARBIT_MAP.get(diaryId);
		if (varbitId == null)
		{
			log.warn("Unknown diary requirement: {}", diaryId);
			return false;
		}

		// Full diary completion check (status is null or "COMPLETED")
		if (requiredStatus == null || requiredStatus.equalsIgnoreCase("COMPLETED"))
		{
			int currentValue = client.getVarbitValue(varbitId);
			log.debug("Checking diary completion: {} varbit: {} value: {}",
				diaryId, varbitId, currentValue);
			return currentValue >= 1;
		}

		// Individual task check via VarPlayer bit
		String taskKey = diaryId + "." + requiredStatus.toUpperCase();
		TaskBit taskBit = DIARY_TASK_MAP.get(taskKey);
		if (taskBit == null)
		{
			log.warn("Unknown diary task: {} (key: {}). Add the task's VarPlayer bit mapping to " +
				"DIARY_TASK_MAP in DiaryRequirementChecker.", requiredStatus, taskKey);
			return false;
		}

		int varpValue = client.getVarpValue(taskBit.varpId);
		boolean taskDone = ((varpValue >> taskBit.bitPosition) & 1) == 1;
		log.debug("Checking diary task: {} varp: {} bit: {} varpValue: {} taskDone: {}",
			taskKey, taskBit.varpId, taskBit.bitPosition, varpValue, taskDone);
		return taskDone;
	}

	@Override
	public Requirement.RequirementType getType()
	{
		return Requirement.RequirementType.DIARY;
	}
}

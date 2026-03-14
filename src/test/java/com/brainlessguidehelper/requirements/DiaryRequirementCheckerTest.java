package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

public class DiaryRequirementCheckerTest
{
	private DiaryRequirementChecker checker;

	@Before
	public void setUp()
	{
		checker = new DiaryRequirementChecker();
	}

	@Test
	public void testGetType()
	{
		assertEquals(Requirement.RequirementType.DIARY, checker.getType());
	}

	@Test
	public void testDiaryVarbitMapContainsAllRegions() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_VARBIT_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Integer> map = (Map<String, Integer>) mapField.get(null);

		String[] regions = {
			"ARDOUGNE", "DESERT", "FALADOR", "FREMENNIK", "KANDARIN",
			"KARAMJA", "KOUREND", "LUMBRIDGE", "MORYTANIA", "VARROCK",
			"WESTERN", "WILDERNESS"
		};
		String[] tiers = {"EASY", "MEDIUM", "HARD", "ELITE"};

		for (String region : regions)
		{
			for (String tier : tiers)
			{
				String key = region + "_" + tier;
				assertNotNull("Missing diary entry: " + key, map.get(key));
			}
		}

		assertEquals(48, map.size());
	}

	@Test
	public void testKaramjaVarbitMappings() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_VARBIT_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Integer> map = (Map<String, Integer>) mapField.get(null);

		assertEquals(Integer.valueOf(VarbitID.ATJUN_EASY_REWARD), map.get("KARAMJA_EASY"));
		assertEquals(Integer.valueOf(VarbitID.ATJUN_MED_REWARD), map.get("KARAMJA_MEDIUM"));
		assertEquals(Integer.valueOf(VarbitID.ATJUN_HARD_REWARD), map.get("KARAMJA_HARD"));
		assertEquals(Integer.valueOf(VarbitID.KARAMJA_ELITE_REWARD), map.get("KARAMJA_ELITE"));
	}

	@Test
	public void testArdougneVarbitMappings() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_VARBIT_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Integer> map = (Map<String, Integer>) mapField.get(null);

		assertEquals(Integer.valueOf(VarbitID.ARDOUGNE_EASY_REWARD), map.get("ARDOUGNE_EASY"));
		assertEquals(Integer.valueOf(VarbitID.ARDOUGNE_MEDIUM_REWARD), map.get("ARDOUGNE_MEDIUM"));
		assertEquals(Integer.valueOf(VarbitID.ARDOUGNE_HARD_REWARD), map.get("ARDOUGNE_HARD"));
		assertEquals(Integer.valueOf(VarbitID.ARDOUGNE_ELITE_REWARD), map.get("ARDOUGNE_ELITE"));
	}

	@Test
	public void testPullLeverTaskMapping() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_TASK_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, ?> map = (Map<String, ?>) mapField.get(null);

		assertNotNull("PULL_LEVER task should be mapped", map.get("ARDOUGNE_EASY.PULL_LEVER"));
	}

	@Test
	public void testArdougneEasyTaskCount() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_TASK_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, ?> map = (Map<String, ?>) mapField.get(null);

		long ardougneEasyTasks = map.keySet().stream()
			.filter(k -> k.startsWith("ARDOUGNE_EASY."))
			.count();
		assertEquals("Should have 10 Ardougne Easy tasks mapped", 10, ardougneEasyTasks);
	}

	@Test
	public void testTaskBitVarpAndPosition() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_TASK_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) mapField.get(null);

		Object pullLever = map.get("ARDOUGNE_EASY.PULL_LEVER");
		assertNotNull(pullLever);

		// Access TaskBit fields via reflection (package-private class)
		Field varpField = pullLever.getClass().getDeclaredField("varpId");
		Field bitField = pullLever.getClass().getDeclaredField("bitPosition");
		varpField.setAccessible(true);
		bitField.setAccessible(true);

		assertEquals("PULL_LEVER varpId should be ARDOUNGE_ACHIEVEMENT_DIARY",
			VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, varpField.getInt(pullLever));
		assertEquals("PULL_LEVER bit position should be 9",
			9, bitField.getInt(pullLever));
	}

	@Test
	public void testAllArdougneEasyTasksUseCorrectVarp() throws Exception
	{
		Field mapField = DiaryRequirementChecker.class.getDeclaredField("DIARY_TASK_MAP");
		mapField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) mapField.get(null);

		for (Map.Entry<String, Object> entry : map.entrySet())
		{
			if (entry.getKey().startsWith("ARDOUGNE_EASY."))
			{
				Object taskBit = entry.getValue();
				Field varpField = taskBit.getClass().getDeclaredField("varpId");
				varpField.setAccessible(true);
				assertEquals("Task " + entry.getKey() + " should use ARDOUNGE_ACHIEVEMENT_DIARY varp",
					VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY, varpField.getInt(taskBit));
			}
		}
	}
}

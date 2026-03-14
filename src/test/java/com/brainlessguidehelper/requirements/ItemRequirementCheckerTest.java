package com.brainlessguidehelper.requirements;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ItemRequirementCheckerTest
{
	private ItemRequirementChecker checker;

	@Before
	public void setUp()
	{
		checker = new ItemRequirementChecker();
	}

	/**
	 * Mirrors the static lookup map built by ItemRequirementChecker to verify
	 * that common item names from the guide JSON resolve correctly.
	 */
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
					// skip
				}
			}
		}
		return map;
	}

	@Test
	public void testCommonItemNamesResolve()
	{
		Map<String, Integer> map = buildItemNameMap();

		assertEquals((int) map.get("ASHES"), ItemID.ASHES);
		assertEquals((int) map.get("LOGS"), ItemID.LOGS);
		assertEquals((int) map.get("ROPE"), ItemID.ROPE);
		assertEquals((int) map.get("HAMMER"), ItemID.HAMMER);
		assertEquals((int) map.get("CHISEL"), ItemID.CHISEL);
		assertEquals((int) map.get("SPADE"), ItemID.SPADE);
	}

	@Test
	public void testUnknownNameReturnsNull()
	{
		Map<String, Integer> map = buildItemNameMap();
		assertNull(map.get("TOTALLY_FAKE_ITEM_NAME_XYZ"));
	}

	@Test
	public void testMapIsNotEmpty()
	{
		Map<String, Integer> map = buildItemNameMap();
		assertTrue("ItemID map should contain many entries", map.size() > 100);
	}

	@Test
	public void testBankCachePopulatedFromItems()
	{
		int itemId = ItemID.LOGS;
		Item[] bankItems = new Item[]{new Item(itemId, 50)};
		checker.updateBankCache(bankItems);

		assertEquals(50, checker.countFromBankCache(itemId));
	}

	@Test
	public void testBankCacheReturnsZeroForUnknownItem()
	{
		checker.updateBankCache(new Item[]{new Item(ItemID.LOGS, 10)});

		assertEquals(0, checker.countFromBankCache(ItemID.ROPE));
	}

	@Test
	public void testBankCacheUpdatedOnReopen()
	{
		int itemId = ItemID.LOGS;

		// First bank visit: 50 logs
		checker.updateBankCache(new Item[]{new Item(itemId, 50)});
		assertEquals(50, checker.countFromBankCache(itemId));

		// Second bank visit: only 10 logs now
		checker.updateBankCache(new Item[]{new Item(itemId, 10)});
		assertEquals(10, checker.countFromBankCache(itemId));
	}

	@Test
	public void testBankCacheClearedOnUpdate()
	{
		int logsId = ItemID.LOGS;
		int ropeId = ItemID.ROPE;

		// First visit has both items
		checker.updateBankCache(new Item[]{new Item(logsId, 50), new Item(ropeId, 5)});
		assertEquals(50, checker.countFromBankCache(logsId));
		assertEquals(5, checker.countFromBankCache(ropeId));

		// Second visit has only logs — rope should be gone
		checker.updateBankCache(new Item[]{new Item(logsId, 50)});
		assertEquals(50, checker.countFromBankCache(logsId));
		assertEquals(0, checker.countFromBankCache(ropeId));
	}

	@Test
	public void testBankCacheHandlesNullItems()
	{
		checker.updateBankCache(new Item[]{new Item(ItemID.LOGS, 50)});
		assertEquals(50, checker.countFromBankCache(ItemID.LOGS));

		checker.updateBankCache(null);
		assertEquals(0, checker.countFromBankCache(ItemID.LOGS));
	}

	@Test
	public void testBankCacheAggregatesDuplicateItemIds()
	{
		int itemId = ItemID.LOGS;
		// Two separate stacks of the same item
		Item[] bankItems = new Item[]{new Item(itemId, 30), new Item(itemId, 20)};
		checker.updateBankCache(bankItems);

		assertEquals(50, checker.countFromBankCache(itemId));
	}

	@Test
	public void testEmptyCacheReturnsZero()
	{
		assertEquals(0, checker.countFromBankCache(ItemID.LOGS));
	}
}

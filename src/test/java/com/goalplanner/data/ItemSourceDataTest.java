package com.goalplanner.data;

import com.goalplanner.model.ItemTag;
import com.goalplanner.model.TagCategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the item-source data loads from its externalized resource
 * ({@code item-sources.tsv}) — the class init throws if the resource is
 * missing or malformed, so a populated lookup proves the load path.
 */
class ItemSourceDataTest
{
	@Test
	@DisplayName("loads source tags from the resource for known collection-log items")
	void loadsKnownSources()
	{
		// Abyssal orphan (13262) → Abyssal Sire, BOSS.
		List<ItemTag> tags = ItemSourceData.getTags(13262);
		assertTrue(tags.stream().anyMatch(t ->
				"Abyssal Sire".equals(t.getLabel()) && t.getCategory() == TagCategory.BOSS),
			"expected Abyssal Sire BOSS tag for item 13262");
	}

	@Test
	@DisplayName("unknown item ids return an empty (non-null) list")
	void unknownItemEmpty()
	{
		assertEquals(List.of(), ItemSourceData.getTags(-1));
	}
}

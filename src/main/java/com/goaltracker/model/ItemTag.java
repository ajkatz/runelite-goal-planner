package com.goaltracker.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A tag linking an item goal to its source.
 * Future: clickable for navigation, searchable for filtering.
 */
@Data
@NoArgsConstructor
public class ItemTag
{
	private String label;          // "Zulrah", "Chambers of Xeric", "Hard Clue"
	private TagCategory category;  // BOSS, RAID, CLUE, etc.

	/**
	 * User-set color override packed as 0xRRGGBB. -1 means "use category default".
	 * Only custom tags on custom goals can meaningfully set this (enforced by API);
	 * default tags inherit their color from {@link TagCategory#getColor()}.
	 */
	private int colorRgb = -1;

	public ItemTag(String label, TagCategory category)
	{
		this.label = label;
		this.category = category;
		this.colorRgb = -1;
	}

	public ItemTag(String label, TagCategory category, int colorRgb)
	{
		this.label = label;
		this.category = category;
		this.colorRgb = colorRgb;
	}
}

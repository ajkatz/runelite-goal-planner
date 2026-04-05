package com.goaltracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A tag linking an item goal to its source.
 * Future: clickable for navigation, searchable for filtering.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTag
{
	private String label;          // "Zulrah", "Chambers of Xeric", "Hard Clue"
	private TagCategory category;  // BOSS, RAID, CLUE, etc.
}

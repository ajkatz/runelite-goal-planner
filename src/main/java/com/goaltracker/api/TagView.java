package com.goaltracker.api;

/**
 * Public DTO for a tag attached to a goal. No internal model classes referenced.
 */
public class TagView
{
	public String label;
	/** Category name as a string. One of: BOSS, RAID, CLUE, MINIGAME, SKILLING, SPECIAL, OTHER. */
	public String category;
	/** Packed RGB color (0xRRGGBB) for the tag pill, derived from the category. */
	public int colorRgb;

	public TagView() {}

	public TagView(String label, String category, int colorRgb)
	{
		this.label = label;
		this.category = category;
		this.colorRgb = colorRgb;
	}
}

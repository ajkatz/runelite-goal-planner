package com.goalplanner;

/**
 * Size scale for the Goal Planner panel text. The multiplier is applied to every
 * panel font so players on large or high-DPI displays can size the whole panel
 * up (or down) in one place.
 */
public enum PanelFontScale
{
	SMALL("Small", 0.9f),
	NORMAL("Normal", 1.0f),
	LARGE("Large", 1.15f),
	LARGER("Larger", 1.3f);

	private final String label;
	private final float multiplier;

	PanelFontScale(String label, float multiplier)
	{
		this.label = label;
		this.multiplier = multiplier;
	}

	public float getMultiplier()
	{
		return multiplier;
	}

	@Override
	public String toString()
	{
		return label;
	}
}

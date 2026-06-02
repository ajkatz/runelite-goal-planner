package com.goalplanner;

/**
 * Font family options for the Goal Planner panel, surfaced in config so players
 * can pick a more legible face than the look-and-feel default. The non-default
 * values map to Java's logical font families, which are always available.
 */
public enum PanelFontFamily
{
	DEFAULT("Default"),
	SANS_SERIF("Sans-serif"),
	SERIF("Serif");

	private final String label;

	PanelFontFamily(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}

package com.goaltracker.data;

import java.util.*;

/**
 * Attributes for drop sources (e.g., "requires slayer task").
 * Items from sources with attributes inherit those attributes as additional tags.
 */
public final class SourceAttributes
{
	private SourceAttributes() {}

	public enum Attribute
	{
		SLAYER_TASK("Slayer Task");

		private final String displayName;

		Attribute(String displayName)
		{
			this.displayName = displayName;
		}

		public String getDisplayName()
		{
			return displayName;
		}
	}

	private static final Map<String, Set<Attribute>> SOURCE_ATTRS = new HashMap<>();

	static
	{
		slayerTask("Abyssal Sire");
		slayerTask("Alchemical Hydra");
		slayerTask("Cerberus");
		slayerTask("Thermonuclear Smoke Devil");
		slayerTask("Kraken");
		slayerTask("Grotesque Guardians");
		slayerTask("Araxxor");
	}

	private static void slayerTask(String sourceName)
	{
		SOURCE_ATTRS.computeIfAbsent(sourceName, k -> new HashSet<>()).add(Attribute.SLAYER_TASK);
	}

	/**
	 * Get attributes for a source, or empty set if none.
	 */
	public static Set<Attribute> getAttributes(String sourceName)
	{
		return SOURCE_ATTRS.getOrDefault(sourceName, Collections.emptySet());
	}

	/**
	 * Check if a source requires a slayer task.
	 */
	public static boolean isSlayerTask(String sourceName)
	{
		return getAttributes(sourceName).contains(Attribute.SLAYER_TASK);
	}
}

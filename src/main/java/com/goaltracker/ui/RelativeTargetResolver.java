package com.goaltracker.ui;

/**
 * Pure helpers for resolving "gain X more" relative goal inputs into the
 * absolute target values stored on Goal entities. Mission 23.
 *
 * <p>Relative goals are an input convenience only — once resolved, the goal
 * is indistinguishable from one created with the same absolute target. The
 * math lives here so it can be unit-tested without spinning up Swing or the
 * RuneLite client.
 */
public final class RelativeTargetResolver
{
	private RelativeTargetResolver() {}

	/** OSRS XP cap. Skill XP saturates here regardless of what the user enters. */
	public static final int MAX_SKILL_XP = 200_000_000;

	/**
	 * Resolve a "gain N XP" delta against the player's current XP for a skill.
	 * Negative or zero deltas are returned as -1 to signal "invalid input" so
	 * the caller can show an error message.
	 *
	 * @param currentXp the player's current XP for the skill (>= 0)
	 * @param deltaXp the XP the user wants to gain (must be > 0)
	 * @return resolved absolute XP target, clamped at {@link #MAX_SKILL_XP},
	 *         or -1 if delta is non-positive
	 */
	public static int resolveSkillXp(int currentXp, int deltaXp)
	{
		if (deltaXp <= 0) return -1;
		long sum = (long) Math.max(0, currentXp) + (long) deltaXp;
		return (int) Math.min((long) MAX_SKILL_XP, sum);
	}

	/**
	 * Resolve a "gain N items" delta against the player's current item count.
	 * Negative or zero deltas are returned as -1 (invalid). Capped at
	 * Integer.MAX_VALUE to avoid overflow on absurd inputs.
	 *
	 * @param currentCount the player's current count across all known
	 *                     containers (>= 0)
	 * @param deltaCount the additional items the user wants
	 * @return resolved absolute item target, or -1 if delta is non-positive
	 */
	public static int resolveItemCount(int currentCount, int deltaCount)
	{
		if (deltaCount <= 0) return -1;
		long sum = (long) Math.max(0, currentCount) + (long) deltaCount;
		return (int) Math.min((long) Integer.MAX_VALUE, sum);
	}
}

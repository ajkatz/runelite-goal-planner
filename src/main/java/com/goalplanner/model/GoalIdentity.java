package com.goalplanner.model;

/**
 * The shared per-type definition of what makes two goals "the same objective"
 * — i.e. duplicates of each other, ignoring section, progress and completion.
 *
 * <p>Centralizes the per-type identity checks that the creation layer used to
 * inline in each {@code addX} duplicate guard, so the no-duplicates rule can be
 * enforced consistently <b>per namespace</b> (each section is its own bucket;
 * the built-in Incomplete + Completed pair is one shared "default" namespace).
 *
 * <p>Conditions mirror the original global duplicate guards exactly:
 * <ul>
 *   <li><b>SKILL</b> — same skill name + same target XP</li>
 *   <li><b>ITEM_GRIND</b> — same item id (any quantity)</li>
 *   <li><b>QUEST</b> — same quest name</li>
 *   <li><b>DIARY</b> — same area name + same tier description (case-insensitive)</li>
 *   <li><b>COMBAT_ACHIEVEMENT</b> — same CA task id, or same name (case-insensitive)</li>
 *   <li><b>ACCOUNT</b> — same metric + same target</li>
 *   <li><b>BOSS</b> — same boss name</li>
 *   <li><b>CUSTOM</b> — same name (case-insensitive)</li>
 * </ul>
 */
public final class GoalIdentity
{
	private GoalIdentity() {}

	/**
	 * True when two goals denote the same underlying objective. Section,
	 * progress, completion, tags and colour are ignored. Different types are
	 * never the same identity.
	 */
	public static boolean sameIdentity(Goal a, Goal b)
	{
		if (a == null || b == null || a.getType() != b.getType())
		{
			return false;
		}
		switch (a.getType())
		{
			case SKILL:
				return eq(a.getSkillName(), b.getSkillName())
					&& a.getTargetValue() == b.getTargetValue();
			case ITEM_GRIND:
				return a.getItemId() == b.getItemId();
			case QUEST:
				return eq(a.getQuestName(), b.getQuestName());
			case DIARY:
				return eqIgnoreCase(a.getName(), b.getName())
					&& eqIgnoreCase(a.getDescription(), b.getDescription());
			case COMBAT_ACHIEVEMENT:
				return (a.getCaTaskId() >= 0 && a.getCaTaskId() == b.getCaTaskId())
					|| eqIgnoreCase(a.getName(), b.getName());
			case ACCOUNT:
				return eq(a.getAccountMetric(), b.getAccountMetric())
					&& a.getTargetValue() == b.getTargetValue();
			case BOSS:
				return eq(a.getBossName(), b.getBossName());
			case CUSTOM:
				return eqIgnoreCase(a.getName(), b.getName());
			default:
				return false;
		}
	}

	private static boolean eq(String a, String b)
	{
		return a != null && a.equals(b);
	}

	private static boolean eqIgnoreCase(String a, String b)
	{
		return a != null && a.equalsIgnoreCase(b);
	}
}

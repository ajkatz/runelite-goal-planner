package com.goaltracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Goal
{
	@Builder.Default
	private String id = UUID.randomUUID().toString();

	private GoalType type;

	@Builder.Default
	private GoalStatus status = GoalStatus.ACTIVE;

	private String name;
	private String description;

	@Builder.Default
	private int priority = Integer.MAX_VALUE;

	// Progress
	private int targetValue;
	private int currentValue;

	// Type-specific references (nullable)
	private String skillName;     // For SKILL goals — matches net.runelite.api.Skill name
	private String questName;     // For QUEST goals — matches net.runelite.api.Quest name
	private String accountMetric; // For ACCOUNT goals — matches AccountMetric enum name
	private int varbitId;         // For DIARY/COMBAT_ACHIEVEMENT goals
	private int itemId;           // For ITEM_GRIND goals
	private int spriteId;         // Optional sprite icon (e.g. CA tier sword); 0 = unset
	private String tooltip;       // Optional hover tooltip text (e.g. CA full description)
	private String sectionId;     // Section this goal belongs to; null = unassigned (migrated on load)

	// For COMBAT_ACHIEVEMENT goals: the wiki / in-game CA task id. Used to look up
	// the bit-packed completion state from one of the 20 CA_TASK_COMPLETED varplayers.
	// Sentinel -1 = not set; tracker skips when negative.
	@Builder.Default
	private int caTaskId = -1;

	/**
	 * User-set background color override packed as 0xRRGGBB. -1 means "use the
	 * GoalType default color". Only custom goals can meaningfully set this
	 * (enforced by API) — other types have category-driven colors.
	 */
	@Builder.Default
	private int customColorRgb = -1;

	// Tag references — IDs into the GoalStore tag collection.
	// Tags themselves are first-class entities; goals only carry references.
	@Builder.Default
	private List<String> tagIds = new ArrayList<>();

	// Default tag id snapshot from creation, for "Restore Defaults"
	@Builder.Default
	private List<String> defaultTagIds = new ArrayList<>();

	// ---- Relations ----
	// Outgoing edges in the requires-DAG: IDs of other goals this one depends
	// on. "Horror from the Deep requires 35 Agility" → HFTD's requiredGoalIds
	// contains the Agility goal's id. Incoming edges ("required by") are NOT
	// stored — they're derived at query time by scanning all goals.
	//
	// Cross-section references ARE allowed. Cycles are rejected by
	// GoalStore.addRequirement; load-time cycle detection drops offending
	// edges rather than failing the load.
	@Builder.Default
	private List<String> requiredGoalIds = new ArrayList<>();

	/** True when this goal was created by the find-or-create requirement
	 *  flow as a seed (user didn't manually add it). Used by the absorption
	 *  rule to decide which goals it's allowed to consume when collapsing
	 *  same-skill goals in the same topo tier — user-added goals are
	 *  preserved regardless of absorbability. Default false. */
	@Builder.Default
	private boolean autoSeeded = false;

	// Integrations
	private String wikiUrl;
	private String inventorySetup;  // Inventory Setups loadout name

	// Metadata
	@Builder.Default
	private long createdAt = System.currentTimeMillis();
	private long completedAt;

	public double getProgressPercent()
	{
		if (targetValue <= 0)
		{
			return isComplete() ? 100.0 : 0.0;
		}
		return Math.max(0.0, Math.min(100.0, (currentValue * 100.0) / targetValue));
	}

	/**
	 * A goal is complete iff it has a non-zero completion timestamp.
	 * This is the canonical check used everywhere for completion state.
	 * The {@link GoalStatus#COMPLETE} value is kept in sync by setters but is no
	 * longer authoritative — completedAt is.
	 */
	public boolean isComplete()
	{
		return completedAt > 0;
	}

	/**
	 * Whether the goal's current value has reached or exceeded its target.
	 * Used by trackers to decide when to <em>set</em> the completion timestamp;
	 * separate from {@link #isComplete()} which is a state check.
	 */
	public boolean meetsTarget()
	{
		return targetValue > 0 && currentValue >= targetValue;
	}
}

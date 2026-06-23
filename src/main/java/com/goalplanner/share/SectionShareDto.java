package com.goalplanner.share;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * One section's worth of shared goals inside a {@link ShareBundle} (schema v2).
 * A v2 bundle carries a list of these, so one share code can import several
 * sections at once. Relation {@code ref} indices on {@link GoalShareDto} are
 * scoped to the section that contains them - edges BETWEEN sections travel on
 * the bundle's {@link ShareBundle#getCrossEdges() crossEdges} list instead.
 */
@Data
public class SectionShareDto
{
	/**
	 * Section display name; {@code null} for a loose-goals section (imported
	 * under a "Shared goals" name, like v1 {@code Kind.GOALS}).
	 */
	private String name;

	/** Section colour override (0xRRGGBB; -1 = default). */
	private int colorRgb = -1;

	/**
	 * When {@code true} the goals land in the recipient's DEFAULT plan
	 * (Incomplete/Completed built-ins) instead of a new user section, and
	 * existing equivalent goals are REUSED rather than duplicated - the same
	 * dedup the in-game Add Goal flow applies. {@link #name} is ignored.
	 */
	private boolean targetDefault;

	/** The shared goal definitions for this section. */
	private List<GoalShareDto> goals = new ArrayList<>();
}

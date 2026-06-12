package com.goalplanner.share;

import lombok.Data;

/**
 * One dependency edge between goals in two DIFFERENT sections of a
 * multi-section (v2) bundle. Within a section, relations ride on each
 * {@link GoalShareDto}'s section-scoped {@code requires}/{@code orRequires}
 * refs; edges that cross sections can't be expressed there, so the bundle
 * carries them in {@link ShareBundle#getCrossEdges()} as
 * (section index, ref) → (section index, ref) pairs. Section indices are
 * positions in the bundle's {@code sections} list; refs are the per-section
 * goal refs.
 */
@Data
public class CrossEdgeDto
{
	/** Index of the dependent goal's section in the bundle's section list. */
	private int fromSection;
	/** The dependent goal's ref within its section. */
	private int fromRef;
	/** Index of the prerequisite goal's section in the bundle's section list. */
	private int toSection;
	/** The prerequisite goal's ref within its section. */
	private int toRef;
	/** True for an OR (any-of) edge; false for a hard requirement. */
	private boolean or;
}

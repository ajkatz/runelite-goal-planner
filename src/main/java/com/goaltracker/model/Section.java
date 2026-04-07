package com.goaltracker.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * A user-facing section in the goal panel. Sections group goals into visual
 * buckets with their own header row.
 *
 * All sections — built-in and user-defined — use UUID identifiers. The
 * {@link #builtInKind} field identifies built-in sections (Incomplete / Completed)
 * without requiring magic string IDs in code; user sections have builtInKind = null.
 *
 * Ordering is explicit via {@link #order}. Built-ins have fixed relative positions:
 * Incomplete at {@link #ORDER_INCOMPLETE}, Completed at {@link #ORDER_COMPLETED}.
 * User sections can be placed between them.
 */
@Data
@Builder
public class Section
{
	/**
	 * Built-in section roles. User-defined sections use {@code null} for this field.
	 */
	public enum BuiltInKind
	{
		INCOMPLETE,
		COMPLETED
	}

	/**
	 * Order value for the Incomplete built-in section. Pinned just above
	 * Completed, so user-defined sections (1..N) render first, then
	 * Incomplete, then Completed.
	 */
	public static final int ORDER_INCOMPLETE = Integer.MAX_VALUE - 1;

	/** Order value for the Completed built-in section (always last). */
	public static final int ORDER_COMPLETED = Integer.MAX_VALUE;

	@Builder.Default
	private String id = UUID.randomUUID().toString();

	private String name;

	/** Lower values render first. */
	private int order;

	@Builder.Default
	private boolean collapsed = false;

	/** Non-null for built-in sections (Incomplete / Completed). Null for user sections. */
	private BuiltInKind builtInKind;

	public boolean isBuiltIn()
	{
		return builtInKind != null;
	}
}

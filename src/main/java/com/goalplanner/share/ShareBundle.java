package com.goalplanner.share;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Transport-neutral payload for sharing a section or a set of goals with
 * another player who runs the plugin. Carries goal <em>definitions</em> only —
 * recipient-specific state (ids, progress, completion, section membership) is
 * stripped on export and regenerated on import, so imported goals track
 * against the recipient's own account.
 *
 * <p>Serialized via {@link ShareCodec}. The {@link #v} schema version lets the
 * decoder reject payloads from an incompatible plugin version up front — the
 * persistence/share format may still change before a stable 1.0.
 */
@Data
public class ShareBundle
{
	/** Current share-format schema version. Bump on any breaking change. */
	public static final int SCHEMA_VERSION = 1;

	/** What the bundle represents. */
	public enum Kind
	{
		/** A whole section and its goals. */
		SECTION,
		/** A loose set of goals with no section identity. */
		GOALS
	}

	private int v = SCHEMA_VERSION;
	private Kind kind;

	/** Display name (RSN) of the sharer, shown in the import prompt. */
	private String sharedBy;

	/** Name of the shared section; null for {@link Kind#GOALS}. */
	private String sectionName;

	/** Section colour override (0xRRGGBB; -1 = default). Ignored for GOALS. */
	private int sectionColorRgb = -1;

	/** The shared goal definitions. Relations between them are encoded as
	 *  bundle-local {@code ref} indices on each {@link GoalShareDto}. */
	private List<GoalShareDto> goals = new ArrayList<>();
}

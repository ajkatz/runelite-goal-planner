package com.goalplanner.share;

/**
 * Wraps a {@link ShareCodec} share code in a human-readable, copy-paste-ready
 * invite line. The point is graceful degradation: a player <em>without</em> the
 * plugin who sees the pasted line (Discord, PM, in-game chat) reads a plain
 * "get the plugin to import this" instruction, while a player <em>with</em> the
 * plugin can import it — {@link ShareCodec#decode(String)} extracts the code
 * from the surrounding text.
 *
 * <p>{@link #MARKER} doubles as the detection token for in-game chat
 * auto-detection (a later transport).
 */
public final class ShareText
{
	/** Leading marker — human label + detection token for chat scanning. */
	public static final String MARKER = "[Goal Planner]";

	private ShareText()
	{
	}

	/**
	 * Build an invite line: {@code [Goal Planner] <who> shared <what> — get the
	 * Goal Planner RuneLite plugin to import it: <code>}.
	 *
	 * @param bundle the bundle being shared (for the sharer name + summary)
	 * @param code   the encoded share code from {@link ShareCodec#encode}
	 */
	public static String invite(ShareBundle bundle, String code)
	{
		String who = bundle != null && notBlank(bundle.getSharedBy())
			? bundle.getSharedBy().trim()
			: "Someone";
		return MARKER + " " + who + " shared " + describe(bundle)
			+ " — get the Goal Planner RuneLite plugin to import it: "
			+ (code != null ? code : "");
	}

	private static String describe(ShareBundle bundle)
	{
		int n = bundle != null && bundle.getGoals() != null ? bundle.getGoals().size() : 0;
		String goals = n == 1 ? "1 goal" : n + " goals";
		if (bundle != null
			&& bundle.getKind() == ShareBundle.Kind.SECTION
			&& notBlank(bundle.getSectionName()))
		{
			return "\"" + bundle.getSectionName().trim() + "\" (" + goals + ")";
		}
		return goals;
	}

	private static boolean notBlank(String s)
	{
		return s != null && !s.trim().isEmpty();
	}
}

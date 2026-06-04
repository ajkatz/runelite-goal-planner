package com.goalplanner.api;

/**
 * Public DTO for a section. No internal model classes referenced.
 */
public class SectionView
{
	public String id;
	public String name;
	public int order;
	public boolean collapsed;
	public boolean builtIn;
	/**
	 * Built-in section role: "INCOMPLETE", "COMPLETED", or {@code null} for user-defined sections.
	 */
	public String kind;
	/**
	 * Current section color — user override if set, else the neutral default.
	 * Packed as 0xRRGGBB.
	 */
	public int colorRgb;
	/**
	 * The neutral default color, even when an override is set. Lets the UI
	 * preview what "reset to default" would revert to.
	 */
	public int defaultColorRgb;
	/** True when {@link #colorRgb} is a user override. */
	public boolean colorOverridden;
	/**
	 * Per-section completed-archiving override: {@code null} = inherit the global
	 * default; {@code TRUE} = always archive to Completed; {@code FALSE} = always
	 * keep inline. Always null for built-ins.
	 */
	public Boolean autoArchiveOverride;
	/**
	 * Per-section override for the nested ("Indent dependencies") view:
	 * {@code null} = follow the global default, {@code TRUE} = always nested,
	 * {@code FALSE} = never nested. View preference only — no effect on goal data.
	 */
	public Boolean nestedOverride;
}

package com.goalplanner.ui;

import com.goalplanner.PanelFontFamily;
import com.goalplanner.PanelFontScale;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Central resolver for Goal Planner panel fonts so the family + size scale chosen
 * in config apply everywhere through one path. Call sites use
 * {@link #derive(int, float)} in place of {@code component.getFont().deriveFont(...)},
 * which previously pinned both family and size.
 *
 * <p>Configured once at start-up and again whenever the relevant config changes
 * (see {@code GoalPlannerPlugin}); the panel is rebuilt afterwards so the new
 * fonts take effect. All reads/writes happen on the Swing EDT; the fields are
 * {@code volatile} as a cheap guard regardless.
 */
public final class PanelFonts
{
	private PanelFonts()
	{
	}

	// Store the CHOICE, not a frozen Font. The base font is resolved live on every
	// derive() call so DEFAULT always tracks the look-and-feel's current Label.font -
	// exactly like the old per-call {@code component.getFont().deriveFont(...)}. A
	// font frozen at configure()-time could capture a not-yet-themed L&F and leave
	// every panel label stuck on the wrong face.
	private static volatile PanelFontFamily family = PanelFontFamily.DEFAULT;
	private static volatile float scale = 1.0f;
	// Bumped on every configure() so callers that cache rendered components (e.g.
	// the GoalPanel card-reuse signature) can detect a font change and rebuild -
	// a reused card keeps the font it was constructed with otherwise.
	private static volatile int generation = 0;

	/** Apply the configured family + size scale. Safe to call repeatedly. */
	public static void configure(PanelFontFamily fontFamily, PanelFontScale fontScale)
	{
		family = fontFamily != null ? fontFamily : PanelFontFamily.DEFAULT;
		scale = fontScale.getMultiplier();
		generation++;
	}

	/** Monotonic token that changes whenever the configured font changes. */
	public static int generation()
	{
		return generation;
	}

	/** The configured size multiplier - so fixed-pixel layouts (card heights,
	 *  icon sizes) can grow with the font instead of clipping larger text. */
	public static float scale()
	{
		return scale;
	}

	/** Font at {@code size} (scaled) in the given {@link Font} style. */
	public static Font derive(int style, float size)
	{
		return resolveBase(family).deriveFont(style, size * scale);
	}

	/** Plain font at {@code size} (scaled). */
	public static Font derive(float size)
	{
		return derive(Font.PLAIN, size);
	}

	/** Restyle at the base size (scaled) - for call sites that only changed style. */
	public static Font derive(int style)
	{
		Font base = resolveBase(family);
		return base.deriveFont(style, base.getSize2D() * scale);
	}

	private static Font resolveBase(PanelFontFamily family)
	{
		switch (family)
		{
			case SANS_SERIF:
				return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			case SERIF:
				return new Font(Font.SERIF, Font.PLAIN, 12);
			case DEFAULT:
			default:
				// The look-and-feel's base label font (FlatLaf), with a sane fallback.
				Font label = UIManager.getFont("Label.font");
				return label != null ? label : new Font(Font.DIALOG, Font.PLAIN, 12);
		}
	}
}

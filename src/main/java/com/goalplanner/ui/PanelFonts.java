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

	private static volatile Font base = resolveBase(PanelFontFamily.DEFAULT);
	private static volatile float scale = 1.0f;

	/** Apply the configured family + size scale. Safe to call repeatedly. */
	public static void configure(PanelFontFamily family, PanelFontScale fontScale)
	{
		base = resolveBase(family);
		scale = fontScale.getMultiplier();
	}

	/** Font at {@code size} (scaled) in the given {@link Font} style. */
	public static Font derive(int style, float size)
	{
		return base.deriveFont(style, size * scale);
	}

	/** Plain font at {@code size} (scaled). */
	public static Font derive(float size)
	{
		return derive(Font.PLAIN, size);
	}

	/** Restyle at the base size (scaled) - for call sites that only changed style. */
	public static Font derive(int style)
	{
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

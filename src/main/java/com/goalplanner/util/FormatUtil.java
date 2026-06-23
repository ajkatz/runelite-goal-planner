package com.goalplanner.util;

/**
 * Shared formatting utilities used across UI components.
 */
public final class FormatUtil
{
	private FormatUtil() {}

	/**
	 * Format a number in shortened form: 1.2M, 50K, 999.
	 */
	public static String formatNumber(int n)
	{
		if (n >= 1_000_000)
		{
			return String.format("%.1fM", n / 1_000_000.0);
		}
		if (n >= 1_000)
		{
			return String.format("%.0fK", n / 1_000.0);
		}
		return String.valueOf(n);
	}

	/**
	 * Format a number with commas: 1,234,567.
	 */
	public static String formatXp(int xp)
	{
		return String.format("%,d", xp);
	}

	/**
	 * Truncate text with ellipsis if too long.
	 */
	public static String truncate(String text, int maxLen)
	{
		if (text == null) return "";
		if (text.length() <= maxLen) return text;
		// Reserve 3 chars for the ASCII "..." so the result stays within maxLen
		// (a 1-char "…" would render as tofu on macOS Tahoe — see check_glyphs).
		if (maxLen <= 3) return text.substring(0, maxLen);
		return text.substring(0, maxLen - 3) + "...";
	}

	/**
	 * Escape HTML special characters.
	 */
	public static String escapeHtml(String text)
	{
		if (text == null) return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}

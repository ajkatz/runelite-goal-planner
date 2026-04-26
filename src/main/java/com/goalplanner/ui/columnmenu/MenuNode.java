package com.goalplanner.ui.columnmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tree node for the {@link ColumnMenu} prototype. Three flavors:
 * <ul>
 *   <li>Leaf: has an action, runs on click and dismisses the menu.</li>
 *   <li>Submenu: has children, opens a column to the right on click.</li>
 *   <li>Separator: a thin horizontal divider (no action, no children).</li>
 * </ul>
 *
 * <p>Built via the static factories — keeps the menu construction
 * read like a flat list rather than a tree of constructor calls.
 */
public final class MenuNode
{
	public final String label;
	public final Runnable action;
	public final List<MenuNode> children;
	public final boolean separator;
	public final boolean enabled;
	public final boolean keepOpen;
	public final String tooltip;

	private MenuNode(String label, Runnable action, List<MenuNode> children,
					 boolean separator, boolean enabled, boolean keepOpen, String tooltip)
	{
		this.label = label;
		this.action = action;
		this.children = children == null ? Collections.emptyList() : children;
		this.separator = separator;
		this.enabled = enabled;
		this.keepOpen = keepOpen;
		this.tooltip = tooltip;
	}

	public static MenuNode leaf(String label, Runnable action)
	{
		return new MenuNode(label, action, null, false, true, false, null);
	}

	/**
	 * Leaf that runs its action but does NOT dismiss the menu — for
	 * repeat-friendly actions like Move Up / Move Down / Move to Top /
	 * Move to Bottom where the user typically clicks several times in a
	 * row to nudge the selection into place.
	 */
	public static MenuNode leafStaysOpen(String label, Runnable action)
	{
		return new MenuNode(label, action, null, false, true, true, null);
	}

	public static MenuNode leafDisabled(String label)
	{
		return new MenuNode(label, null, null, false, false, false, null);
	}

	public static MenuNode submenu(String label, List<MenuNode> children)
	{
		return new MenuNode(label, null, children, false, true, false, null);
	}

	public static MenuNode separator()
	{
		return new MenuNode(null, null, null, true, false, false, null);
	}

	public MenuNode withTooltip(String tip)
	{
		return new MenuNode(label, action, children, separator, enabled, keepOpen, tip);
	}

	public boolean isLeaf() { return !separator && action != null; }
	public boolean isSubmenu() { return !separator && !children.isEmpty() && action == null; }

	/** Convenience builder for a flat list of items — wraps in an ArrayList so callers can append. */
	public static List<MenuNode> list()
	{
		return new ArrayList<>();
	}
}

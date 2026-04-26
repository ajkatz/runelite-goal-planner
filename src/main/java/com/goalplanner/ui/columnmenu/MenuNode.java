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
	public final String tooltip;

	private MenuNode(String label, Runnable action, List<MenuNode> children,
					 boolean separator, boolean enabled, String tooltip)
	{
		this.label = label;
		this.action = action;
		this.children = children == null ? Collections.emptyList() : children;
		this.separator = separator;
		this.enabled = enabled;
		this.tooltip = tooltip;
	}

	public static MenuNode leaf(String label, Runnable action)
	{
		return new MenuNode(label, action, null, false, true, null);
	}

	public static MenuNode leafDisabled(String label)
	{
		return new MenuNode(label, null, null, false, false, null);
	}

	public static MenuNode submenu(String label, List<MenuNode> children)
	{
		return new MenuNode(label, null, children, false, true, null);
	}

	public static MenuNode separator()
	{
		return new MenuNode(null, null, null, true, false, null);
	}

	public MenuNode withTooltip(String tip)
	{
		return new MenuNode(label, action, children, separator, enabled, tip);
	}

	public boolean isLeaf() { return !separator && action != null; }
	public boolean isSubmenu() { return !separator && !children.isEmpty() && action == null; }

	/** Convenience builder for a flat list of items — wraps in an ArrayList so callers can append. */
	public static List<MenuNode> list()
	{
		return new ArrayList<>();
	}
}

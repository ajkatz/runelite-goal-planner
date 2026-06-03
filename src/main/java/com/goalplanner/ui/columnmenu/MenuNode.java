package com.goalplanner.ui.columnmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tree node for the {@link ColumnMenu}. Flavors:
 * <ul>
 *   <li>Leaf: has an action, runs on click and dismisses the menu.</li>
 *   <li>Submenu: has children, opens a column to the right on click.</li>
 *   <li>Separator: a thin horizontal divider (no action, no children).</li>
 * </ul>
 *
 * <p>A leaf may also be <b>checkable</b> — part of a selectable group, which
 * reserves a left gutter; the currently-selected one is rendered with a dot.
 *
 * <p>Built via the static factories — keeps the menu construction read like a
 * flat list rather than a tree of constructor calls.
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
	/** True when this leaf belongs to a selectable group (reserves a left dot gutter). */
	public final boolean checkable;
	/** True when this checkable leaf is the current selection (shown with a dot). */
	public final boolean checked;

	private MenuNode(String label, Runnable action, List<MenuNode> children,
					 boolean separator, boolean enabled, boolean keepOpen, String tooltip,
					 boolean checkable, boolean checked)
	{
		this.label = label;
		this.action = action;
		this.children = children == null ? Collections.emptyList() : children;
		this.separator = separator;
		this.enabled = enabled;
		this.keepOpen = keepOpen;
		this.tooltip = tooltip;
		this.checkable = checkable;
		this.checked = checked;
	}

	public static MenuNode leaf(String label, Runnable action)
	{
		return new MenuNode(label, action, null, false, true, false, null, false, false);
	}

	/**
	 * Leaf that runs its action but does NOT dismiss the menu — for
	 * repeat-friendly actions like Move Up / Move Down / Move to Top /
	 * Move to Bottom where the user typically clicks several times in a
	 * row to nudge the selection into place.
	 */
	public static MenuNode leafStaysOpen(String label, Runnable action)
	{
		return new MenuNode(label, action, null, false, true, true, null, false, false);
	}

	public static MenuNode leafDisabled(String label)
	{
		return new MenuNode(label, null, null, false, false, false, null, false, false);
	}

	public static MenuNode submenu(String label, List<MenuNode> children)
	{
		return new MenuNode(label, null, children, false, true, false, null, false, false);
	}

	public static MenuNode separator()
	{
		return new MenuNode(null, null, null, true, false, false, null, false, false);
	}

	public MenuNode withTooltip(String tip)
	{
		return new MenuNode(label, action, children, separator, enabled, keepOpen, tip, checkable, checked);
	}

	/** Mark this leaf as part of a selectable group (reserves the dot gutter), optionally selected. */
	public MenuNode withCheck(boolean checkableFlag, boolean checkedFlag)
	{
		return new MenuNode(label, action, children, separator, enabled, keepOpen, tooltip, checkableFlag, checkedFlag);
	}

	public boolean isLeaf() { return !separator && action != null; }
	public boolean isSubmenu() { return !separator && !children.isEmpty() && action == null; }

	/** Convenience builder for a flat list of items — wraps in an ArrayList so callers can append. */
	public static List<MenuNode> list()
	{
		return new ArrayList<>();
	}
}

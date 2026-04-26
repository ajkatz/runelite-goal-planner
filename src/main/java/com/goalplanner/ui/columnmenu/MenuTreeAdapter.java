package com.goalplanner.ui.columnmenu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts an already-built {@link JPopupMenu} (or a {@link JMenu}
 * subtree) into a {@link MenuNode} tree the {@link ColumnMenu} can
 * render. Lets us reuse the existing buildBulkMenu construction
 * code without porting the menu-build logic to the new model.
 *
 * <p>Each leaf {@link JMenuItem}'s action listeners are wrapped in a
 * single Runnable that fires them with a synthesized ActionEvent —
 * preserves the existing menu's behavior verbatim.
 */
public final class MenuTreeAdapter
{
	private MenuTreeAdapter() {}

	public static List<MenuNode> fromPopup(JPopupMenu popup)
	{
		List<MenuNode> out = new ArrayList<>();
		for (int i = 0; i < popup.getComponentCount(); i++)
		{
			MenuNode n = fromComponent(popup.getComponent(i));
			if (n != null) out.add(n);
		}
		return out;
	}

	private static List<MenuNode> fromMenu(JMenu menu)
	{
		List<MenuNode> out = new ArrayList<>();
		for (Component c : menu.getMenuComponents())
		{
			MenuNode n = fromComponent(c);
			if (n != null) out.add(n);
		}
		return out;
	}

	private static MenuNode fromComponent(Component c)
	{
		if (c instanceof JPopupMenu.Separator || c instanceof javax.swing.JSeparator)
		{
			return MenuNode.separator();
		}
		if (c instanceof JMenu)
		{
			JMenu menu = (JMenu) c;
			MenuNode node = MenuNode.submenu(menu.getText(), fromMenu(menu));
			return menu.getToolTipText() != null
				? node.withTooltip(menu.getToolTipText()) : node;
		}
		if (c instanceof JMenuItem)
		{
			JMenuItem item = (JMenuItem) c;
			if (!item.isEnabled())
			{
				MenuNode disabled = MenuNode.leafDisabled(item.getText());
				return item.getToolTipText() != null
					? disabled.withTooltip(item.getToolTipText()) : disabled;
			}
			ActionListener[] listeners = item.getActionListeners();
			Runnable action = () -> {
				ActionEvent ev = new ActionEvent(
					item, ActionEvent.ACTION_PERFORMED, item.getActionCommand());
				for (ActionListener l : listeners) l.actionPerformed(ev);
			};
			MenuNode leaf = MenuNode.leaf(item.getText(), action);
			return item.getToolTipText() != null
				? leaf.withTooltip(item.getToolTipText()) : leaf;
		}
		return null;
	}
}

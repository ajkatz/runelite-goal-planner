package com.goalplanner.ui.columnmenu;

import net.runelite.client.ui.ColorScheme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Click-driven, hover-stable replacement for cascading {@link javax.swing.JPopupMenu}.
 *
 * <p>Renders nested menu trees as side-by-side columns inside a single
 * {@link JWindow}. Drilling into a submenu appends a column to the right
 * rather than spawning a hover-collapsing popup. Clicking a leaf runs its
 * action and dismisses the whole window. Clicking outside dismisses.
 *
 * <p>The window is non-focusable so the host application (RuneLite) keeps
 * keyboard input — important for in-game gameplay.
 */
public final class ColumnMenu
{
	private static final int COLUMN_WIDTH = 220;
	private static final int ROW_HEIGHT = 24;
	private static final int MAX_COLUMN_HEIGHT = 480;

	private static final Color BG = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BG_HOVER = brighten(BG, 18);
	private static final Color FG = new Color(0xE0, 0xE0, 0xE0);
	private static final Color FG_DISABLED = new Color(0x80, 0x80, 0x80);
	private static final Color SEP = new Color(0x40, 0x40, 0x40);
	private static final Color BORDER = new Color(0x30, 0x30, 0x30);

	private final JWindow window;
	private final JPanel root;
	private final List<JPanel> columns = new ArrayList<>();
	private AWTEventListener globalListener;

	public static void show(Component anchor, int x, int y, List<MenuNode> rootItems)
	{
		ColumnMenu m = new ColumnMenu(anchor);
		m.openColumn(0, rootItems);
		m.position(anchor, x, y);
		m.window.setVisible(true);
		m.attachGlobalListeners();
	}

	private ColumnMenu(Component anchor)
	{
		Window owner = SwingUtilities.getWindowAncestor(anchor);
		window = new JWindow(owner);
		window.setFocusableWindowState(false);
		window.setAlwaysOnTop(true);
		root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
		root.setBackground(BG);
		root.setBorder(BorderFactory.createLineBorder(BORDER, 1));
		window.setContentPane(root);
	}

	/**
	 * Show a column at {@code level}, replacing any columns at or beyond
	 * that depth. Used for both initial rendering and when the user
	 * picks a sibling in an upstream column (collapses the trailing
	 * columns and opens the new one).
	 */
	private void openColumn(int level, List<MenuNode> items)
	{
		while (columns.size() > level)
		{
			JPanel removed = columns.remove(columns.size() - 1);
			root.remove(removed);
		}
		JPanel column = buildColumn(items, level);
		columns.add(column);
		root.add(column);
		window.pack();
		repositionIfOffscreen();
	}

	private JPanel buildColumn(List<MenuNode> items, int level)
	{
		JPanel inner = new JPanel();
		inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
		inner.setBackground(BG);
		inner.setBorder(new EmptyBorder(4, 0, 4, 0));

		for (MenuNode node : items)
		{
			if (node.separator)
			{
				JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
				sep.setForeground(SEP);
				sep.setBackground(SEP);
				sep.setMaximumSize(new Dimension(COLUMN_WIDTH, 1));
				sep.setBorder(new EmptyBorder(2, 0, 2, 0));
				inner.add(sep);
			}
			else
			{
				inner.add(buildRow(node, level));
			}
		}

		// Wrap in a scrollpane so very long lists (Move to Section,
		// Remove Requirement on a heavy graph) don't overflow the screen.
		JScrollPane scroll = new JScrollPane(inner);
		scroll.setBorder(level == 0 ? null
			: BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(BG);
		scroll.setBackground(BG);
		int desiredHeight = Math.min(
			items.size() * ROW_HEIGHT + 12,
			MAX_COLUMN_HEIGHT);
		scroll.setPreferredSize(new Dimension(COLUMN_WIDTH, desiredHeight));
		scroll.setMaximumSize(new Dimension(COLUMN_WIDTH, MAX_COLUMN_HEIGHT));

		// Wrap-of-wrap so the column itself behaves as a fixed-width box
		// in the parent X_AXIS layout.
		JPanel column = new JPanel(new BorderLayout());
		column.setBackground(BG);
		column.add(scroll, BorderLayout.CENTER);
		column.setMaximumSize(new Dimension(COLUMN_WIDTH, MAX_COLUMN_HEIGHT));
		return column;
	}

	private JPanel buildRow(MenuNode node, int level)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(BG);
		row.setBorder(new EmptyBorder(4, 10, 4, 10));
		row.setMaximumSize(new Dimension(COLUMN_WIDTH, ROW_HEIGHT));

		JLabel label = new JLabel(node.label);
		label.setForeground(node.enabled ? FG : FG_DISABLED);
		label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
		row.add(label, BorderLayout.WEST);

		if (node.isSubmenu())
		{
			JLabel arrow = new JLabel("▸"); // ▸
			arrow.setForeground(node.enabled ? FG_DISABLED : SEP);
			arrow.setFont(arrow.getFont().deriveFont(Font.PLAIN, 10f));
			row.add(arrow, BorderLayout.EAST);
		}

		if (node.tooltip != null) row.setToolTipText(node.tooltip);

		if (node.enabled && !node.separator)
		{
			row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			row.addMouseListener(new MouseAdapter()
			{
				@Override public void mouseEntered(MouseEvent e) { row.setBackground(BG_HOVER); }
				@Override public void mouseExited(MouseEvent e) { row.setBackground(BG); }
				@Override public void mouseClicked(MouseEvent e)
				{
					if (e.getButton() != MouseEvent.BUTTON1) return;
					if (node.isLeaf())
					{
						Runnable action = node.action;
						close();
						if (action != null) action.run();
					}
					else if (node.isSubmenu())
					{
						openColumn(level + 1, node.children);
					}
				}
			});
		}

		return row;
	}

	private void position(Component anchor, int x, int y)
	{
		Point screen = anchor.getLocationOnScreen();
		window.setLocation(screen.x + x, screen.y + y);
	}

	/**
	 * Keep the window inside the visible screen bounds. When the
	 * cumulative columns extend past the right edge, shift left.
	 */
	private void repositionIfOffscreen()
	{
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
		Rectangle bounds = window.getBounds();
		int x = bounds.x;
		int y = bounds.y;
		if (bounds.x + bounds.width > screen.x + screen.width)
		{
			x = Math.max(screen.x, screen.x + screen.width - bounds.width);
		}
		if (bounds.y + bounds.height > screen.y + screen.height)
		{
			y = Math.max(screen.y, screen.y + screen.height - bounds.height);
		}
		if (x != bounds.x || y != bounds.y) window.setLocation(x, y);
	}

	private void attachGlobalListeners()
	{
		globalListener = event -> {
			if (event instanceof MouseEvent)
			{
				MouseEvent me = (MouseEvent) event;
				if (me.getID() == MouseEvent.MOUSE_PRESSED)
				{
					Point p = me.getLocationOnScreen();
					if (!window.getBounds().contains(p)) close();
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(
			globalListener, AWTEvent.MOUSE_EVENT_MASK);
	}

	private void close()
	{
		if (globalListener != null)
		{
			Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
			globalListener = null;
		}
		window.dispose();
	}

	private static Color brighten(Color c, int amount)
	{
		return new Color(
			Math.min(255, c.getRed() + amount),
			Math.min(255, c.getGreen() + amount),
			Math.min(255, c.getBlue() + amount));
	}

}

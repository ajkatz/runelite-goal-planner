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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Click-driven, hover-stable replacement for cascading {@link javax.swing.JPopupMenu}.
 *
 * <p>Renders the menu tree as a single column inside a non-focusable
 * {@link JWindow}. Drilling into a submenu replaces the current column
 * with the submenu's items and adds a "← &lt;parent&gt;" back row at
 * the top. Hitting that row pops back to the previous level. Clicking
 * a leaf runs the action and dismisses. Clicking outside dismisses.
 *
 * <p>Window is non-focusable so the host application (RuneLite) keeps
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
	private static final Color FG_DIM = new Color(0xA0, 0xA0, 0xA0);
	private static final Color FG_DISABLED = new Color(0x80, 0x80, 0x80);
	private static final Color SEP = new Color(0x40, 0x40, 0x40);
	private static final Color BORDER = new Color(0x30, 0x30, 0x30);

	private final JWindow window;
	private final JPanel root;
	private final Deque<Frame> stack = new ArrayDeque<>();
	private AWTEventListener globalListener;

	/** A drill-down level: which submenu items to render and what to label
	 *  the back row when navigating up from here. */
	private static final class Frame
	{
		final String parentLabel; // null for root
		final List<MenuNode> items;
		Frame(String parentLabel, List<MenuNode> items)
		{
			this.parentLabel = parentLabel;
			this.items = items;
		}
	}

	public static void show(Component anchor, int x, int y, List<MenuNode> rootItems)
	{
		ColumnMenu m = new ColumnMenu(anchor);
		m.stack.push(new Frame(null, rootItems));
		m.renderCurrent();
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
		root = new JPanel(new BorderLayout());
		root.setBackground(BG);
		root.setBorder(BorderFactory.createLineBorder(BORDER, 1));
		window.setContentPane(root);
	}

	/**
	 * Replace the visible column with the top-of-stack frame's items.
	 * If we're not at the root, prepend a "← &lt;parentLabel&gt;" row that
	 * pops back to the prior frame on click.
	 */
	private void renderCurrent()
	{
		root.removeAll();
		Frame frame = stack.peek();
		if (frame == null) return;

		JPanel inner = new JPanel();
		inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
		inner.setBackground(BG);
		inner.setBorder(new EmptyBorder(4, 0, 4, 0));

		boolean hasBack = stack.size() > 1;
		if (hasBack)
		{
			inner.add(buildBackRow(frame.parentLabel));
			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setForeground(SEP);
			sep.setBackground(SEP);
			sep.setMaximumSize(new Dimension(COLUMN_WIDTH, 1));
			sep.setBorder(new EmptyBorder(2, 0, 2, 0));
			inner.add(sep);
		}

		for (MenuNode node : frame.items)
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
				inner.add(buildRow(node));
			}
		}

		JScrollPane scroll = new JScrollPane(inner);
		scroll.setBorder(null);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(BG);
		scroll.setBackground(BG);

		int rowCount = frame.items.size() + (hasBack ? 2 : 0); // back row + separator
		int desired = Math.min(rowCount * ROW_HEIGHT + 12, MAX_COLUMN_HEIGHT);
		scroll.setPreferredSize(new Dimension(COLUMN_WIDTH, desired));

		root.add(scroll, BorderLayout.CENTER);
		window.pack();
		repositionIfOffscreen();
	}

	private JPanel buildBackRow(String parentLabel)
	{
		String label = parentLabel != null ? parentLabel : "Back";
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(BG);
		row.setBorder(new EmptyBorder(4, 10, 4, 10));
		row.setMaximumSize(new Dimension(COLUMN_WIDTH, ROW_HEIGHT));

		JLabel arrow = new JLabel("←");
		arrow.setForeground(FG_DIM);
		arrow.setFont(arrow.getFont().deriveFont(Font.PLAIN, 12f));
		arrow.setBorder(new EmptyBorder(0, 0, 0, 6));
		row.add(arrow, BorderLayout.WEST);

		JLabel text = new JLabel(label);
		text.setForeground(FG_DIM);
		text.setFont(text.getFont().deriveFont(Font.PLAIN, 12f));
		row.add(text, BorderLayout.CENTER);

		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		row.addMouseListener(new MouseAdapter()
		{
			@Override public void mouseEntered(MouseEvent e) { row.setBackground(BG_HOVER); }
			@Override public void mouseExited(MouseEvent e) { row.setBackground(BG); }
			@Override public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1) return;
				stack.pop();
				renderCurrent();
			}
		});
		return row;
	}

	private JPanel buildRow(MenuNode node)
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
			JLabel arrow = new JLabel(">");
			arrow.setForeground(node.enabled ? FG_DIM : SEP);
			arrow.setFont(arrow.getFont().deriveFont(Font.BOLD, 12f));
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
						stack.push(new Frame(node.label, node.children));
						renderCurrent();
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

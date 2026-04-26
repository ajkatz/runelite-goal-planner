package com.goalplanner.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modal dialog that presents a list of items as checkboxes plus a
 * Submit/Cancel button row. Used for Remove Requirement / Remove
 * Dependent flows where the user benefits from picking several edges
 * to drop in one gesture rather than clicking a submenu item per edge.
 *
 * <p>Strictly a picker — returns the selected ids; callers are
 * responsible for the actual mutation and any compound-undo wrapping.
 */
public final class MultiSelectDialog
{
	private MultiSelectDialog() {}

	/**
	 * Each item exposes a stable id for the result and a display label.
	 */
	public static final class Item
	{
		public final String id;
		public final String label;

		public Item(String id, String label)
		{
			this.id = id;
			this.label = label;
		}
	}

	/**
	 * Show the dialog modally. Returns the ids of every checkbox the
	 * user ticked, in input order. Returns an empty list on cancel /
	 * close. Returns an empty list when {@code items} is empty without
	 * even showing the dialog.
	 *
	 * @param submitLabel button label, e.g. "Remove" or "Apply"
	 */
	public static List<String> show(Component parent, String title, String submitLabel,
									List<Item> items)
	{
		if (items == null || items.isEmpty()) return Collections.emptyList();

		Window owner = SwingUtilities.getWindowAncestor(parent);
		Frame frame = owner instanceof Frame ? (Frame) owner : null;
		JDialog dialog = new JDialog(frame, title, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel root = new JPanel(new BorderLayout(0, 8));
		root.setBackground(ColorScheme.DARK_GRAY_COLOR);
		root.setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel list = new JPanel();
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.setBackground(ColorScheme.DARK_GRAY_COLOR);

		List<JCheckBox> checkboxes = new ArrayList<>(items.size());
		for (Item item : items)
		{
			JCheckBox cb = new JCheckBox(item.label);
			cb.setBackground(ColorScheme.DARK_GRAY_COLOR);
			cb.setForeground(new java.awt.Color(0xE0, 0xE0, 0xE0));
			cb.setFocusPainted(false);
			cb.setBorder(new EmptyBorder(2, 0, 2, 0));
			cb.setOpaque(true);
			list.add(cb);
			checkboxes.add(cb);
		}

		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0x30, 0x30, 0x30), 1));
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
		// Cap height so a goal with many edges doesn't push the dialog
		// off-screen.
		int rowHeight = 22;
		int desired = Math.min(items.size() * rowHeight + 8, 320);
		scroll.setPreferredSize(new Dimension(280, desired));

		root.add(scroll, BorderLayout.CENTER);

		List<String> chosen = new ArrayList<>();
		JButton submit = new JButton(submitLabel);
		submit.addActionListener(e -> {
			for (int i = 0; i < checkboxes.size(); i++)
			{
				if (checkboxes.get(i).isSelected()) chosen.add(items.get(i).id);
			}
			dialog.dispose();
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> dialog.dispose());

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttons.add(cancel);
		buttons.add(submit);

		// Header label — concise, no period; the title bar already
		// communicates the action.
		JLabel header = new JLabel("Select items to " + submitLabel.toLowerCase() + ":");
		header.setForeground(new java.awt.Color(0xE0, 0xE0, 0xE0));

		JPanel north = new JPanel();
		north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
		north.setBackground(ColorScheme.DARK_GRAY_COLOR);
		north.add(header);
		north.add(Box.createVerticalStrut(6));
		root.add(north, BorderLayout.NORTH);
		root.add(buttons, BorderLayout.SOUTH);

		dialog.setContentPane(root);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.getRootPane().setDefaultButton(submit);
		dialog.setVisible(true);

		return chosen;
	}
}

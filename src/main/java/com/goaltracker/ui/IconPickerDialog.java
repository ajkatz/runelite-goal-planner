package com.goaltracker.ui;

import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.http.api.item.ItemPrice;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal icon picker dialog. Shows two grids:
 * <ul>
 *   <li>Skill icons — one per {@link Skill} enum value, rendered via
 *       {@link SkillIconManager}. Selecting one returns the skill enum name
 *       (e.g. "ATTACK") as the iconKey.</li>
 *   <li>Bundled icons — anything in {@code src/main/resources/icons/*.png}.
 *       Selecting one returns the filename without extension as the iconKey.
 *       Currently the project ships zero bundled icons; this section shows a
 *       hint until icons are added.</li>
 * </ul>
 *
 * <p>The "Clear icon" button at the bottom returns null (caller should
 * interpret as "remove icon, render as colored pill"). Cancelling returns
 * the original iconKey unchanged.
 *
 * <p>Usage: {@code String picked = IconPickerDialog.show(parent, currentKey, skillIconManager)}.
 * Returns the new iconKey (possibly null), or {@code currentKey} on cancel.
 */
public class IconPickerDialog extends JDialog
{
	private static final int CELL = 36;
	private static final int ICON_PX = 22;

	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;
	private String result;
	private boolean cancelled = true;
	private JPanel itemResultsGrid;

	private IconPickerDialog(java.awt.Frame owner, String currentKey,
		SkillIconManager skillIconManager, ItemManager itemManager)
	{
		super(owner, "Pick an icon", true);
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;
		this.result = currentKey;

		setLayout(new BorderLayout());
		setSize(480, 520);
		setLocationRelativeTo(owner);

		JLabel header = new JLabel("Pick an icon", SwingConstants.LEFT);
		header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
		header.setBorder(new EmptyBorder(10, 12, 8, 12));
		add(header, BorderLayout.NORTH);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(new EmptyBorder(4, 8, 8, 8));

		// Items section — search OSRS items by name and pick from results.
		// Powered by ItemManager.search which returns up to 250 ItemPrices.
		// Skills section removed: system skill tags already carry their icons
		// from the seed, and users can only icon non-system tags, so the
		// skill grid was unreachable in practice.
		JLabel itemsHeader = new JLabel("Items (search by name)");
		itemsHeader.setFont(itemsHeader.getFont().deriveFont(Font.BOLD));
		itemsHeader.setBorder(new EmptyBorder(4, 4, 4, 4));
		itemsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(itemsHeader);
		content.add(buildItemSearch());

		content.add(Box.createVerticalStrut(12));

		// Bundled icons section
		JLabel bundledHeader = new JLabel("Bundled");
		bundledHeader.setFont(bundledHeader.getFont().deriveFont(Font.BOLD));
		bundledHeader.setBorder(new EmptyBorder(4, 4, 4, 4));
		bundledHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(bundledHeader);
		content.add(buildBundledGrid());

		JScrollPane scroll = new JScrollPane(content);
		scroll.setBorder(null);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);

		// Bottom action row
		JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
		actions.setBorder(new EmptyBorder(4, 8, 8, 8));

		JButton clear = new JButton("Clear icon");
		clear.addActionListener(e -> { result = null; cancelled = false; dispose(); });
		actions.add(clear);

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> dispose());
		actions.add(cancel);

		add(actions, BorderLayout.SOUTH);
	}

	private JPanel buildItemSearch()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Narrow content width — the dialog is wider for breathing room but
		// the actual widgets stay compact on the left.
		final int contentWidth = 280;

		// Search row: text input + Search button
		JPanel searchRow = new JPanel(new BorderLayout(4, 0));
		searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchRow.setBorder(new EmptyBorder(4, 4, 4, 4));
		searchRow.setMaximumSize(new Dimension(contentWidth, 28));
		searchRow.setPreferredSize(new Dimension(contentWidth, 28));

		final javax.swing.JTextField searchField = new javax.swing.JTextField();
		searchRow.add(searchField, BorderLayout.CENTER);

		JButton searchBtn = new JButton("Search");
		searchBtn.setMargin(new java.awt.Insets(2, 8, 2, 8));
		searchRow.add(searchBtn, BorderLayout.EAST);

		container.add(searchRow);

		// Results grid (populated on search). Constrain max width so it stays
		// compact on the left of the wider dialog instead of stretching.
		itemResultsGrid = new JPanel(new GridLayout(0, 4, 4, 4));
		itemResultsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
		itemResultsGrid.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
		container.add(itemResultsGrid);

		// Force the container itself to stay narrow
		container.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));

		Runnable doSearch = () -> {
			String query = searchField.getText().trim();
			itemResultsGrid.removeAll();
			if (query.isEmpty() || itemManager == null)
			{
				itemResultsGrid.revalidate();
				itemResultsGrid.repaint();
				return;
			}
			try
			{
				java.util.List<ItemPrice> results = itemManager.search(query);
				int max = Math.min(results.size(), 32); // cap to avoid huge grids
				for (int i = 0; i < max; i++)
				{
					ItemPrice it = results.get(i);
					itemResultsGrid.add(buildAsyncItemCell(it.getId(), it.getName()));
				}
			}
			catch (Exception ignored) {}
			itemResultsGrid.revalidate();
			itemResultsGrid.repaint();
		};
		searchBtn.addActionListener(e -> doSearch.run());
		searchField.addActionListener(e -> doSearch.run()); // enter key

		return container;
	}

	private JPanel buildBundledGrid()
	{
		List<String> bundled = listBundledIconKeys();
		if (bundled.isEmpty())
		{
			JPanel placeholder = new JPanel(new BorderLayout());
			placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
			placeholder.setBorder(new EmptyBorder(4, 4, 4, 4));
			JLabel hint = new JLabel("<html>No bundled icons yet. Drop PNG files into "
				+ "<code>src/main/resources/icons/</code> and rebuild — the filename "
				+ "(without .png) becomes the iconKey.</html>");
			hint.setForeground(new Color(140, 140, 140));
			placeholder.add(hint, BorderLayout.CENTER);
			return placeholder;
		}
		int cols = 3;
		int rows = (bundled.size() + cols - 1) / cols;
		JPanel grid = new JPanel(new GridLayout(rows, cols, 4, 4));
		grid.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (String key : bundled)
		{
			grid.add(buildIconCell(key, key, resolveBundledImage(key)));
		}
		return grid;
	}

	/**
	 * Build an item icon cell that handles {@link net.runelite.client.util.AsyncBufferedImage}
	 * loading correctly. Uses {@code addTo(JLabel)} so the image populates
	 * itself when the async load completes — no need to wait or scale.
	 */
	private JPanel buildAsyncItemCell(int itemId, String tooltip)
	{
		String iconKey = "item:" + itemId;
		JPanel cell = new JPanel(new BorderLayout());
		cell.setPreferredSize(new Dimension(CELL, CELL));
		cell.setBorder(BorderFactory.createLineBorder(
			iconKey.equalsIgnoreCase(result) ? Color.WHITE : new Color(80, 80, 80), 1));
		cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cell.setToolTipText(tooltip);

		JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
		try
		{
			net.runelite.client.util.AsyncBufferedImage img = itemManager.getImage(itemId);
			img.addTo(iconLabel);
		}
		catch (Exception ignored) {}
		cell.add(iconLabel, BorderLayout.CENTER);

		cell.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				result = iconKey;
				cancelled = false;
				dispose();
			}
		});
		return cell;
	}

	private JPanel buildIconCell(String iconKey, String tooltip, BufferedImage image)
	{
		JPanel cell = new JPanel(new BorderLayout());
		cell.setPreferredSize(new Dimension(CELL, CELL));
		cell.setBorder(BorderFactory.createLineBorder(
			iconKey.equalsIgnoreCase(result) ? Color.WHITE : new Color(80, 80, 80), 1));
		cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cell.setToolTipText(tooltip);

		if (image != null)
		{
			BufferedImage scaled = new BufferedImage(ICON_PX, ICON_PX, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = scaled.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.drawImage(image, 0, 0, ICON_PX, ICON_PX, null);
			g2d.dispose();
			JLabel iconLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
			cell.add(iconLabel, BorderLayout.CENTER);
		}
		else
		{
			JLabel placeholder = new JLabel("?", SwingConstants.CENTER);
			placeholder.setForeground(new Color(140, 140, 140));
			cell.add(placeholder, BorderLayout.CENTER);
		}

		cell.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				result = iconKey;
				cancelled = false;
				dispose();
			}
		});
		return cell;
	}

	private BufferedImage resolveBundledImage(String key)
	{
		try (InputStream in = getClass().getResourceAsStream("/icons/" + key + ".png"))
		{
			if (in != null) return ImageIO.read(in);
		}
		catch (IOException ignored) {}
		return null;
	}

	/**
	 * Discover bundled icon keys by listing /icons/ on the classpath.
	 * Java's classloader doesn't expose directory listings reliably, so
	 * this returns an empty list for now — bundled icon support is wired
	 * but no icons are currently bundled. Future work can replace this
	 * with a proper resource scanner or a hardcoded manifest.
	 */
	private List<String> listBundledIconKeys()
	{
		return new ArrayList<>();
	}

	/**
	 * Show the picker dialog. Returns the selected iconKey (possibly null
	 * for "clear icon"), or {@code currentKey} unchanged on cancel.
	 */
	public static String show(java.awt.Frame owner, String currentKey,
		SkillIconManager skillIconManager, ItemManager itemManager)
	{
		IconPickerDialog dialog = new IconPickerDialog(owner, currentKey, skillIconManager, itemManager);
		dialog.setVisible(true);
		return dialog.cancelled ? currentKey : dialog.result;
	}
}

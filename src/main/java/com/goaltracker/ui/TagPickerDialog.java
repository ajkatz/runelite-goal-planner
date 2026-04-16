package com.goaltracker.ui;

import com.goaltracker.api.GoalTrackerInternalApi;
import com.goaltracker.api.TagView;
import com.goaltracker.model.TagCategory;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Shared (category, label) picker for "Add Tag" flows. Both the single-item
 * right-click Add Tag and the bulk multi-select Add Tag dialogs route through
 * here so there's one place that defines the category list, the
 * dropdown-vs-freeform switch, and the SKILLING-is-locked-to-existing rule.
 *
 * <p>Extracted to stop drift between the two dialogs. An earlier
 * version had the SKILLING filter applied in only one of them, which
 * silently blocked skill tag attachment on single goals while allowing
 * it in bulk — the kind of bug this class exists to prevent.
 *
 * <p>The dialog is strictly a picker: it collects a (category, label) pair
 * from the user and returns it. It does NOT mutate any goals — callers
 * decide whether to apply the result to a single goal, a selection, or
 * something else, and they own the compound-undo wrapping if applicable.
 */
public final class TagPickerDialog
{
	private TagPickerDialog() {}

	/**
	 * Result of a successful picker interaction. Returned from
	 * {@link #show(Component, String, GoalTrackerInternalApi)}.
	 */
	public static final class Result
	{
		public final TagCategory category;
		public final String label;

		Result(TagCategory category, String label)
		{
			this.category = category;
			this.label = label;
		}
	}

	/**
	 * Show the picker dialog modally.
	 *
	 * @param parent parent component for centering
	 * @param title  dialog title (e.g. "Add Tag" or "Add Tag to 5 goals")
	 * @param api    internal API used to query the existing tag set for the
	 *               current-category dropdown
	 * @return the user's pick, or null if they cancelled or submitted an
	 *         empty/invalid label
	 */
	public static Result show(Component parent, String title, GoalTrackerInternalApi api)
	{
		JPanel tagPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;

		// --- Category selector ---
		JLabel catLabel = new JLabel("Category:");
		catLabel.setPreferredSize(new Dimension(80, 24));
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		tagPanel.add(catLabel, gbc);

		// All categories. SKILLING is system-only — the dropdown below locks
		// to non-editable when SKILLING is picked so users can only attach
		// an existing seeded skill tag. The downstream API layer
		// (addTagWithCategory) also enforces this, but the UI gate avoids
		// surprising the user at submit time.
		TagCategory[] categories = TagCategory.values();
		JComboBox<TagCategory> catCombo = new JComboBox<>(categories);
		catCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof TagCategory)
				{
					setText(((TagCategory) value).getDisplayName());
				}
				return this;
			}
		});
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		tagPanel.add(catCombo, gbc);

		// --- Label input ---
		JLabel nameLabel = new JLabel("Tag:");
		nameLabel.setPreferredSize(new Dimension(80, 24));
		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		tagPanel.add(nameLabel, gbc);

		JComboBox<String> dropdownField = new JComboBox<>();
		JTextField freeField = new JTextField(15); // kept for layout symmetry — not currently shown
		JPanel fieldSwap = new JPanel(new CardLayout());
		fieldSwap.add(dropdownField, "DROPDOWN");
		fieldSwap.add(freeField, "FREEFORM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		tagPanel.add(fieldSwap, gbc);

		// Populate the dropdown with existing tags in the selected category.
		// The combo is editable for non-SKILLING categories so the user can
		// type a new label (find-or-create downstream). For SKILLING it's
		// locked to the existing seeded set — you can attach a skill tag to
		// a goal but you can't create new ones.
		Runnable updateField = () ->
		{
			TagCategory cat = (TagCategory) catCombo.getSelectedItem();
			dropdownField.setEditable(cat != TagCategory.SKILLING);
			dropdownField.removeAllItems();
			List<TagView> all = api.queryAllTags();
			for (TagView t : all)
			{
				if (cat != null && cat.name().equals(t.category))
				{
					dropdownField.addItem(t.label);
				}
			}
			if (dropdownField.isEditable()) dropdownField.setSelectedItem("");
			else if (dropdownField.getItemCount() > 0) dropdownField.setSelectedIndex(0);
			((CardLayout) fieldSwap.getLayout()).show(fieldSwap, "DROPDOWN");
		};
		catCombo.addActionListener(ev -> updateField.run());
		updateField.run();

		tagPanel.setPreferredSize(new Dimension(300, tagPanel.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(
			parent, tagPanel, title,
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);
		if (result != JOptionPane.OK_OPTION) return null;

		TagCategory selectedCat = (TagCategory) catCombo.getSelectedItem();
		// Editable combo: getEditor().getItem() captures typed text.
		// Non-editable (SKILLING): getSelectedItem() returns the picked option.
		Object raw = dropdownField.isEditable()
			? dropdownField.getEditor().getItem() : dropdownField.getSelectedItem();
		String tagText = raw == null ? "" : raw.toString().trim();
		if (tagText.isEmpty() || selectedCat == null) return null;
		return new Result(selectedCat, tagText);
	}
}

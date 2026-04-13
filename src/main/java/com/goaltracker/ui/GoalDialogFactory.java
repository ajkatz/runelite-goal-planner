package com.goaltracker.ui;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.api.GoalTrackerInternalApi;
import com.goaltracker.api.SectionView;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Factory that builds and shows every goal/section dialog the panel needs.
 * Extracted from GoalPanel to keep dialog construction out of the panel class.
 */
@Slf4j
class GoalDialogFactory
{
	private final GoalTrackerApiImpl api;
	private final com.goaltracker.persistence.GoalStore goalStore;
	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;
	private final GoalPanel.ItemSearchRequest itemSearchCallback;
	private final Component parentComponent;

	/** In-section position to place the next goal created via
	 *  showAddGoalDialog. -1 = default (bottom). Cleared after each create. */
	int pendingAddPositionInSection = -1;

	private Client client;

	GoalDialogFactory(GoalTrackerApiImpl api,
					  com.goaltracker.persistence.GoalStore goalStore,
					  SkillIconManager skillIconManager,
					  ItemManager itemManager,
					  SpriteManager spriteManager,
					  GoalPanel.ItemSearchRequest itemSearchCallback,
					  Component parentComponent)
	{
		this.api = api;
		this.goalStore = goalStore;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.itemSearchCallback = itemSearchCallback;
		this.parentComponent = parentComponent;
	}

	void setClient(Client client)
	{
		this.client = client;
	}

	/**
	 * Re-target dialog — opens a modal with a
	 * SkillTargetForm with synced Level/XP fields plus a Mode toggle so the
	 * user can target an absolute level/XP OR a delta gain.
	 */
	void showChangeSkillTargetDialog(Goal goal)
	{
		net.runelite.api.Skill skill;
		try
		{
			skill = net.runelite.api.Skill.valueOf(goal.getSkillName());
		}
		catch (Exception ex) { return; }

		int currentXp = client != null ? client.getSkillExperience(skill) : 0;
		int currentTargetLevel = goal.getTargetValue() > 0
			? net.runelite.api.Experience.getLevelForXp(goal.getTargetValue()) : 1;

		SkillTargetForm form = new SkillTargetForm(currentTargetLevel);

		javax.swing.JRadioButton modeAbsolute = new javax.swing.JRadioButton("Reach X", true);
		javax.swing.JRadioButton modeRelative = new javax.swing.JRadioButton("Gain X more");
		modeAbsolute.setOpaque(false);
		modeRelative.setOpaque(false);
		javax.swing.ButtonGroup grp = new javax.swing.ButtonGroup();
		grp.add(modeAbsolute); grp.add(modeRelative);
		JPanel modeRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		modeRow.setOpaque(false);
		modeRow.add(modeAbsolute);
		modeRow.add(modeRelative);
		modeAbsolute.addActionListener(ev -> form.setRelativeBaseline(-1));
		modeRelative.addActionListener(ev -> form.setRelativeBaseline(currentXp));

		JPanel panel = new JPanel(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.gridx = 0; gbc.gridy = 0;
		panel.add(new JLabel("Mode:"), gbc);
		gbc.gridx = 1;
		panel.add(modeRow, gbc);
		gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
		panel.add(form, gbc);

		int result = JOptionPane.showConfirmDialog(parentComponent, panel,
			"Change " + skill.getName() + " Target",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;

		int formValue = form.getTargetXp();
		if (formValue < 0) return;
		int newXp = modeRelative.isSelected()
			? RelativeTargetResolver.resolveSkillXp(currentXp, formValue)
			: formValue;
		if (newXp < 0) return;
		api.changeTarget(goal.getId(), newXp);
	}

	/**
	 * Bulk Remove Tag dialog. Shows the merged set of removable
	 * tags across the selection with a count badge ("Slayer (3)") so the user
	 * knows how many of their selection have it. Picking a tag fires a single
	 * bulk API call.
	 */
	void showBulkRemoveTagDialog(Set<String> selectedIds,
		List<GoalTrackerInternalApi.TagRemovalOption> opts)
	{
		String[] labels = new String[opts.size()];
		for (int i = 0; i < opts.size(); i++)
		{
			GoalTrackerInternalApi.TagRemovalOption o = opts.get(i);
			labels[i] = o.label + " (" + o.count + ")";
		}
		String picked = (String) JOptionPane.showInputDialog(
			parentComponent, "Remove which tag from the selection?", "Bulk Remove Tag",
			JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
		if (picked == null) return;
		int idx = Arrays.asList(labels).indexOf(picked);
		if (idx < 0) return;
		String tagId = opts.get(idx).tagId;
		int removed = api.bulkRemoveTagFromGoals(selectedIds, tagId);
		log.debug("bulkRemoveTagFromGoals removed {} from {}", opts.get(idx).label, removed);
	}

	void showBulkAddTagDialog(List<Goal> selectedGoals)
	{
		TagPickerDialog.Result picked = TagPickerDialog.show(
			parentComponent, "Add Tag to " + selectedGoals.size() + " goals", api);
		if (picked == null) return;

		// Route through the internal API so the bulk path matches the single-item
		// path. addTagWithCategory preserves the user-picked
		// category (api.addTag would force OTHER). Each call fires onGoalsChanged,
		// which fires N rebuilds for N selected goals — acceptable tradeoff for
		// keeping the API the canonical mutation surface; the user clicks OK once
		// so the cumulative work is bounded. Wrapping in a compound keeps the
		// whole gesture as a single undo entry.
		api.beginCompound("Add tag '" + picked.label + "' to " + selectedGoals.size() + " goals");
		try
		{
			for (Goal g : selectedGoals)
			{
				api.addTagWithCategory(g.getId(), picked.label, picked.category.name());
			}
		}
		finally { api.endCompound(); }
	}

	/**
	 * Bulk Change Color dialog. Opens the ColorPickerField with a neutral
	 * default (mixed selections have no single sensible default), then applies
	 * the chosen color via api.setGoalColor for every selected goal.
	 */
	void showBulkChangeColorDialog(List<Goal> selectedGoals)
	{
		ColorPickerField picker = new ColorPickerField(-1, 0x3C3C3C);
		int result = JOptionPane.showConfirmDialog(parentComponent, picker,
			"Color for " + selectedGoals.size() + " goals",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		int rgb = picker.getSelectedRgb();
		api.beginCompound("Recolor " + selectedGoals.size() + " goals");
		try
		{
			for (Goal g : selectedGoals) api.setGoalColor(g.getId(), rgb);
		}
		finally { api.endCompound(); }
	}

	void showCreateSectionDialog()
	{
		showCreateSectionDialog(-1);
	}

	/**
	 * Show the create-section dialog and, on success, reorder the new section
	 * to the requested user-band index. -1 = default (end of user band).
	 */
	void showCreateSectionDialog(int userBandPosition)
	{
		String input = JOptionPane.showInputDialog(parentComponent, "Section name:", "New Section",
			JOptionPane.PLAIN_MESSAGE);
		if (input == null) return;
		try
		{
			String newId = api.createSection(input);
			if (newId != null && userBandPosition >= 0)
			{
				api.reorderSection(newId, userBandPosition);
			}
		}
		catch (IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(parentComponent, ex.getMessage(), "Invalid name",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	void showSectionColorDialog(SectionView section)
	{
		int current = section.colorOverridden ? section.colorRgb : -1;
		ColorPickerField picker = new ColorPickerField(current, section.defaultColorRgb);
		int result = JOptionPane.showConfirmDialog(parentComponent, picker,
			"Section Color — " + section.name,
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		api.setSectionColor(section.id, picker.getSelectedRgb());
	}

	void showGoalColorDialog(Goal goal)
	{
		int defaultRgb;
		java.awt.Color c = goal.getType().getColor();
		defaultRgb = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
		ColorPickerField picker = new ColorPickerField(goal.getCustomColorRgb(), defaultRgb);
		int result = JOptionPane.showConfirmDialog(parentComponent, picker,
			"Goal Color — " + goal.getName(),
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) return;
		api.setGoalColor(goal.getId(), picker.getSelectedRgb());
	}

	void showRenameSectionDialog(SectionView section)
	{
		String input = (String) JOptionPane.showInputDialog(parentComponent, "New name:", "Rename Section",
			JOptionPane.PLAIN_MESSAGE, null, null, section.name);
		if (input == null) return;
		boolean ok = api.renameSection(section.id, input);
		if (!ok)
		{
			JOptionPane.showMessageDialog(parentComponent,
				"Could not rename section. Name may be invalid, duplicate, or unchanged.",
				"Rename failed", JOptionPane.WARNING_MESSAGE);
		}
	}

	void showAddGoalDialog(String preferredSectionId)
	{
		// Use GridBagLayout for reliable sizing in JOptionPane
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;

		// Labels column
		int labelWidth = 100;

		// Row 0: Type
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		JLabel typeLabel = new JLabel("Type:");
		typeLabel.setPreferredSize(new Dimension(labelWidth, 24));
		panel.add(typeLabel, gbc);

		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		JComboBox<GoalType> typeCombo = new JComboBox<>(new GoalType[]{GoalType.SKILL, GoalType.BOSS, GoalType.ITEM_GRIND, GoalType.ACCOUNT, GoalType.CUSTOM});
		typeCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof GoalType)
				{
					setText(((GoalType) value).getDisplayName());
				}
				return this;
			}
		});
		panel.add(typeCombo, gbc);

		// Row 1: Field 1 label + input
		JLabel label1 = new JLabel("Skill:");
		label1.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label1, gbc);

		// Filter out skills already at 99
		Skill[] availableSkills = java.util.Arrays.stream(Skill.values())
			.filter(s -> {
				if (client == null) return true;
				try { return client.getRealSkillLevel(s) < 99; }
				catch (Exception e) { return true; }
			})
			.toArray(Skill[]::new);
		JComboBox<Skill> skillCombo = new JComboBox<>(availableSkills);
		skillCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Skill)
				{
					setText(((Skill) value).getName());
				}
				return this;
			}
		});
		JTextField nameField = new JTextField(15);
		JTextField itemQtyField = new JTextField("1", 15);

		// Account metric combo
		com.goaltracker.model.AccountMetric[] metrics = com.goaltracker.model.AccountMetric.values();
		JComboBox<com.goaltracker.model.AccountMetric> metricCombo = new JComboBox<>(metrics);
		metricCombo.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof com.goaltracker.model.AccountMetric)
				{
					setText(((com.goaltracker.model.AccountMetric) value).getDisplayName());
				}
				return this;
			}
		});

		// Boss combo
		String[] bossNames = com.goaltracker.data.BossKillData.getBossNames();
		JComboBox<String> bossCombo = new JComboBox<>(bossNames);
		JTextField bossKillsField = new JTextField("1", 15);

		// CardLayout to swap between types
		JPanel field1Panel = new JPanel(new CardLayout());
		field1Panel.add(skillCombo, "SKILL");
		field1Panel.add(bossCombo, "BOSS");
		field1Panel.add(itemQtyField, "ITEM_GRIND");
		field1Panel.add(metricCombo, "ACCOUNT");
		field1Panel.add(nameField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field1Panel, gbc);

		// Row 1.5: Mode toggle for relative goals.
		// "Reach X" = absolute (existing behavior). "Gain X more" = compute
		// resolved target as currentValue + entered delta. SKILL/ITEM/CUSTOM
		// all support this; the actual math runs in the submit handlers.
		gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		JLabel modeLabel = new JLabel("Mode:");
		modeLabel.setPreferredSize(new Dimension(labelWidth, 24));
		panel.add(modeLabel, gbc);

		javax.swing.JRadioButton modeAbsolute = new javax.swing.JRadioButton("Reach X", true);
		javax.swing.JRadioButton modeRelative = new javax.swing.JRadioButton("Gain X more");
		modeAbsolute.setOpaque(false);
		modeRelative.setOpaque(false);
		javax.swing.ButtonGroup modeGroup = new javax.swing.ButtonGroup();
		modeGroup.add(modeAbsolute);
		modeGroup.add(modeRelative);
		JPanel modeRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		modeRow.setOpaque(false);
		modeRow.add(modeAbsolute);
		modeRow.add(modeRelative);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(modeRow, gbc);

		// Row 3 (was Row 2): Field 2
		JLabel label2 = new JLabel("Target:");
		label2.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label2, gbc);

		JTextField descField = new JTextField(15);
		JLabel itemHint = new JLabel("<html><i>Item search opens in-game</i></html>");
		itemHint.setForeground(new Color(140, 140, 140));

		// Skill row uses the shared SkillTargetForm with synced Level/XP fields.
		SkillTargetForm skillTargetForm = new SkillTargetForm(99);

		JTextField accountTargetField = new JTextField("1", 15);

		JPanel field2Panel = new JPanel(new CardLayout());
		field2Panel.add(skillTargetForm, "SKILL");
		field2Panel.add(bossKillsField, "BOSS");
		field2Panel.add(itemHint, "ITEM_GRIND");
		field2Panel.add(accountTargetField, "ACCOUNT");
		field2Panel.add(descField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field2Panel, gbc);

		// Row 4: Account shortcuts (CA tier combo + Max button).
		// Visible only when type=ACCOUNT.
		JLabel shortcutLabel = new JLabel("Shortcut:");
		shortcutLabel.setPreferredSize(new Dimension(labelWidth, 24));
		shortcutLabel.setVisible(false);
		gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(shortcutLabel, gbc);

		// CA tier dropdown
		String[] caTierOptions = new String[com.goaltracker.model.AccountMetric.CA_TIER_NAMES.length + 1];
		caTierOptions[0] = "-- Select tier --";
		System.arraycopy(com.goaltracker.model.AccountMetric.CA_TIER_NAMES, 0, caTierOptions, 1,
			com.goaltracker.model.AccountMetric.CA_TIER_NAMES.length);
		JComboBox<String> caTierCombo = new JComboBox<>(caTierOptions);
		caTierCombo.addActionListener(e -> {
			int idx = caTierCombo.getSelectedIndex();
			if (idx > 0)
			{
				accountTargetField.setText(
					String.valueOf(com.goaltracker.model.AccountMetric.CA_TIER_VALUES[idx - 1]));
			}
		});

		// Max button — fills target with the metric's max value
		javax.swing.JButton maxButton = new javax.swing.JButton("Max");
		maxButton.addActionListener(e -> {
			com.goaltracker.model.AccountMetric m =
				(com.goaltracker.model.AccountMetric) metricCombo.getSelectedItem();
			if (m != null)
			{
				accountTargetField.setText(String.valueOf(m.getMaxTarget()));
			}
		});

		JPanel shortcutRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
		shortcutRow.setOpaque(false);
		shortcutRow.add(caTierCombo);
		shortcutRow.add(maxButton);
		caTierCombo.setVisible(false);
		shortcutRow.setVisible(false);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(shortcutRow, gbc);

		// Swap fields when type changes. Also relabel based on
		// current mode so "Quantity:" → "Gain Quantity:" when relative.
		Runnable updateLabels = () ->
		{
			GoalType selected = (GoalType) typeCombo.getSelectedItem();
			((CardLayout) field1Panel.getLayout()).show(field1Panel, selected.name());
			((CardLayout) field2Panel.getLayout()).show(field2Panel, selected.name());
			// Relative mode for SKILL and BOSS (e.g. "Gain 50 more kills").
			boolean modeRowVisible = selected == GoalType.SKILL || selected == GoalType.BOSS;
			modeLabel.setVisible(modeRowVisible);
			modeRow.setVisible(modeRowVisible);
			if (!modeRowVisible) modeAbsolute.setSelected(true);
			boolean rel = modeRelative.isSelected();
			// If relative + SKILL, hand the form the player's
			// current XP for the chosen skill so deltas resolve correctly.
			if (rel && selected == GoalType.SKILL && client != null)
			{
				Skill chosen = (Skill) skillCombo.getSelectedItem();
				int currentXp = chosen != null ? client.getSkillExperience(chosen) : 0;
				skillTargetForm.setRelativeBaseline(currentXp);
			}
			else
			{
				skillTargetForm.setRelativeBaseline(-1);
			}
			// Hide account shortcut row by default; ACCOUNT case shows it.
			shortcutLabel.setVisible(false);
			shortcutRow.setVisible(false);
			switch (selected)
			{
				case SKILL:
					label1.setText("Skill:");
					label2.setText(rel ? "Add XP:" : "Target:");
					break;
				case BOSS:
					label1.setText("Boss:");
					label2.setText(rel ? "Gain Kills:" : "Target Kills:");
					break;
				case ITEM_GRIND:
					label1.setText(rel ? "Gain qty:" : "Quantity:");
					label2.setText("");
					break;
				case ACCOUNT:
					label1.setText("Metric:");
					label2.setText("Target:");
					{
						com.goaltracker.model.AccountMetric m =
							(com.goaltracker.model.AccountMetric) metricCombo.getSelectedItem();
						boolean isCa = m == com.goaltracker.model.AccountMetric.CA_POINTS;
						caTierCombo.setVisible(isCa);
						shortcutLabel.setVisible(true);
						shortcutRow.setVisible(true);
					}
					break;
				default:
					label1.setText("Goal Name:");
					label2.setText(rel ? "Description (gain target via Custom value):" : "Description:");
					break;
			}
			Window w = SwingUtilities.getWindowAncestor(panel);
			if (w != null) w.pack();
		};
		typeCombo.addActionListener(e -> updateLabels.run());
		metricCombo.addActionListener(e -> updateLabels.run());
		modeAbsolute.addActionListener(e -> updateLabels.run());
		modeRelative.addActionListener(e -> updateLabels.run());
		skillCombo.addActionListener(e -> updateLabels.run());

		panel.setPreferredSize(new Dimension(320, panel.getPreferredSize().height));

		// Non-modal dialog so the sidebar stays responsive while this is open.
		Window ownerWindow = SwingUtilities.getWindowAncestor(parentComponent);
		JDialog dialog = new JDialog(
			ownerWindow instanceof java.awt.Frame ? (java.awt.Frame) ownerWindow : null,
			"Add Goal", false);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel buttonRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		buttonRow.add(okButton);
		buttonRow.add(cancelButton);

		JPanel content = new JPanel(new BorderLayout(0, 8));
		content.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(panel, BorderLayout.CENTER);
		content.add(buttonRow, BorderLayout.SOUTH);
		dialog.setContentPane(content);

		cancelButton.addActionListener(e -> dialog.dispose());
		okButton.addActionListener(e ->
		{
			GoalType selectedType = (GoalType) typeCombo.getSelectedItem();
			boolean relative = modeRelative.isSelected();

			if (selectedType == GoalType.SKILL)
			{
				addSkillGoal(skillCombo, skillTargetForm, preferredSectionId, relative);
				dialog.dispose();
			}
			else if (selectedType == GoalType.BOSS)
			{
				String selectedBoss = (String) bossCombo.getSelectedItem();
				if (selectedBoss == null) return;
				try
				{
					int enteredKills = Integer.parseInt(bossKillsField.getText().trim().replace(",", ""));
					if (enteredKills <= 0)
					{
						JOptionPane.showMessageDialog(dialog, "Kill count must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// Relative mode: target = current KC + entered delta.
					int targetKills = enteredKills;
					if (relative && client != null)
					{
						int varpId = com.goaltracker.data.BossKillData.getVarpId(selectedBoss);
						if (varpId >= 0)
						{
							int currentKc = client.getVarpValue(varpId);
							targetKills = currentKc + enteredKills;
						}
					}
					api.beginCompound("Add boss goal: " + selectedBoss);
					try
					{
						String createdId = api.addBossGoal(selectedBoss, targetKills);
						moveToPreferredSection(createdId, preferredSectionId);
					}
					finally
					{
						api.endCompound();
					}
					dialog.dispose();
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(dialog, "Invalid kill count.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.ITEM_GRIND)
			{
				try
				{
					int qty = Integer.parseInt(itemQtyField.getText().trim().replace(",", ""));
					if (qty <= 0)
					{
						JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int capturedPosition = pendingAddPositionInSection;
					pendingAddPositionInSection = -1;
					dialog.dispose();
					itemSearchCallback.accept(qty, preferredSectionId, capturedPosition);
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(dialog, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.ACCOUNT)
			{
				com.goaltracker.model.AccountMetric metric =
					(com.goaltracker.model.AccountMetric) metricCombo.getSelectedItem();
				if (metric == null) return;
				try
				{
					int target = Integer.parseInt(accountTargetField.getText().trim().replace(",", ""));
					if (target <= 0)
					{
						JOptionPane.showMessageDialog(dialog, "Target must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					api.beginCompound("Add goal: " + metric.getDisplayName());
					try
					{
						String createdId = api.addAccountGoal(metric.name(), target);
						moveToPreferredSection(createdId, preferredSectionId);
					}
					finally
					{
						api.endCompound();
					}
					dialog.dispose();
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showMessageDialog(dialog, "Invalid target number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.CUSTOM)
			{
				addCustomGoal(nameField, descField, preferredSectionId);
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setVisible(true);
	}

	private void addSkillGoal(JComboBox<Skill> skillCombo, SkillTargetForm form, String preferredSectionId, boolean relative)
	{
		Skill skill = (Skill) skillCombo.getSelectedItem();
		int formValue = form.getTargetXp();
		if (formValue < 0)
		{
			JOptionPane.showMessageDialog(parentComponent,
				relative ? "Enter a valid XP delta (1–200,000,000)."
					: "Enter a valid target level (1–99) or XP (0–200,000,000).",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// In relative mode, the form returns a delta. Resolve to
		// absolute by adding the player's current XP for the chosen skill.
		int targetXp;
		if (relative)
		{
			int currentXp = client != null ? client.getSkillExperience(skill) : 0;
			targetXp = RelativeTargetResolver.resolveSkillXp(currentXp, formValue);
			if (targetXp < 0)
			{
				JOptionPane.showMessageDialog(parentComponent, "XP delta must be greater than 0.",
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else
		{
			targetXp = formValue;
		}

		String conflict = checkSkillConflict(skill, targetXp);
		if (conflict != null)
		{
			JOptionPane.showMessageDialog(parentComponent, conflict, "Conflict", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Wrap create + position in a single compound undo entry
		// so one undo fully reverses the operation.
		api.beginCompound("Add goal: " + skill.getName());
		try
		{
			String createdId = api.addSkillGoal(skill, targetXp);
			moveToPreferredSection(createdId, preferredSectionId);
		}
		finally
		{
			api.endCompound();
		}
	}

	private void addCustomGoal(JTextField nameField, JTextField descField, String preferredSectionId)
	{
		String name = nameField.getText().trim();
		if (name.isEmpty())
		{
			JOptionPane.showMessageDialog(parentComponent, "Goal name is required.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		api.beginCompound("Add goal: " + name);
		try
		{
			String createdId = api.addCustomGoal(name, descField.getText().trim());
			moveToPreferredSection(createdId, preferredSectionId);
		}
		finally
		{
			api.endCompound();
		}
	}

	/**
	 * Move a freshly-created goal to a section other than the default Incomplete.
	 * Used by the section header "Add Goal" entry to drop new goals directly
	 * into the section the user right-clicked. No-op when preferredSectionId is
	 * null (the toolbar + button) or the goal didn't actually get created.
	 *
	 * <p>Also honors {@link #pendingAddPositionInSection} so the
	 * goal lands at the exact slot the user picked from the context menu
	 * (Top, Bottom, Above, Below). Field is cleared after use.
	 */
	private void moveToPreferredSection(String goalId, String preferredSectionId)
	{
		if (goalId == null) return;
		try
		{
			if (preferredSectionId != null && pendingAddPositionInSection >= 0)
			{
				api.positionGoalInSection(goalId, preferredSectionId, pendingAddPositionInSection);
			}
			else if (preferredSectionId != null)
			{
				api.moveGoalToSection(goalId, preferredSectionId);
			}
		}
		finally
		{
			pendingAddPositionInSection = -1;
		}
	}

	/**
	 * Check if a new skill goal conflicts with existing goals.
	 * Blocks exact duplicates only. Multiple levels for the same skill are fine.
	 * Returns an error message if conflicting, null if OK.
	 */
	String checkSkillConflict(Skill skill, int target)
	{
		for (Goal existing : goalStore.getGoals())
		{
			if (existing.getType() != GoalType.SKILL || existing.getSkillName() == null)
			{
				continue;
			}
			if (!existing.getSkillName().equals(skill.name()))
			{
				continue;
			}
			if (existing.isComplete())
			{
				continue;
			}

			if (existing.getTargetValue() == target)
			{
				return String.format("You already have a %s goal for %s.",
					skill.getName(), target > 99 ? FormatUtil.formatNumber(target) + " XP" : "Level " + target);
			}
		}
		return null;
	}
}

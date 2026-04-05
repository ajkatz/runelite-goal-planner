package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar panel — priority list of goals with gradient cards and arrow reordering.
 */
@Slf4j
public class GoalPanel extends PluginPanel
{
	private final GoalStore goalStore;
	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;
	private final java.util.function.IntConsumer itemSearchCallback;
	private final java.util.function.Consumer<String> itemScanCallback;
	private Client client;
	private final JPanel goalListPanel;
	private final Map<String, GoalCard> cardMap = new HashMap<>();

	public GoalPanel(GoalStore goalStore, SkillIconManager skillIconManager, ItemManager itemManager,
					 java.util.function.IntConsumer itemSearchCallback,
					 java.util.function.Consumer<String> itemScanCallback)
	{
		super(false);
		this.goalStore = goalStore;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;
		this.itemSearchCallback = itemSearchCallback;
		this.itemScanCallback = itemScanCallback;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header with add button
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Goal Tracker");
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

		JButton addButton = new JButton("+");
		addButton.setToolTipText("Add a new goal");
		addButton.addActionListener(e -> showAddGoalDialog());

		header.add(title, BorderLayout.WEST);
		header.add(addButton, BorderLayout.EAST);

		// Scrollable goal list
		goalListPanel = new JPanel();
		goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
		goalListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		goalListPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

		JScrollPane scrollPane = new JScrollPane(goalListPanel);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(header, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		rebuild();
	}

	public void setClient(Client client)
	{
		this.client = client;
	}

	public void rebuild()
	{
		goalListPanel.removeAll();
		cardMap.clear();

		List<Goal> goals = goalStore.getGoals();

		for (int i = 0; i < goals.size(); i++)
		{
			final int index = i;
			Goal goal = goals.get(i);

			GoalCard card = new GoalCard(
				goal,
				e -> moveGoal(index, index - 1),
				e -> moveGoal(index, index + 1),
				skillIconManager,
				itemManager
			);

			card.setFirstInList(i == 0);
			card.setLastInList(i == goals.size() - 1);

			addContextMenu(card, goal, index, goals.size());
			cardMap.put(goal.getId(), card);

			goalListPanel.add(card);
			goalListPanel.add(Box.createVerticalStrut(4));
		}

		if (goals.isEmpty())
		{
			JLabel empty = new JLabel("No goals yet. Click + to add one.");
			empty.setForeground(new Color(120, 120, 120));
			empty.setAlignmentX(Component.CENTER_ALIGNMENT);
			empty.setBorder(new EmptyBorder(20, 0, 0, 0));
			goalListPanel.add(empty);
		}

		goalListPanel.revalidate();
		goalListPanel.repaint();
	}

	private void moveGoalTo(int fromIndex, int toIndex)
	{
		List<Goal> goals = goalStore.getGoals();
		if (fromIndex < 0 || fromIndex >= goals.size() || toIndex < 0 || toIndex >= goals.size() || fromIndex == toIndex)
		{
			return;
		}

		goalStore.reorder(fromIndex, toIndex);

		// Enforce skill ordering after direct placement
		goals = goalStore.getGoals();
		boolean fixed = true;
		int maxPasses = goals.size();
		while (fixed && maxPasses-- > 0)
		{
			fixed = false;
			for (int i = 0; i < goals.size(); i++)
			{
				Goal a = goals.get(i);
				if (a.getType() != GoalType.SKILL || a.getSkillName() == null || a.getStatus() == GoalStatus.COMPLETE)
					continue;
				for (int j = i + 1; j < goals.size(); j++)
				{
					Goal b = goals.get(j);
					if (b.getType() == GoalType.SKILL && a.getSkillName().equals(b.getSkillName())
						&& b.getStatus() != GoalStatus.COMPLETE && a.getTargetValue() > b.getTargetValue())
					{
						goalStore.reorder(j, i);
						goals = goalStore.getGoals();
						fixed = true;
						break;
					}
				}
				if (fixed) break;
			}
		}

		goalStore.save();
		rebuild();
	}

	private void moveGoal(int fromIndex, int toIndex)
	{
		List<Goal> goals = goalStore.getGoals();
		if (toIndex < 0 || toIndex >= goals.size())
		{
			return;
		}

		boolean movingUp = toIndex < fromIndex;
		Goal moving = goals.get(fromIndex);

		// Recursively make room if this move would violate same-skill ordering
		makeRoom(moving, fromIndex, movingUp);

		// Re-read goals (may have changed from makeRoom)
		goals = goalStore.getGoals();
		int currentIndex = goals.indexOf(moving);
		int newTarget = movingUp ? currentIndex - 1 : currentIndex + 1;

		if (newTarget >= 0 && newTarget < goals.size())
		{
			goalStore.reorder(currentIndex, newTarget);
		}

		// Ensure final state is persisted
		goalStore.save();
		rebuild();
	}

	/**
	 * Recursively make room for a goal to move in a direction.
	 *
	 * If the goal would swap with a same-skill partner (creating a violation),
	 * the partner moves first. The partner's move may recursively trigger
	 * its own partner to move, from the end of the chain inward.
	 */
	private void makeRoom(Goal moving, int movingIndex, boolean movingUp)
	{
		if (moving.getType() != GoalType.SKILL || moving.getSkillName() == null
			|| moving.getStatus() == GoalStatus.COMPLETE)
		{
			return;
		}

		List<Goal> goals = goalStore.getGoals();
		int targetIndex = movingUp ? movingIndex - 1 : movingIndex + 1;

		if (targetIndex < 0 || targetIndex >= goals.size())
		{
			return;
		}

		Goal neighbor = goals.get(targetIndex);

		// Check if the neighbor is a same-skill partner that would create a violation
		if (neighbor.getType() != GoalType.SKILL
			|| !moving.getSkillName().equals(neighbor.getSkillName())
			|| neighbor.getStatus() == GoalStatus.COMPLETE)
		{
			return; // neighbor is unrelated, no conflict
		}

		// Would this swap violate ordering? (lower target must be above higher target)
		boolean wouldViolate;
		if (movingUp)
		{
			// Moving up: we'd end up above neighbor. Violation if our target > neighbor's target
			wouldViolate = moving.getTargetValue() > neighbor.getTargetValue();
		}
		else
		{
			// Moving down: we'd end up below neighbor. Violation if our target < neighbor's target
			wouldViolate = moving.getTargetValue() < neighbor.getTargetValue();
		}

		if (!wouldViolate)
		{
			return;
		}

		// The partner needs to move first to make room.
		// Recursively check if the partner also needs to make room.
		makeRoom(neighbor, targetIndex, movingUp);

		// Now move the partner
		goals = goalStore.getGoals(); // refresh after recursive call
		int partnerIndex = goals.indexOf(neighbor);
		int partnerTarget = movingUp ? partnerIndex - 1 : partnerIndex + 1;

		if (partnerTarget >= 0 && partnerTarget < goals.size())
		{
			goalStore.reorder(partnerIndex, partnerTarget);
		}
	}

	public void updateGoal(Goal goal)
	{
		GoalCard card = cardMap.get(goal.getId());
		if (card != null)
		{
			card.update(goal);
		}
	}

	public void refresh()
	{
		for (Goal goal : goalStore.getGoals())
		{
			updateGoal(goal);
		}
	}

	private void addContextMenu(GoalCard card, Goal goal, int index, int totalGoals)
	{
		JPopupMenu menu = new JPopupMenu();

		if (index > 0)
		{
			JMenuItem moveFirst = new JMenuItem("Move to Top");
			moveFirst.addActionListener(e -> {
				moveGoalTo(index, 0);
			});
			menu.add(moveFirst);
		}

		if (index < totalGoals - 1)
		{
			JMenuItem moveLast = new JMenuItem("Move to Bottom");
			moveLast.addActionListener(e -> {
				moveGoalTo(index, totalGoals - 1);
			});
			menu.add(moveLast);
		}

		if (menu.getComponentCount() > 0)
		{
			menu.addSeparator();
		}

		if (goal.getStatus() == GoalStatus.COMPLETE)
		{
			JMenuItem reopen = new JMenuItem("Mark Incomplete");
			reopen.addActionListener(e -> {
				goal.setStatus(GoalStatus.ACTIVE);
				goal.setCompletedAt(0);
				goalStore.updateGoal(goal);
				rebuild();
			});
			menu.add(reopen);
		}
		else if (goal.getType() == GoalType.CUSTOM)
		{
			JMenuItem complete = new JMenuItem("Mark Complete");
			complete.addActionListener(e -> {
				goal.setStatus(GoalStatus.COMPLETE);
				goal.setCompletedAt(System.currentTimeMillis());
				goalStore.updateGoal(goal);
				rebuild();
			});
			menu.add(complete);

			JMenuItem editName = new JMenuItem("Change Name");
			editName.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(this, "New name:", goal.getName());
				if (input != null && !input.trim().isEmpty())
				{
					goal.setName(input.trim());
					goalStore.updateGoal(goal);
					rebuild();
				}
			});
			menu.add(editName);

			JMenuItem editDesc = new JMenuItem("Change Description");
			editDesc.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(this, "New description:",
					goal.getDescription() != null ? goal.getDescription() : "");
				if (input != null)
				{
					goal.setDescription(input.trim());
					goalStore.updateGoal(goal);
					rebuild();
				}
			});
			menu.add(editDesc);
		}

		// Skill-specific options
		if (goal.getType() == GoalType.SKILL && goal.getStatus() != GoalStatus.COMPLETE)
		{
			JMenuItem editLevel = new JMenuItem("Change Target");
			editLevel.addActionListener(e -> {
				int currentTargetLevel = goal.getTargetValue() > 0
					? net.runelite.api.Experience.getLevelForXp(goal.getTargetValue()) : 99;
				String input = JOptionPane.showInputDialog(
					this,
					"New target level for " + (goal.getSkillName() != null
					? net.runelite.api.Skill.valueOf(goal.getSkillName()).getName() : goal.getName()) + ":",
					String.valueOf(currentTargetLevel)
				);
				if (input != null)
				{
					try
					{
						int newLevel = Integer.parseInt(input.trim());
						if (newLevel >= 1 && newLevel <= 99)
						{
							int newXp = net.runelite.api.Experience.getXpForLevel(newLevel);
							goal.setTargetValue(newXp);
							goal.setName(net.runelite.api.Skill.valueOf(goal.getSkillName()).getName()
								+ " \u2192 Level " + newLevel);
							goalStore.updateGoal(goal);
							rebuild();
						}
					}
					catch (NumberFormatException ignored) {}
				}
			});
			menu.add(editLevel);
		}

		// Item-specific options
		if (goal.getType() == GoalType.ITEM_GRIND && goal.getStatus() != GoalStatus.COMPLETE)
		{
			JMenuItem editQty = new JMenuItem("Change Target");
			editQty.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(
					this,
					"New target quantity for " + goal.getName() + ":",
					String.valueOf(goal.getTargetValue())
				);
				if (input != null)
				{
					try
					{
						int newQty = Integer.parseInt(input.trim().replace(",", ""));
						if (newQty > 0)
						{
							goal.setTargetValue(newQty);
							goal.setDescription(formatNumber(newQty) + " total");
							goalStore.updateGoal(goal);
							rebuild();
						}
					}
					catch (NumberFormatException ignored) {}
				}
			});
			menu.add(editQty);

		}

		JMenuItem remove = new JMenuItem("Remove");
		remove.addActionListener(e -> {
			goalStore.removeGoal(goal.getId());
			rebuild();
		});
		menu.add(remove);

		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					menu.show(card, e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					menu.show(card, e.getX(), e.getY());
				}
			}
		});
	}

	private void showAddGoalDialog()
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
		JComboBox<GoalType> typeCombo = new JComboBox<>(new GoalType[]{GoalType.SKILL, GoalType.ITEM_GRIND, GoalType.CUSTOM});
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

		// CardLayout to swap between types
		JPanel field1Panel = new JPanel(new CardLayout());
		field1Panel.add(skillCombo, "SKILL");
		field1Panel.add(itemQtyField, "ITEM_GRIND");
		field1Panel.add(nameField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field1Panel, gbc);

		// Row 2: Field 2
		JLabel label2 = new JLabel("Target Level/XP:");
		label2.setPreferredSize(new Dimension(labelWidth, 24));
		gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		panel.add(label2, gbc);

		JTextField targetField = new JTextField("99", 15);
		JTextField descField = new JTextField(15);
		JLabel itemHint = new JLabel("<html><i>Item search opens in-game</i></html>");
		itemHint.setForeground(new Color(140, 140, 140));

		JPanel field2Panel = new JPanel(new CardLayout());
		field2Panel.add(targetField, "SKILL");
		field2Panel.add(itemHint, "ITEM_GRIND");
		field2Panel.add(descField, "CUSTOM");
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		panel.add(field2Panel, gbc);

		// Swap fields when type changes
		typeCombo.addActionListener(e ->
		{
			GoalType selected = (GoalType) typeCombo.getSelectedItem();

			((CardLayout) field1Panel.getLayout()).show(field1Panel, selected.name());
			((CardLayout) field2Panel.getLayout()).show(field2Panel, selected.name());

			switch (selected)
			{
				case SKILL:
					label1.setText("Skill:");
					label2.setText("Target Level:");
					break;
				case ITEM_GRIND:
					label1.setText("Quantity:");
					label2.setText("");
					break;
				default:
					label1.setText("Goal Name:");
					label2.setText("Description:");
					break;
			}

			Window w = SwingUtilities.getWindowAncestor(panel);
			if (w != null) w.pack();
		});

		panel.setPreferredSize(new Dimension(320, panel.getPreferredSize().height));

		int result = JOptionPane.showConfirmDialog(
			this, panel, "Add Goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION)
		{
			GoalType selectedType = (GoalType) typeCombo.getSelectedItem();

			if (selectedType == GoalType.SKILL)
			{
				addSkillGoal(skillCombo, targetField);
			}
			else if (selectedType == GoalType.ITEM_GRIND)
			{
				try
				{
					int qty = Integer.parseInt(itemQtyField.getText().trim().replace(",", ""));
					if (qty <= 0)
					{
						JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// Open the in-game chatbox item search
					itemSearchCallback.accept(qty);
				}
				catch (NumberFormatException e)
				{
					JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (selectedType == GoalType.CUSTOM)
			{
				addCustomGoal(nameField, descField);
			}
		}
	}

	private void addSkillGoal(JComboBox<Skill> skillCombo, JTextField targetField)
	{
		try
		{
			Skill skill = (Skill) skillCombo.getSelectedItem();
			int targetLevel = Integer.parseInt(targetField.getText().trim().replace(",", ""));

			if (targetLevel < 1 || targetLevel > 99)
			{
				JOptionPane.showMessageDialog(this, "Target level must be between 1 and 99.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Convert level to XP target
			int targetXp = net.runelite.api.Experience.getXpForLevel(targetLevel);

			String conflict = checkSkillConflict(skill, targetXp);
			if (conflict != null)
			{
				JOptionPane.showMessageDialog(this, conflict, "Conflict", JOptionPane.WARNING_MESSAGE);
				return;
			}

			Goal goal = Goal.builder()
				.type(GoalType.SKILL)
				.name(skill.getName() + " \u2192 Level " + targetLevel)
				.skillName(skill.name())
				.targetValue(targetXp)
				.build();

			int insertBefore = -1;
			List<Goal> existing = goalStore.getGoals();
			for (int i = 0; i < existing.size(); i++)
			{
				Goal g = existing.get(i);
				if (g.getType() == GoalType.SKILL
					&& skill.name().equals(g.getSkillName())
					&& g.getStatus() != GoalStatus.COMPLETE
					&& g.getTargetValue() > targetXp)
				{
					insertBefore = i;
					break;
				}
			}

			goalStore.addGoal(goal);
			if (insertBefore >= 0)
			{
				goalStore.reorder(goalStore.getGoals().size() - 1, insertBefore);
			}
			rebuild();
		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "Invalid target value.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addCustomGoal(JTextField nameField, JTextField descField)
	{
		String name = nameField.getText().trim();
		if (name.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Goal name is required.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Goal goal = Goal.builder()
			.type(GoalType.CUSTOM)
			.name(name)
			.description(descField.getText().trim())
			.targetValue(1)  // binary: 0 = not done, 1 = done
			.currentValue(0)
			.build();

		goalStore.addGoal(goal);
		rebuild();
	}

	/**
	 * Check if a new skill goal conflicts with existing goals.
	 * Blocks exact duplicates only. Multiple levels for the same skill are fine.
	 * Returns an error message if conflicting, null if OK.
	 */
	private String checkSkillConflict(Skill skill, int target)
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
			if (existing.getStatus() == GoalStatus.COMPLETE)
			{
				continue;
			}

			if (existing.getTargetValue() == target)
			{
				return String.format("You already have a %s goal for %s.",
					skill.getName(), target > 99 ? formatNumber(target) + " XP" : "Level " + target);
			}
		}
		return null;
	}


	private JPanel makeFieldRow(String label, JComponent field)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		JLabel lbl = new JLabel(label);
		lbl.setPreferredSize(new Dimension(100, 24));
		row.add(lbl, BorderLayout.WEST);
		row.add(field, BorderLayout.CENTER);
		return row;
	}

	public static String formatNumber(int n)
	{
		if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
		if (n >= 1_000) return String.format("%.0fK", n / 1_000.0);
		return String.valueOf(n);
	}
}

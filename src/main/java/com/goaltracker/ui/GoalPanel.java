package com.goaltracker.ui;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
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
	private final JPanel goalListPanel;
	private final Map<String, GoalCard> cardMap = new HashMap<>();

	public GoalPanel(GoalStore goalStore)
	{
		super(false);
		this.goalStore = goalStore;

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
				e -> moveGoal(index, index - 1),  // up
				e -> moveGoal(index, index + 1)    // down
			);

			card.setFirstInList(i == 0);
			card.setLastInList(i == goals.size() - 1);

			addContextMenu(card, goal);
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

	private void moveGoal(int fromIndex, int toIndex)
	{
		List<Goal> goals = goalStore.getGoals();
		if (toIndex < 0 || toIndex >= goals.size())
		{
			return;
		}
		goalStore.reorder(fromIndex, toIndex);
		rebuild();
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

	private void addContextMenu(GoalCard card, Goal goal)
	{
		JPopupMenu menu = new JPopupMenu();

		if (goal.getStatus() != GoalStatus.COMPLETE)
		{
			JMenuItem complete = new JMenuItem("Mark Complete");
			complete.addActionListener(e -> {
				goal.setStatus(GoalStatus.COMPLETE);
				goal.setCompletedAt(System.currentTimeMillis());
				goalStore.updateGoal(goal);
				rebuild();
			});
			menu.add(complete);
		}
		else
		{
			JMenuItem reopen = new JMenuItem("Reopen");
			reopen.addActionListener(e -> {
				goal.setStatus(GoalStatus.ACTIVE);
				goal.setCompletedAt(0);
				goalStore.updateGoal(goal);
				rebuild();
			});
			menu.add(reopen);
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
		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

		JComboBox<GoalType> typeCombo = new JComboBox<>(new GoalType[]{GoalType.SKILL});
		panel.add(new JLabel("Type:"));
		panel.add(typeCombo);

		JComboBox<Skill> skillCombo = new JComboBox<>(Skill.values());
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
		panel.add(new JLabel("Skill:"));
		panel.add(skillCombo);

		JTextField targetField = new JTextField("99");
		panel.add(new JLabel("Target Level/XP:"));
		panel.add(targetField);

		int result = JOptionPane.showConfirmDialog(
			this, panel, "Add Goal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION)
		{
			try
			{
				Skill skill = (Skill) skillCombo.getSelectedItem();
				int target = Integer.parseInt(targetField.getText().trim().replace(",", ""));

				Goal goal = Goal.builder()
					.type(GoalType.SKILL)
					.name(String.format("%s \u2192 %s", skill.getName(),
						target > 99 ? formatNumber(target) + " XP" : "Level " + target))
					.skillName(skill.name())
					.targetValue(target)
					.build();

				goalStore.addGoal(goal);
				rebuild();
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(this, "Invalid target value.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static String formatNumber(int n)
	{
		if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
		if (n >= 1_000) return String.format("%.0fK", n / 1_000.0);
		return String.valueOf(n);
	}
}

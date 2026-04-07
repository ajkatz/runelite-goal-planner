package com.goaltracker.ui;

import net.runelite.api.Experience;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A two-row form with synchronized Level and XP fields. Editing either field
 * updates the other on the fly so the user can target a level or a raw XP value
 * with whichever feels more natural. Both rows always reflect the same goal.
 *
 * <p>Used by both the panel Add Goal dialog (skill type) and the Skills-tab
 * right-click "Add Goal" handler so the UX is identical in both places.
 */
public class SkillTargetForm extends JPanel
{
	private final JTextField levelField = new JTextField(8);
	private final JTextField xpField = new JTextField(8);

	/** Suppress the cross-update listener while we programmatically set a field. */
	private boolean syncing = false;

	public SkillTargetForm(int initialLevel)
	{
		super(new GridBagLayout());
		setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 4, 3, 4);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		JLabel levelLabel = new JLabel("Level:");
		levelLabel.setPreferredSize(new Dimension(60, 24));
		add(levelLabel, gbc);

		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		add(levelField, gbc);

		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		JLabel xpLabel = new JLabel("XP:");
		xpLabel.setPreferredSize(new Dimension(60, 24));
		add(xpLabel, gbc);

		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
		add(xpField, gbc);

		// Wire bidirectional sync via DocumentListeners. The `syncing` guard prevents
		// the level→xp update from re-triggering xp→level (and vice versa).
		levelField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
			if (syncing) return;
			Integer level = parseInt(levelField.getText());
			if (level == null || level < 1 || level > 126) return;
			int xp = level <= 99
				? Experience.getXpForLevel(level)
				: Experience.getXpForLevel(99); // virtual levels capped at 99 XP table
			syncing = true;
			try { xpField.setText(Integer.toString(xp)); }
			finally { syncing = false; }
		}));

		xpField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
			if (syncing) return;
			Integer xp = parseInt(xpField.getText());
			if (xp == null || xp < 0 || xp > 200_000_000) return;
			int level = Experience.getLevelForXp(xp);
			syncing = true;
			try { levelField.setText(Integer.toString(level)); }
			finally { syncing = false; }
		}));

		// Seed both fields from the initial level. The level setter triggers the
		// listener which fills in the XP field.
		levelField.setText(Integer.toString(initialLevel));
	}

	/** @return target XP currently represented by the form, or -1 if invalid. */
	public int getTargetXp()
	{
		Integer xp = parseInt(xpField.getText());
		if (xp == null || xp < 0 || xp > 200_000_000) return -1;
		return xp;
	}

	private static Integer parseInt(String s)
	{
		if (s == null) return null;
		String trimmed = s.trim().replace(",", "");
		if (trimmed.isEmpty()) return null;
		try { return Integer.parseInt(trimmed); }
		catch (NumberFormatException e) { return null; }
	}

	/** Adapter so we can supply a single Runnable instead of three method overrides. */
	private static class SimpleDocumentListener implements DocumentListener
	{
		private final Runnable onChange;
		SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
		@Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
		@Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
		@Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
	}
}

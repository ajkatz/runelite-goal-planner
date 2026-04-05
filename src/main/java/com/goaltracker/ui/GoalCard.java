package com.goaltracker.ui;

import javax.swing.JPanel;

/**
 * Individual goal card with gradient progress fill.
 *
 * The card IS the progress bar — color fills left-to-right with
 * increasing saturation as progress grows.
 *
 * Paint logic:
 * 1. Fill width = cardWidth * (progress / 100)
 * 2. Color alpha/saturation = lerp(0.15, 1.0, progress / 100)
 * 3. Base color from GoalType.getColor()
 */
public class GoalCard extends JPanel
{
	// TODO: Custom paint, goal info display, right-click menu
}

package com.goalplanner;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GoalPlannerPluginTest
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		// Loads the demo sender too so a goalplanner:import-share lands on login —
		// proves Goal Planner as a Postie cross-plugin action consumer.
		ExternalPluginManager.loadBuiltin(GoalPlannerPlugin.class, PostieDemoSenderPlugin.class);
		RuneLite.main(args);
	}
}

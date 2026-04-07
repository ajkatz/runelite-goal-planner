package com.goaltracker.tracker;

import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Tracks combat achievement completion via the bit-packed CA_TASK_COMPLETED
 * varplayers. OSRS stores CA completion across 20 varplayers (32 bits each =
 * 640 task slots). For task id N, the completion bit is at index N % 32 of
 * varp CA_TASK_COMPLETED_(N / 32).
 *
 * The wiki id is the canonical task id and is set on the Goal at creation time
 * (see GoalTrackerPlugin.buildCombatAchievementGoal). Goals with caTaskId &lt; 0
 * (legacy or unmatched) are skipped.
 */
@Slf4j
@Singleton
public class CombatAchievementTracker
{
	/**
	 * Lookup table for the 20 CA completion varplayers, indexed by (taskId / 32).
	 * Order matches the named constants CA_TASK_COMPLETED_0 through CA_TASK_COMPLETED_19.
	 */
	private static final int[] CA_VARPS = {
		VarPlayerID.CA_TASK_COMPLETED_0,
		VarPlayerID.CA_TASK_COMPLETED_1,
		VarPlayerID.CA_TASK_COMPLETED_2,
		VarPlayerID.CA_TASK_COMPLETED_3,
		VarPlayerID.CA_TASK_COMPLETED_4,
		VarPlayerID.CA_TASK_COMPLETED_5,
		VarPlayerID.CA_TASK_COMPLETED_6,
		VarPlayerID.CA_TASK_COMPLETED_7,
		VarPlayerID.CA_TASK_COMPLETED_8,
		VarPlayerID.CA_TASK_COMPLETED_9,
		VarPlayerID.CA_TASK_COMPLETED_10,
		VarPlayerID.CA_TASK_COMPLETED_11,
		VarPlayerID.CA_TASK_COMPLETED_12,
		VarPlayerID.CA_TASK_COMPLETED_13,
		VarPlayerID.CA_TASK_COMPLETED_14,
		VarPlayerID.CA_TASK_COMPLETED_15,
		VarPlayerID.CA_TASK_COMPLETED_16,
		VarPlayerID.CA_TASK_COMPLETED_17,
		VarPlayerID.CA_TASK_COMPLETED_18,
		VarPlayerID.CA_TASK_COMPLETED_19,
	};

	private static final int MAX_TASK_ID = CA_VARPS.length * 32 - 1; // 639

	private final Client client;
	private final GoalTrackerApiImpl api;

	@Inject
	public CombatAchievementTracker(Client client, GoalTrackerApiImpl api)
	{
		this.client = client;
		this.api = api;
	}

	public boolean checkGoals(List<Goal> goals)
	{
		boolean anyUpdated = false;

		for (Goal goal : goals)
		{
			if (goal.getType() != GoalType.COMBAT_ACHIEVEMENT
				|| goal.getStatus() != GoalStatus.ACTIVE)
			{
				continue;
			}

			int taskId = goal.getCaTaskId();
			if (taskId < 0 || taskId > MAX_TASK_ID)
			{
				// Legacy goal without a task id, or task id out of range.
				continue;
			}

			int varpId = CA_VARPS[taskId / 32];
			int bitIndex = taskId % 32;
			int varpValue = client.getVarpValue(varpId);
			boolean done = ((varpValue >> bitIndex) & 1) == 1;
			int newValue = done ? 1 : 0;
			if (api.recordGoalProgress(goal.getId(), newValue))
			{
				anyUpdated = true;
			}
		}

		return anyUpdated;
	}
}

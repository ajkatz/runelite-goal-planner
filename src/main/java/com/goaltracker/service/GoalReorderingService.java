package com.goaltracker.service;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalStatus;
import com.goaltracker.model.GoalType;
import com.goaltracker.persistence.GoalStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Handles goal reordering logic including recursive skill chain movement.
 *
 * Rules:
 * - Goals move independently through unrelated goals
 * - If a move would violate same-skill ordering (lower target must be above higher),
 *   partners recursively move first from end of chain inward
 */
@Singleton
public class GoalReorderingService
{
	private final GoalStore goalStore;

	@Inject
	public GoalReorderingService(GoalStore goalStore)
	{
		this.goalStore = goalStore;
	}

	/**
	 * Move a goal one position up or down, respecting skill chain ordering.
	 */
	public void moveGoal(int fromIndex, int toIndex)
	{
		List<Goal> goals = goalStore.getGoals();
		if (toIndex < 0 || toIndex >= goals.size())
		{
			return;
		}

		boolean movingUp = toIndex < fromIndex;
		Goal moving = goals.get(fromIndex);

		makeRoom(moving, fromIndex, movingUp);

		goals = goalStore.getGoals();
		int currentIndex = goals.indexOf(moving);
		int newTarget = movingUp ? currentIndex - 1 : currentIndex + 1;

		if (newTarget >= 0 && newTarget < goals.size())
		{
			goalStore.reorder(currentIndex, newTarget);
		}
		// goalStore.reorder() already persists; no extra save needed.
	}

	/**
	 * Move a goal directly to a specific position (Move to Top/Bottom).
	 * Enforces skill ordering after placement.
	 */
	public void moveGoalTo(int fromIndex, int toIndex)
	{
		List<Goal> goals = goalStore.getGoals();
		if (fromIndex < 0 || fromIndex >= goals.size()
			|| toIndex < 0 || toIndex >= goals.size()
			|| fromIndex == toIndex)
		{
			return;
		}

		goalStore.reorder(fromIndex, toIndex);
		enforceSkillOrdering();
		// goalStore.reorder() (called above and inside enforceSkillOrdering) persists.
	}

	/**
	 * Enforce same-skill ordering across every section. Each section is
	 * processed independently so we never swap goals across section boundaries.
	 */
	public void enforceSkillOrdering()
	{
		java.util.Set<String> sectionIds = new java.util.HashSet<>();
		for (Goal g : goalStore.getGoals())
		{
			if (g.getSectionId() != null) sectionIds.add(g.getSectionId());
		}
		for (String sectionId : sectionIds)
		{
			enforceSkillOrderingInSection(sectionId);
		}
	}

	/**
	 * Enforce same-skill ordering within a single section: for any pair of
	 * same-skill goals, the one with the lower target must come first.
	 * Goals in other sections are not touched.
	 */
	public void enforceSkillOrderingInSection(String sectionId)
	{
		if (sectionId == null) return;
		List<Goal> goals = goalStore.getGoals();
		boolean fixed = true;
		int maxPasses = goals.size();

		while (fixed && maxPasses-- > 0)
		{
			fixed = false;
			for (int i = 0; i < goals.size(); i++)
			{
				Goal a = goals.get(i);
				if (!isActiveSkillGoal(a) || !sectionId.equals(a.getSectionId()))
				{
					continue;
				}
				for (int j = i + 1; j < goals.size(); j++)
				{
					Goal b = goals.get(j);
					if (!sectionId.equals(b.getSectionId())) continue;
					if (isSameSkillChain(a, b) && a.getTargetValue() > b.getTargetValue())
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
	}

	/**
	 * Find the correct insertion index for a new skill goal within the given
	 * section: just above the first same-skill same-section goal with a higher
	 * target. Returns -1 if no specific position needed (or sectionId is null).
	 * Section-scoped so the returned index never crosses section boundaries.
	 */
	public int findInsertionIndex(String skillName, int targetXp, String sectionId)
	{
		if (sectionId == null) return -1;
		List<Goal> goals = goalStore.getGoals();
		for (int i = 0; i < goals.size(); i++)
		{
			Goal g = goals.get(i);
			if (g.getType() == GoalType.SKILL
				&& skillName.equals(g.getSkillName())
				&& sectionId.equals(g.getSectionId())
				&& g.getStatus() != GoalStatus.COMPLETE
				&& g.getTargetValue() > targetXp)
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Recursively make room for a goal to move in a direction.
	 */
	private void makeRoom(Goal moving, int movingIndex, boolean movingUp)
	{
		if (!isActiveSkillGoal(moving))
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

		if (!isSameSkillChain(moving, neighbor))
		{
			return;
		}

		boolean wouldViolate;
		if (movingUp)
		{
			wouldViolate = moving.getTargetValue() > neighbor.getTargetValue();
		}
		else
		{
			wouldViolate = moving.getTargetValue() < neighbor.getTargetValue();
		}

		if (!wouldViolate)
		{
			return;
		}

		makeRoom(neighbor, targetIndex, movingUp);

		goals = goalStore.getGoals();
		int partnerIndex = goals.indexOf(neighbor);
		int partnerTarget = movingUp ? partnerIndex - 1 : partnerIndex + 1;

		if (partnerTarget >= 0 && partnerTarget < goals.size())
		{
			goalStore.reorder(partnerIndex, partnerTarget);
		}
	}

	private static boolean isActiveSkillGoal(Goal goal)
	{
		return goal.getType() == GoalType.SKILL
			&& goal.getSkillName() != null
			&& goal.getStatus() != GoalStatus.COMPLETE;
	}

	private static boolean isSameSkillChain(Goal a, Goal b)
	{
		return isActiveSkillGoal(a) && isActiveSkillGoal(b)
			&& a.getSkillName().equals(b.getSkillName());
	}
}

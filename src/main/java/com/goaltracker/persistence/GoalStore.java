package com.goaltracker.persistence;

import com.goaltracker.model.Goal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Persists goals using RuneLite's ConfigManager.
 * Goals are stored as JSON per-account, surviving client restarts.
 */
@Slf4j
@Singleton
public class GoalStore
{
	private static final String CONFIG_GROUP = "goaltracker";
	private static final String GOALS_KEY = "goals";
	private static final Gson GSON = new GsonBuilder().create();
	private static final Type GOAL_LIST_TYPE = new TypeToken<List<Goal>>(){}.getType();

	private final ConfigManager configManager;
	private List<Goal> goals = new ArrayList<>();

	@Inject
	public GoalStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public void load()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, GOALS_KEY);
		if (json != null && !json.isEmpty())
		{
			try
			{
				List<Goal> loaded = GSON.fromJson(json, GOAL_LIST_TYPE);
				if (loaded != null)
				{
					goals = new ArrayList<>(loaded);
					goals.sort(Comparator.comparingInt(Goal::getPriority));
					log.info("Loaded {} goals", goals.size());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to load goals", e);
				goals = new ArrayList<>();
			}
		}
	}

	public void save()
	{
		try
		{
			String json = GSON.toJson(goals);
			configManager.setConfiguration(CONFIG_GROUP, GOALS_KEY, json);
		}
		catch (Exception e)
		{
			log.error("Failed to save goals", e);
		}
	}

	public List<Goal> getGoals()
	{
		return goals;
	}

	public void addGoal(Goal goal)
	{
		goal.setPriority(goals.size());
		goals.add(goal);
		save();
	}

	public void removeGoal(String goalId)
	{
		goals.removeIf(g -> g.getId().equals(goalId));
		reindex();
		save();
	}

	public void updateGoal(Goal goal)
	{
		for (int i = 0; i < goals.size(); i++)
		{
			if (goals.get(i).getId().equals(goal.getId()))
			{
				goals.set(i, goal);
				break;
			}
		}
		save();
	}

	public void reorder(int fromIndex, int toIndex)
	{
		if (fromIndex < 0 || fromIndex >= goals.size() || toIndex < 0 || toIndex >= goals.size())
		{
			return;
		}
		Goal moved = goals.remove(fromIndex);
		goals.add(toIndex, moved);
		reindex();
		save();
	}

	private void reindex()
	{
		for (int i = 0; i < goals.size(); i++)
		{
			goals.get(i).setPriority(i);
		}
	}
}

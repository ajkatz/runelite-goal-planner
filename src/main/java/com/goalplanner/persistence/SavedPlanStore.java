package com.goalplanner.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Persists the user's library of saved share codes ("Saved Plans") via
 * RuneLite's {@link ConfigManager}.
 *
 * <p>Unlike goals, this library is <b>global</b> - it is not profile-scoped, so
 * the same set is visible on every account / leagues profile. A share code
 * carries goal <em>definitions</em> only (no account state), so a single
 * library is the right model.
 */
@Slf4j
@Singleton
public class SavedPlanStore
{
	private static final String CONFIG_GROUP = "goalplanner";
	/** Global key - deliberately NOT prefixed with the active profile. */
	private static final String KEY = "savedPlans";

	private final ConfigManager configManager;
	private final Gson gson;
	private final List<SavedPlan> plans = new ArrayList<>();

	@Inject
	public SavedPlanStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	/** Load the library from config. Call once at start-up. */
	public void load()
	{
		plans.clear();
		String json = configManager.getConfiguration(CONFIG_GROUP, KEY);
		if (json == null || json.isEmpty())
		{
			return;
		}
		try
		{
			List<SavedPlan> loaded = gson.fromJson(json,
				new TypeToken<List<SavedPlan>>(){}.getType());
			if (loaded != null)
			{
				for (SavedPlan p : loaded)
				{
					if (p != null && p.getCode() != null)
					{
						plans.add(p);
					}
				}
			}
		}
		catch (RuntimeException e)
		{
			log.warn("Could not parse saved plans; starting with an empty library", e);
		}
	}

	/** A snapshot copy of the saved plans, in insertion (oldest-first) order. */
	public List<SavedPlan> getPlans()
	{
		return new ArrayList<>(plans);
	}

	/** Bookmark a share code. {@code sectionNames} may be null (no overrides). */
	public SavedPlan add(String name, String code, List<String> sectionNames)
	{
		SavedPlan p = new SavedPlan();
		p.setId(UUID.randomUUID().toString());
		p.setName(name);
		p.setCode(code);
		p.setSectionNames(sectionNames != null ? new ArrayList<>(sectionNames) : new ArrayList<>());
		p.setSavedAt(System.currentTimeMillis());
		plans.add(p);
		persist();
		return p;
	}

	/** Rename a saved plan's bookmark label. Returns false if the id is unknown. */
	public boolean rename(String id, String name)
	{
		SavedPlan p = find(id);
		if (p == null)
		{
			return false;
		}
		p.setName(name);
		persist();
		return true;
	}

	/** Replace a saved plan's per-section display-name overrides. */
	public boolean setSectionNames(String id, List<String> sectionNames)
	{
		SavedPlan p = find(id);
		if (p == null)
		{
			return false;
		}
		p.setSectionNames(sectionNames != null ? new ArrayList<>(sectionNames) : new ArrayList<>());
		persist();
		return true;
	}

	/** Remove a saved plan by id. Returns false if nothing matched. */
	public boolean remove(String id)
	{
		boolean removed = plans.removeIf(p -> p.getId() != null && p.getId().equals(id));
		if (removed)
		{
			persist();
		}
		return removed;
	}

	private SavedPlan find(String id)
	{
		if (id == null)
		{
			return null;
		}
		for (SavedPlan p : plans)
		{
			if (id.equals(p.getId()))
			{
				return p;
			}
		}
		return null;
	}

	private void persist()
	{
		configManager.setConfiguration(CONFIG_GROUP, KEY, gson.toJson(plans));
	}
}

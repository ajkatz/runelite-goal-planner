package com.goalplanner.data;

import com.goalplanner.model.ItemTag;
import com.goalplanner.model.TagCategory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps item IDs to their drop sources (boss / raid / clue / minigame), used to
 * auto-seed source tags on collection-log item goals.
 *
 * <p>The data - 3,785 (itemId, source, category) rows covering inventory,
 * noted, collection-log, and follower item variants, generated from the OSRS
 * Wiki Collection Log - lives in the {@code item-sources.tsv} resource beside
 * this class rather than inline, and is loaded once into {@link #SOURCES} on
 * class init. Format: {@code itemId<TAB>label<TAB>TagCategory} per line.
 */
public final class ItemSourceData
{
	private ItemSourceData() {}

	private static final String RESOURCE = "item-sources.tsv";
	private static final Map<Integer, List<ItemTag>> SOURCES = new HashMap<>();

	public static List<ItemTag> getTags(int itemId)
	{
		return SOURCES.getOrDefault(itemId, Collections.emptyList());
	}

	private static void source(int itemId, String label, TagCategory category)
	{
		SOURCES.computeIfAbsent(itemId, k -> new ArrayList<>()).add(new ItemTag(label, category));
	}

	static
	{
		try (InputStream in = ItemSourceData.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				throw new IllegalStateException("missing resource: " + RESOURCE);
			}
			try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
			{
				String line;
				while ((line = r.readLine()) != null)
				{
					if (line.isEmpty())
					{
						continue;
					}
					// itemId \t label \t category - labels never contain tabs.
					int t1 = line.indexOf('\t');
					int t2 = line.indexOf('\t', t1 + 1);
					int itemId = Integer.parseInt(line.substring(0, t1));
					String label = line.substring(t1 + 1, t2);
					TagCategory category = TagCategory.valueOf(line.substring(t2 + 1));
					source(itemId, label, category);
				}
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("failed to load " + RESOURCE, e);
		}
	}
}

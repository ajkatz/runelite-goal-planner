package com.goaltracker.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fetches and caches combat achievement metadata from the OSRS Wiki.
 *
 * Data source: the wiki's public "bucket" API, backed by the combat_achievement table
 * consumed by Module:Combat_Achievements. One query returns all ~650 CAs with
 * name/task/tier/monster/type.
 *
 * Cache is stored in ConfigManager (same pattern as GoalStore) and refreshed when
 * older than {@link #CACHE_MAX_AGE_MS}. A fetch failure falls back to the existing
 * cache if present, so offline startup still works once seeded.
 */
@Slf4j
@Singleton
public class WikiCaRepository
{
	private static final String CONFIG_GROUP = "goaltracker";
	private static final String CACHE_KEY = "caWikiCache";
	private static final String CACHE_TS_KEY = "caWikiCacheTs";
	private static final long CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000; // 7 days

	private static final HttpUrl WIKI_API = HttpUrl.parse("https://oldschool.runescape.wiki/api.php");
	private static final String BUCKET_QUERY =
		"bucket('combat_achievement')"
			+ ".select('name','task','tier','monster','type')"
			+ ".limit(5000).run()";

	private final OkHttpClient httpClient;
	private final ConfigManager configManager;
	private final Gson gson = new Gson();

	/** Keyed by lowercased CA name for case-insensitive lookup. */
	private volatile Map<String, CaInfo> byName = Collections.emptyMap();

	@Inject
	public WikiCaRepository(OkHttpClient httpClient, ConfigManager configManager)
	{
		this.httpClient = httpClient;
		this.configManager = configManager;
	}

	/**
	 * Load cached data immediately (if present), then refresh from the wiki if the
	 * cache is stale. Safe to call on plugin startup from any thread.
	 */
	public void loadAsync()
	{
		// 1. Load from cache synchronously so callers see populated data ASAP.
		String cachedJson = configManager.getConfiguration(CONFIG_GROUP, CACHE_KEY);
		if (cachedJson != null && !cachedJson.isEmpty())
		{
			try
			{
				byName = parseBucketJson(cachedJson);
				log.debug("Loaded {} CA entries from cache", byName.size());
			}
			catch (Exception e)
			{
				log.warn("Failed to parse cached CA data, will refetch", e);
			}
		}

		// 2. Check cache freshness; fetch if stale or missing.
		long cachedAt = parseLong(configManager.getConfiguration(CONFIG_GROUP, CACHE_TS_KEY));
		long age = System.currentTimeMillis() - cachedAt;
		if (cachedAt == 0 || age > CACHE_MAX_AGE_MS)
		{
			fetchFromWiki();
		}
	}

	/**
	 * Look up a CA by name. Returns null if not in the repository.
	 * Name comparison is case-insensitive to tolerate minor formatting differences.
	 */
	public CaInfo get(String name)
	{
		if (name == null) return null;
		return byName.get(name.toLowerCase(Locale.ROOT));
	}

	/** Number of entries currently loaded. */
	public int size()
	{
		return byName.size();
	}

	private void fetchFromWiki()
	{
		HttpUrl url = WIKI_API.newBuilder()
			.addQueryParameter("action", "bucket")
			.addQueryParameter("format", "json")
			.addQueryParameter("query", BUCKET_QUERY)
			.build();

		Request request = new Request.Builder()
			.url(url)
			.header("User-Agent", "runelite-goal-tracker (plugin)")
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("CA wiki fetch failed: {}", e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try (Response r = response)
				{
					if (!r.isSuccessful() || r.body() == null)
					{
						log.warn("CA wiki fetch HTTP {}", r.code());
						return;
					}
					String body = r.body().string();
					Map<String, CaInfo> parsed = parseBucketJson(body);
					if (parsed.isEmpty())
					{
						log.warn("CA wiki fetch returned no rows");
						return;
					}
					byName = parsed;
					configManager.setConfiguration(CONFIG_GROUP, CACHE_KEY, body);
					configManager.setConfiguration(CONFIG_GROUP, CACHE_TS_KEY,
						Long.toString(System.currentTimeMillis()));
					log.info("Fetched {} CA entries from wiki", parsed.size());
				}
				catch (Exception e)
				{
					log.warn("Failed to parse CA wiki response", e);
				}
			}
		});
	}

	/**
	 * Parse a wiki bucket-API response body into a name-keyed map.
	 * Expected shape: {"bucketQuery": "...", "bucket": [ {name, task, tier, monster, type}, ... ]}
	 */
	private Map<String, CaInfo> parseBucketJson(String body)
	{
		JsonElement root = new JsonParser().parse(body);
		if (!root.isJsonObject()) return Collections.emptyMap();
		JsonElement bucketEl = root.getAsJsonObject().get("bucket");
		if (bucketEl == null || !bucketEl.isJsonArray()) return Collections.emptyMap();

		JsonArray rows = bucketEl.getAsJsonArray();
		Map<String, CaInfo> out = new HashMap<>(rows.size() * 2);
		for (JsonElement el : rows)
		{
			if (!el.isJsonObject()) continue;
			JsonObject o = el.getAsJsonObject();
			CaInfo info = new CaInfo();
			info.name = asString(o, "name");
			info.task = asString(o, "task");
			info.tier = asString(o, "tier");
			info.monster = asString(o, "monster");
			info.type = asString(o, "type");
			if (info.name != null && !info.name.isEmpty())
			{
				out.put(info.name.toLowerCase(Locale.ROOT), info);
			}
		}
		return out;
	}

	private static String asString(JsonObject o, String key)
	{
		JsonElement e = o.get(key);
		return (e == null || e.isJsonNull()) ? null : e.getAsString();
	}

	private static long parseLong(String s)
	{
		if (s == null || s.isEmpty()) return 0;
		try { return Long.parseLong(s); }
		catch (NumberFormatException e) { return 0; }
	}

	/**
	 * Lightweight value object for a single combat achievement.
	 * Fields are public for direct access (no model-level concerns here).
	 */
	public static class CaInfo
	{
		public String name;
		public String task;    // full description text (e.g. "Kill Amoxiatl in less than 30 seconds.")
		public String tier;    // "Easy", "Medium", ...
		public String monster; // raw monster field from the wiki
		public String type;    // "Kill Count", "Perfection", ...
	}
}

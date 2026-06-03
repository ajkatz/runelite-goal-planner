package com.goalplanner.postie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;

/**
 * Postie — a self-contained cross-plugin action-sharing convention + helper over
 * RuneLite's {@link PluginMessage}.
 *
 * <p>Vendored inside Goal Planner as its own module: it depends <b>only on
 * RuneLite core types</b> (no Goal Planner classes), so it can be lifted out into
 * a standalone library/plugin later with no changes.
 *
 * <p><b>Convention</b> (the same shape shortest-path / quest-helper interop with):
 * <ul>
 *   <li><b>namespace</b> = the target plugin's config-group id</li>
 *   <li><b>name</b> = the action verb (e.g. {@code "import-share"})</li>
 *   <li><b>data</b> = a {@code String -> Object} map payload</li>
 * </ul>
 * Receivers own their namespace + the set of actions they accept (their "SDK");
 * senders address {@code namespace + name} and fill the data map. The Map payload
 * crosses the plugin classloader boundary with no shared types, so consumers need
 * no compile-time dependency on a sender (or on Postie).
 */
public final class Postie
{
	/** Payload key carrying the envelope version. */
	public static final String VERSION_KEY = "postie.v";
	/** Current envelope version. */
	public static final int VERSION = 1;

	/** Reserved namespace for the discovery handshake. */
	public static final String POSTIE_NAMESPACE = "postie";
	/** Discovery request action. */
	public static final String DISCOVER = "discover";
	/** Discovery response action. */
	public static final String ANNOUNCE = "announce";
	/** ANNOUNCE payload key: announcer namespace. */
	public static final String KEY_NAMESPACE = "namespace";
	/** ANNOUNCE payload key: announcer action names ({@code List<String>}). */
	public static final String KEY_ACTIONS = "actions";

	private Postie()
	{
	}

	/** Build the {@link PluginMessage} for an action (pure — no dispatch). */
	public static PluginMessage action(String targetNamespace, String name, Map<String, Object> data)
	{
		Map<String, Object> payload = new HashMap<>();
		if (data != null)
		{
			payload.putAll(data);
		}
		payload.put(VERSION_KEY, VERSION);
		return new PluginMessage(targetNamespace, name, payload);
	}

	/** Dispatch an action to a target plugin over the event bus. */
	public static void call(EventBus eventBus, String targetNamespace, String name, Map<String, Object> data)
	{
		eventBus.post(action(targetNamespace, name, data));
	}

	/** Convenience for receivers: is this message addressed to my namespace + action? */
	public static boolean isAction(PluginMessage msg, String myNamespace, String name)
	{
		return myNamespace.equals(msg.getNamespace()) && name.equals(msg.getName());
	}

	// --- discovery: "who's on the bus, and what do they accept?" ---

	/** Broadcast a discovery request; discoverable plugins reply via {@link #announce}. */
	public static void discover(EventBus eventBus)
	{
		eventBus.post(action(POSTIE_NAMESPACE, DISCOVER, Map.of()));
	}

	/** Announce this plugin's namespace + accepted actions (a reply to DISCOVER). */
	public static void announce(EventBus eventBus, String myNamespace, List<String> actions)
	{
		Map<String, Object> data = new HashMap<>();
		data.put(KEY_NAMESPACE, myNamespace);
		data.put(KEY_ACTIONS, actions != null ? new ArrayList<>(actions) : new ArrayList<>());
		eventBus.post(action(POSTIE_NAMESPACE, ANNOUNCE, data));
	}

	public static boolean isDiscover(PluginMessage msg)
	{
		return POSTIE_NAMESPACE.equals(msg.getNamespace()) && DISCOVER.equals(msg.getName());
	}

	public static boolean isAnnounce(PluginMessage msg)
	{
		return POSTIE_NAMESPACE.equals(msg.getNamespace()) && ANNOUNCE.equals(msg.getName());
	}

	public static String announcedNamespace(PluginMessage msg)
	{
		Object v = msg.getData().get(KEY_NAMESPACE);
		return v instanceof String ? (String) v : null;
	}

	@SuppressWarnings("unchecked")
	public static List<String> announcedActions(PluginMessage msg)
	{
		Object v = msg.getData().get(KEY_ACTIONS);
		return v instanceof List ? (List<String>) v : List.of();
	}
}

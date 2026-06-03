package com.goalplanner;

import com.goalplanner.postie.Postie;
import com.goalplanner.share.GoalShareDto;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.goalplanner.share.TagShareDto;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Dev-only demo: stands in for a 3rd-party plugin using the {@link Postie}
 * convention. On login it <b>discovers</b> who's on the bus; when Goal Planner
 * announces it accepts {@code import-share}, it hands over a whole section (7
 * goals, a tag, internal relations) via {@code goalplanner:import-share}.
 *
 * <p>Shows the full flow — discover → announce → call — and that the in-client
 * {@link PluginMessage} path has no size limit (the code is hundreds of chars).
 * Test sources only; never shipped.
 */
@PluginDescriptor(name = "Postie Demo Sender (goalplanner)")
public class PostieDemoSenderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	private boolean done;

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN && !done)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"Postie demo: discover — who accepts actions?", null);
			Postie.discover(eventBus);
		}
	}

	@Subscribe
	public void onPluginMessage(PluginMessage msg)
	{
		if (done || !Postie.isAnnounce(msg))
		{
			return;
		}
		String ns = Postie.announcedNamespace(msg);
		List<String> actions = Postie.announcedActions(msg);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"Postie demo: discovered '" + ns + "' actions " + actions, null);

		if ("goalplanner".equals(ns) && actions.contains("import-share"))
		{
			done = true;
			ShareBundle bundle = infernoPrepSection();
			String code = new ShareCodec(new Gson()).encode(bundle);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"Postie demo: sending a " + bundle.getGoals().size() + "-goal section ("
					+ code.length() + "-char code) via goalplanner:import-share", null);
			Postie.call(eventBus, "goalplanner", "import-share", Map.of("code", code));
		}
	}

	/** A chunky shared section: 5 skill goals + an item + a boss that requires the skills, plus a tag. */
	private static ShareBundle infernoPrepSection()
	{
		TagShareDto inferno = new TagShareDto();
		inferno.setLabel("Inferno");
		inferno.setCategory("OTHER");
		inferno.setColorRgb(0xAA3322);

		List<GoalShareDto> goals = new ArrayList<>();
		goals.add(skill(0, "Ranged", "RANGED", 75, Collections.singletonList(inferno)));
		goals.add(skill(1, "Defence", "DEFENCE", 70, null));
		goals.add(skill(2, "Prayer", "PRAYER", 70, null));
		goals.add(skill(3, "Magic", "MAGIC", 75, null));
		goals.add(skill(4, "Hitpoints", "HITPOINTS", 75, null));

		GoalShareDto brews = new GoalShareDto();
		brews.setRef(5);
		brews.setType("ITEM_GRIND");
		brews.setName("Saradomin brews x12");
		brews.setItemId(6685);
		brews.setTargetValue(12);
		goals.add(brews);

		GoalShareDto zuk = new GoalShareDto();
		zuk.setRef(6);
		zuk.setType("BOSS");
		zuk.setName("TzKal-Zuk");
		zuk.setBossName("TzKal-Zuk");
		zuk.setTargetValue(1);
		zuk.setTags(Collections.singletonList(inferno));
		zuk.setRequires(Arrays.asList(0, 1, 2, 3, 4));
		goals.add(zuk);

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Inferno Prep (Postie demo)");
		bundle.setSharedBy("Postie demo");
		bundle.setGoals(goals);
		return bundle;
	}

	private static GoalShareDto skill(int ref, String display, String skill, int level, List<TagShareDto> tags)
	{
		GoalShareDto g = new GoalShareDto();
		g.setRef(ref);
		g.setType("SKILL");
		g.setName(display + " - Level " + level);
		g.setSkillName(skill);
		g.setTargetValue(Experience.getXpForLevel(level));
		if (tags != null)
		{
			g.setTags(tags);
		}
		return g;
	}
}

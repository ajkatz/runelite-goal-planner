package com.goalplanner;

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
 * Dev-only demo: stands in for a 3rd-party plugin using the Postie convention to
 * hand Goal Planner a <b>whole section</b> — multiple goals, a tag, and internal
 * relations. On login it builds the bundle, encodes it, and posts
 * {@code goalplanner:import-share}; Goal Planner imports the lot.
 *
 * <p>Demonstrates that the in-client {@link PluginMessage} path has <b>no size
 * limit</b> — the code here is hundreds of chars, which could never fit in game
 * chat (80) and would be tight even over Party. Test sources only; never shipped.
 */
@PluginDescriptor(name = "Postie Demo Sender (goalplanner)")
public class PostieDemoSenderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	private boolean sent;

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() != GameState.LOGGED_IN || sent)
		{
			return;
		}
		sent = true;

		ShareBundle bundle = infernoPrepSection();
		String code = new ShareCodec(new Gson()).encode(bundle);

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"Postie demo: sending a " + bundle.getGoals().size() + "-goal section ("
				+ code.length() + "-char code — would never fit in game chat) via goalplanner:import-share",
			null);
		eventBus.post(new PluginMessage("goalplanner", "import-share", Map.of("code", code)));
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
		// Zuk requires all five skill goals (encoded as in-bundle refs).
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

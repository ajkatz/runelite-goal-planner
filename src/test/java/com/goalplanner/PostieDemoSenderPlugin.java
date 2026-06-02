package com.goalplanner;

import com.goalplanner.share.GoalShareDto;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Dev-only demo: stands in for a 3rd-party plugin using the Postie convention to
 * hand Goal Planner a share bundle. On login it builds a one-goal bundle, encodes
 * it, and posts a {@link PluginMessage} to {@code goalplanner:import-share} — Goal
 * Planner's {@code onPluginMessage} handler then imports it. Test sources only;
 * never shipped. (Note: it depends on nothing from Postie — just the convention.)
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

		GoalShareDto goal = new GoalShareDto();
		goal.setRef(0);
		goal.setType("SKILL");
		goal.setName("Postie test — Cooking 50");
		goal.setSkillName("COOKING");
		goal.setTargetValue(101_333);

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.GOALS);
		bundle.setSharedBy("Postie demo");
		bundle.setGoals(Collections.singletonList(goal));

		String code = new ShareCodec(new Gson()).encode(bundle);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"Postie demo: posting goalplanner:import-share", null);
		eventBus.post(new PluginMessage("goalplanner", "import-share", Map.of("code", code)));
	}
}

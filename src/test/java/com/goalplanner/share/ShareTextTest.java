package com.goalplanner.share;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The instruction-led invite line: readable for non-plugin users, and still
 * decodable back to the bundle by a plugin user.
 */
public class ShareTextTest
{
	private final ShareCodec codec = new ShareCodec(new Gson());

	private static ShareBundle sectionBundle()
	{
		GoalShareDto a = new GoalShareDto();
		a.setRef(0);
		a.setType("SKILL");
		a.setName("Ranged - Level 75");
		GoalShareDto b = new GoalShareDto();
		b.setRef(1);
		b.setType("BOSS");
		b.setName("TzKal-Zuk");

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Inferno Prep");
		bundle.setSharedBy("Andrew");
		bundle.setGoals(Arrays.asList(a, b));
		return bundle;
	}

	@Test
	public void inviteLineReadsAsAnInstructionForNonPluginUsers()
	{
		String line = ShareText.invite(sectionBundle(), codec.encode(sectionBundle()));
		assertTrue(line.startsWith("[Goal Planner]"));
		assertTrue(line.contains("Andrew shared"));
		assertTrue(line.contains("\"Inferno Prep\""));
		assertTrue(line.contains("2 goals"));
		assertTrue(line.contains("get the Goal Planner RuneLite plugin to import"));
	}

	@Test
	public void inviteLineRemainsDecodableByPluginUsers()
	{
		ShareBundle bundle = sectionBundle();
		String line = ShareText.invite(bundle, codec.encode(bundle));
		// A plugin user pastes the whole line; the code is still recovered.
		assertEquals(bundle, codec.decode(line));
	}

	@Test
	public void fallsBackToSomeoneWhenSharerUnknown()
	{
		ShareBundle bundle = sectionBundle();
		bundle.setSharedBy(null);
		assertTrue(ShareText.invite(bundle, "GPSHARE1:x").contains("Someone shared"));
	}

	@Test
	public void summarisesLooseGoalsWithoutASectionName()
	{
		GoalShareDto only = new GoalShareDto();
		only.setRef(0);
		only.setType("SKILL");
		only.setName("Cooking - Level 50");
		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.GOALS);
		bundle.setGoals(Collections.singletonList(only));

		String line = ShareText.invite(bundle, "GPSHARE1:x");
		assertTrue("singular goal count", line.contains("1 goal"));
		assertTrue("no section quoting for loose goals", !line.contains("\""));
	}
}

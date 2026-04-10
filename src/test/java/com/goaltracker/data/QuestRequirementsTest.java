package com.goaltracker.data;

import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link QuestRequirements} data table and the
 * {@link QuestRequirementResolver} live-state filtering layer.
 *
 * <p>The resolver's core overload takes functional lookups so tests
 * can supply fake skill levels and quest states without mocking the
 * RuneLite {@link net.runelite.api.Client} or {@link Quest#getState}.
 */
class QuestRequirementsTest
{
	@Nested
	@DisplayName("QuestRequirements.lookup / hasRequirements")
	class DataTableTests
	{
		@Test
		@DisplayName("returns null for a quest that is not in the table")
		void nullForMissingQuest()
		{
			// Use a quest known to NOT be in the table (RFD sub-quests
			// are not individually trackable in the same way).
			assertNull(QuestRequirements.lookup(null));
			assertFalse(QuestRequirements.hasRequirements(null));
		}

		@Test
		@DisplayName("returns empty Reqs for Cook's Assistant (baseline in table)")
		void emptyReqsForCooksAssistant()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.COOKS_ASSISTANT);
			assertNotNull(reqs);
			assertTrue(reqs.skills.isEmpty());
			assertTrue(reqs.prereqQuests.isEmpty());
			assertEquals(0, reqs.questPoints);
			// hasRequirements returns false even though the quest is in the
			// table — the Reqs are empty, so the secondary menu entry
			// (which gates on hasRequirements) won't render.
			assertFalse(QuestRequirements.hasRequirements(Quest.COOKS_ASSISTANT));
		}

		@Test
		@DisplayName("Horror from the Deep has one skill req and the Barcrawl miniquest")
		void hftdSingleSkillReqPlusBarcrawl()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.HORROR_FROM_THE_DEEP);
			assertNotNull(reqs);
			assertEquals(1, reqs.skills.size());
			assertEquals(Skill.AGILITY, reqs.skills.get(0).skill);
			assertEquals(35, reqs.skills.get(0).level);
			assertEquals(1, reqs.prereqQuests.size());
			assertEquals(Quest.ALFRED_GRIMHANDS_BARCRAWL, reqs.prereqQuests.get(0));
			assertTrue(QuestRequirements.hasRequirements(Quest.HORROR_FROM_THE_DEEP));
		}

		@Test
		@DisplayName("Dream Mentor records its 85 Combat requirement (stubbed)")
		void dreamMentorRecordsCombatLevel()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.DREAM_MENTOR);
			assertNotNull(reqs);
			// Wiki-confirmed: Dream Mentor's ONLY hard gate is 85 Combat
			// (no skill reqs, no QP). The earlier hand-entry of 125 QP
			// was incorrect.
			assertEquals(0, reqs.questPoints);
			assertEquals(85, reqs.combatLevel);
			assertTrue(QuestRequirements.hasRequirements(Quest.DREAM_MENTOR));
		}

		@Test
		@DisplayName("Dragon Slayer II has many skill and quest prereqs + 200 QP")
		void ds2IsTheStressTest()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.DRAGON_SLAYER_II);
			assertNotNull(reqs);
			assertTrue(reqs.skills.size() >= 5);
			assertTrue(reqs.prereqQuests.size() >= 5);
			assertEquals(200, reqs.questPoints);
		}

		@Test
		@DisplayName("Secrets of the North has 3 skills + 4 quest prereqs")
		void secretsOfTheNorthRequirements()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.SECRETS_OF_THE_NORTH);
			assertNotNull(reqs);
			assertEquals(3, reqs.skills.size());
			assertEquals(69, reqs.skills.get(0).level); // Agility
			assertEquals(Skill.AGILITY, reqs.skills.get(0).skill);
			assertEquals(4, reqs.prereqQuests.size());
			assertTrue(reqs.prereqQuests.contains(Quest.MAKING_FRIENDS_WITH_MY_ARM));
			assertTrue(reqs.prereqQuests.contains(Quest.THE_GENERALS_SHADOW));
			assertTrue(reqs.prereqQuests.contains(Quest.DEVIOUS_MINDS));
			assertTrue(reqs.prereqQuests.contains(Quest.HAZEEL_CULT));
			assertTrue(QuestRequirements.hasRequirements(Quest.SECRETS_OF_THE_NORTH));
		}

		@Test
		@DisplayName("The Garden of Death has Farming 20, no quest prereqs")
		void gardenOfDeathRequirements()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.THE_GARDEN_OF_DEATH);
			assertNotNull(reqs);
			assertEquals(1, reqs.skills.size());
			assertEquals(Skill.FARMING, reqs.skills.get(0).skill);
			assertEquals(20, reqs.skills.get(0).level);
			assertTrue(reqs.prereqQuests.isEmpty());
			assertTrue(QuestRequirements.hasRequirements(Quest.THE_GARDEN_OF_DEATH));
		}

		@Test
		@DisplayName("His Faithful Servants requires Priest in Peril")
		void hisFaithfulServantsRequirements()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.HIS_FAITHFUL_SERVANTS);
			assertNotNull(reqs);
			assertTrue(reqs.skills.isEmpty());
			assertEquals(1, reqs.prereqQuests.size());
			assertEquals(Quest.PRIEST_IN_PERIL, reqs.prereqQuests.get(0));
			assertTrue(QuestRequirements.hasRequirements(Quest.HIS_FAITHFUL_SERVANTS));
		}

		@Test
		@DisplayName("Swan Song has 100 QP requirement")
		void swanSongQuestPoints()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.SWAN_SONG);
			assertNotNull(reqs);
			assertEquals(100, reqs.questPoints);
			assertEquals(6, reqs.skills.size());
			assertTrue(reqs.prereqQuests.contains(Quest.ONE_SMALL_FAVOUR));
			assertTrue(reqs.prereqQuests.contains(Quest.GARDEN_OF_TRANQUILLITY));
		}

		@Test
		@DisplayName("Making Friends with My Arm has 4 skills + 4 quest prereqs")
		void makingFriendsRequirements()
		{
			QuestRequirements.Reqs reqs = QuestRequirements.lookup(Quest.MAKING_FRIENDS_WITH_MY_ARM);
			assertNotNull(reqs);
			assertEquals(4, reqs.skills.size());
			assertEquals(4, reqs.prereqQuests.size());
			assertTrue(reqs.prereqQuests.contains(Quest.MY_ARMS_BIG_ADVENTURE));
			assertTrue(reqs.prereqQuests.contains(Quest.SWAN_SONG));
		}
	}

	@Nested
	@DisplayName("QuestRequirements.isF2P")
	class F2PTests
	{
		@Test
		@DisplayName("F2P quests are correctly identified")
		void f2pQuestsIdentified()
		{
			assertTrue(QuestRequirements.isF2P(Quest.COOKS_ASSISTANT));
			assertTrue(QuestRequirements.isF2P(Quest.DRAGON_SLAYER_I));
			assertTrue(QuestRequirements.isF2P(Quest.ERNEST_THE_CHICKEN));
			assertTrue(QuestRequirements.isF2P(Quest.THE_RESTLESS_GHOST));
			assertTrue(QuestRequirements.isF2P(Quest.X_MARKS_THE_SPOT));
			assertTrue(QuestRequirements.isF2P(Quest.BELOW_ICE_MOUNTAIN));
			assertTrue(QuestRequirements.isF2P(Quest.THE_IDES_OF_MILK));
		}

		@Test
		@DisplayName("members quests are not F2P")
		void membersQuestsNotF2P()
		{
			assertFalse(QuestRequirements.isF2P(Quest.DRAGON_SLAYER_II));
			assertFalse(QuestRequirements.isF2P(Quest.HORROR_FROM_THE_DEEP));
			assertFalse(QuestRequirements.isF2P(Quest.PLAGUE_CITY));
			assertFalse(QuestRequirements.isF2P(Quest.SONG_OF_THE_ELVES));
		}

		@Test
		@DisplayName("null returns false")
		void nullReturnsFalse()
		{
			assertFalse(QuestRequirements.isF2P(null));
		}
	}

	@Nested
	@DisplayName("QuestRequirementResolver.resolve (with injected lookups)")
	class ResolverTests
	{
		/** Default skill-level lookup: player is level 1 in everything. */
		private final ToIntFunction<Skill> NO_SKILLS = s -> 1;
		/** Default quest-state lookup: player has not started any quest. */
		private final Function<Quest, QuestState> NO_QUESTS = q -> QuestState.NOT_STARTED;

		@Test
		@DisplayName("returns an empty Resolved for a quest not in the table")
		void emptyForMissingQuest()
		{
			// null quest → empty resolved
			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(null, NO_SKILLS, NO_QUESTS);
			assertTrue(out.isEmpty());
			assertEquals(0, out.templates.size());
			assertEquals(0, out.stubbedQuestPoints);
		}

		@Test
		@DisplayName("skill req already met → skipped, counted (Barcrawl prereq still yields a template)")
		void skillReqAlreadyMet()
		{
			// Player has Agility 50 — HFTD's 35 req is skipped. The
			// Barcrawl miniquest prereq is still produced as a template
			// because NO_QUESTS treats everything as NOT_STARTED.
			Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
			levels.put(Skill.AGILITY, 50);
			ToIntFunction<Skill> skills = s -> levels.getOrDefault(s, 1);

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.HORROR_FROM_THE_DEEP, skills, NO_QUESTS);

			assertEquals(1, out.skippedSkills);
			assertEquals(0, out.skippedQuests);
			// Barcrawl template + optional combat recommendation.
			assertEquals(2, out.templates.size());
			Goal questT = out.templates.stream()
				.filter(g -> g.getType() == GoalType.QUEST)
				.findFirst().orElseThrow();
			assertEquals("ALFRED_GRIMHANDS_BARCRAWL", questT.getQuestName());
		}

		@Test
		@DisplayName("skill req not yet met → template built with correct XP target")
		void skillReqNotMetYieldsTemplate()
		{
			// Player has Agility 30 — below the 35 requirement. Barcrawl
			// also produces a template (not finished).
			Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
			levels.put(Skill.AGILITY, 30);
			ToIntFunction<Skill> skills = s -> levels.getOrDefault(s, 1);

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.HORROR_FROM_THE_DEEP, skills, NO_QUESTS);

			// 1 skill + 1 quest prereq + 1 optional combat = 3 templates.
			assertEquals(3, out.templates.size());
			Goal skillT = out.templates.stream()
				.filter(g -> g.getType() == GoalType.SKILL)
				.findFirst().orElseThrow();
			assertEquals("AGILITY", skillT.getSkillName());
			assertEquals(Experience.getXpForLevel(35), skillT.getTargetValue());
			assertEquals(0, out.skippedSkills);
		}

		@Test
		@DisplayName("quest prereq already finished → skipped, counted")
		void questPrereqAlreadyFinished()
		{
			// MM1 requires Tree Gnome Village + Grand Tree. Both done.
			Map<Quest, QuestState> states = new EnumMap<>(Quest.class);
			states.put(Quest.TREE_GNOME_VILLAGE, QuestState.FINISHED);
			states.put(Quest.THE_GRAND_TREE, QuestState.FINISHED);
			Function<Quest, QuestState> quests = q -> states.getOrDefault(q, QuestState.NOT_STARTED);

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.MONKEY_MADNESS_I, NO_SKILLS, quests);

			// Both quest prereqs skipped; only the optional combat recommendation remains.
			assertEquals(1, out.templates.size());
			assertTrue(out.templates.get(0).isOptional());
			assertEquals(2, out.skippedQuests);
		}

		@Test
		@DisplayName("quest prereq in progress → still seeded as template")
		void questPrereqInProgressYieldsTemplate()
		{
			Map<Quest, QuestState> states = new EnumMap<>(Quest.class);
			states.put(Quest.TREE_GNOME_VILLAGE, QuestState.IN_PROGRESS);
			states.put(Quest.THE_GRAND_TREE, QuestState.NOT_STARTED);
			Function<Quest, QuestState> quests = q -> states.getOrDefault(q, QuestState.NOT_STARTED);

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.MONKEY_MADNESS_I, NO_SKILLS, quests);

			// 2 quest prereqs + 1 optional combat recommendation.
			assertEquals(3, out.templates.size());
			assertEquals(2, out.templates.stream().filter(g -> g.getType() == GoalType.QUEST).count());
			assertEquals(0, out.skippedQuests);
		}

		@Test
		@DisplayName("combatLevel produces an ACCOUNT goal template")
		void combatLevelProducesAccountGoal()
		{
			// Dream Mentor's only hard gate is 85 Combat. Mark its quest
			// prereqs finished so we can isolate the combat-level template.
			Map<Quest, QuestState> states = new EnumMap<>(Quest.class);
			states.put(Quest.LUNAR_DIPLOMACY, QuestState.FINISHED);
			states.put(Quest.EADGARS_RUSE, QuestState.FINISHED);
			Function<Quest, QuestState> quests = q -> states.getOrDefault(q, QuestState.NOT_STARTED);

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.DREAM_MENTOR, NO_SKILLS, quests);

			assertEquals(1, out.templates.size());
			assertEquals(GoalType.ACCOUNT, out.templates.get(0).getType());
			assertEquals("COMBAT_LEVEL", out.templates.get(0).getAccountMetric());
			assertEquals(85, out.templates.get(0).getTargetValue());
			assertEquals(85, out.stubbedCombatLevel);
			assertFalse(out.isEmpty());
		}

		@Test
		@DisplayName("questPoints produces an ACCOUNT goal template (DS2 has 200 QP)")
		void questPointsProducesAccountGoal()
		{
			// Mark every quest as finished and every skill high enough
			// so only the QP template remains.
			ToIntFunction<Skill> maxed = s -> 99;
			Function<Quest, QuestState> allFinished = q -> QuestState.FINISHED;

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.DRAGON_SLAYER_II, maxed, allFinished);

			// QP template + optional combat recommendation.
			assertEquals(2, out.templates.size());
			Goal qpTemplate = out.templates.stream()
				.filter(g -> "QUEST_POINTS".equals(g.getAccountMetric()))
				.findFirst().orElseThrow();
			assertEquals(GoalType.ACCOUNT, qpTemplate.getType());
			assertEquals(200, qpTemplate.getTargetValue());
			assertFalse(qpTemplate.isOptional());
			assertEquals(200, out.stubbedQuestPoints);
			assertFalse(out.isEmpty());
		}

		@Test
		@DisplayName("null quest-state lookup result is treated as NOT_STARTED")
		void nullStateIsNotStarted()
		{
			Function<Quest, QuestState> quests = q -> null;

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.MONKEY_MADNESS_I, NO_SKILLS, quests);

			// Both prereqs treated as unfinished + optional combat = 3 templates.
			assertEquals(3, out.templates.size());
			assertEquals(0, out.skippedQuests);
		}

		@Test
		@DisplayName("exception from quest-state lookup is treated as NOT_STARTED")
		void exceptionFromLookupIsNotStarted()
		{
			Function<Quest, QuestState> quests = q ->
			{
				throw new RuntimeException("varp blew up");
			};

			QuestRequirementResolver.Resolved out =
				QuestRequirementResolver.resolve(Quest.MONKEY_MADNESS_I, NO_SKILLS, quests);

			assertEquals(3, out.templates.size());
		}
	}
}

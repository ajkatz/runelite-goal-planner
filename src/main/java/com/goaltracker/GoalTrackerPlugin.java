package com.goaltracker;

import com.goaltracker.api.GoalTrackerApi;
import com.goaltracker.api.GoalTrackerApiImpl;
import com.goaltracker.data.AchievementDiaryData;
import com.goaltracker.data.CombatAchievementData;
import com.goaltracker.data.CombatAchievementData.Tier;
import com.goaltracker.data.ItemSourceData;
import com.goaltracker.data.SourceAttributes;
import com.goaltracker.data.WikiCaRepository;
import com.google.inject.Binder;
import com.goaltracker.model.Goal;
import com.goaltracker.model.GoalType;
import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;
import com.goaltracker.persistence.GoalStore;
import com.goaltracker.tracker.ItemTracker;
import com.goaltracker.tracker.SkillTracker;
import com.goaltracker.ui.GoalPanel;
import com.goaltracker.util.FormatUtil;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Goal Tracker",
	description = "Track and manage OSRS goals with a visual priority list",
	tags = {"goals", "tracker", "progress", "skills", "quests"}
)
public class GoalTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GoalTrackerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private GoalStore goalStore;

	@Inject
	private SkillTracker skillTracker;

	@Inject
	private com.goaltracker.tracker.QuestTracker questTracker;

	@Inject
	private com.goaltracker.tracker.DiaryTracker diaryTracker;

	@Inject
	private com.goaltracker.tracker.CombatAchievementTracker combatAchievementTracker;

	@Inject
	private ItemTracker itemTracker;

	@Inject
	private com.goaltracker.tracker.AccountTracker accountTracker;

	@Inject
	private net.runelite.client.plugins.PluginManager pluginManager;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private WikiCaRepository wikiCaRepository;

	@Inject
	private ChatboxItemSearch chatboxItemSearch;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GoalTrackerApiImpl goalTrackerApi;

	@Inject
	private com.goaltracker.service.GoalReorderingService goalReorderingService;

	private GoalPanel panel;
	private NavigationButton navButton;

	/**
	 * Bind the public {@link GoalTrackerApi} interface to its implementation so
	 * other plugins can {@code @Inject GoalTrackerApi} after declaring
	 * {@code @PluginDependency(GoalTrackerPlugin.class)}.
	 */
	@Override
	public void configure(Binder binder)
	{
		binder.bind(GoalTrackerApi.class).to(GoalTrackerApiImpl.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Goal Tracker started");

		goalStore.load();
		seedCanonicalSystemTags();
		wikiCaRepository.loadAsync();
		// Migrate any pre-existing CA goals (from before the bit-packed varp tracking
		// switch) by looking up their wiki id by name. No-op if the wiki cache hasn't
		// populated yet — they'll get filled on a later startup.
		migrateCaTaskIds();

		panel = new GoalPanel(goalStore, skillIconManager, itemManager, spriteManager,
			goalTrackerApi, goalReorderingService, this::openItemSearch);
		panel.setClient(client);
		panel.setQuestHelperCallback(this::openQuestInHelper, this::isQuestHelperAvailable);

		// Wire the API's UI-refresh hook so external addGoal calls trigger a rebuild
		// on the Swing thread.
		goalTrackerApi.setOnGoalsChanged(
			() -> javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild()));

		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "/goal_icon.png");
		}
		catch (Exception e)
		{
			log.warn("Could not load goal_icon.png, using fallback");
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("Goal Tracker")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Goal Tracker stopped");
		goalStore.save();
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
	}

	/**
	 * Opens the in-game chatbox item search.
	 * After selection, shows a confirmation dialog with item name + quantity.
	 */
	public void openItemSearch(int targetQty, String preferredSectionId, int positionInSection)
	{
		chatboxItemSearch
			.tooltipText("Select item for goal")
			.onItemSelected(itemId ->
			{
				// All ItemManager calls (canonicalize, getItemComposition,
				// buildItemTags' fallback) require the client thread. Compute
				// everything we need from item data here, then hand only the
				// pure data + the dialog show to the Swing thread. Doing
				// buildItemTags inside SwingUtilities.invokeLater previously
				// triggered an AssertionError ("must be called on client thread")
				// for any item not in ItemSourceData (lyres, runes, etc.) — the
				// EDT swallowed the exception and the goal was silently dropped.
				clientThread.invokeLater(() ->
				{
					int canonicalId = itemManager.canonicalize(itemId);
					String itemName = itemManager.getItemComposition(canonicalId).getName();
					java.util.List<ItemTag> autoTags = buildItemTags(canonicalId);
					java.util.List<String> autoTagIds = new java.util.ArrayList<>();
					for (ItemTag spec : autoTags)
					{
						com.goaltracker.model.Tag tag =
							goalStore.findOrCreateSystemTag(spec.getLabel(), spec.getCategory());
						if (tag != null) autoTagIds.add(tag.getId());
					}

					javax.swing.SwingUtilities.invokeLater(() ->
					{
						// Set sectionId at build time so addGoal places the
						// goal in the right section instead of defaulting to
						// Incomplete. Position-within-section is applied as a
						// follow-up call after the goal exists.
						Goal goal = Goal.builder()
							.type(GoalType.ITEM_GRIND)
							.name(itemName)
							.description(FormatUtil.formatNumber(targetQty) + " total")
							.itemId(canonicalId)
							.targetValue(targetQty)
							.currentValue(-1)
							.sectionId(preferredSectionId)  // null → addGoal falls back to Incomplete
							.tagIds(new java.util.ArrayList<>(autoTagIds))
							.defaultTagIds(new java.util.ArrayList<>(autoTagIds))
							.build();

						// Route through the command path so this is undoable.
						// Wrap create + position in a single compound entry so
						// one undo reverses the whole gesture.
						final String capturedGoalId = goal.getId();
						final String displayName = itemName;
						goalTrackerApi.beginCompound("Add goal: " + displayName);
						try
						{
							goalTrackerApi.executeCommand(new com.goaltracker.command.Command()
							{
								@Override public boolean apply()
								{
									boolean exists = goalStore.getGoals().stream()
										.anyMatch(x -> capturedGoalId.equals(x.getId()));
									if (exists) return false;
									goalStore.addGoal(goal);
									return true;
								}
								@Override public boolean revert()
								{
									goalStore.removeGoal(capturedGoalId);
									return true;
								}
								@Override public String getDescription() { return "Add goal: " + displayName; }
							});
							if (preferredSectionId != null && positionInSection >= 0)
							{
								goalTrackerApi.positionGoalInSection(
									capturedGoalId, preferredSectionId, positionInSection);
							}
						}
						finally
						{
							goalTrackerApi.endCompound();
						}
						refreshItemGoalsNow();
					});
				});
			})
			.build();
	}

	/**
	 * Build tags for an item, including source tags and inherited attributes (e.g., Slayer Task).
	 */
	/**
	 * Route an already-built Goal through the undo/redo command
	 * system so user-triggered "Add Goal" actions from menu entries (CA,
	 * diary, inventory/bank/collection log) land on the undo stack.
	 */
	private void addGoalUndoable(Goal goal, String description)
	{
		final String goalId = goal.getId();
		final Goal captured = goal;
		goalTrackerApi.executeCommand(new com.goaltracker.command.Command()
		{
			@Override public boolean apply()
			{
				boolean exists = goalStore.getGoals().stream()
					.anyMatch(x -> goalId.equals(x.getId()));
				if (exists) return false;
				goalStore.addGoal(captured);
				return true;
			}
			@Override public boolean revert()
			{
				goalStore.removeGoal(goalId);
				return true;
			}
			@Override public String getDescription() { return description; }
		});
	}

	private java.util.List<ItemTag> buildItemTags(int itemId)
	{
		// Callers MUST pass an already-canonicalized item id and MUST invoke
		// from the client thread. The earlier in-method canonicalize fallback
		// was removed because (a) every call site now canonicalizes upstream,
		// and (b) calling itemManager.canonicalize from the Swing EDT throws
		// AssertionError ("must be called on client thread"), which the EDT
		// silently swallowed and dropped the goal-add silently.
		java.util.List<ItemTag> tags = new java.util.ArrayList<>(ItemSourceData.getTags(itemId));

		// Check for inherited attributes. Slayer inheritance now comes from
		// two independent signals unioned together:
		//   1. Boss-source inheritance: any of this item's boss sources is a
		//      registered slayer-task monster (Abyssal Sire, Alchemical
		//      Hydra, Kraken, etc.). This is how Sire drops like the
		//      Abyssal whip / dagger pick up Slayer.
		//   2. Direct item inheritance: the item id is in the
		//      DIRECT_SLAYER_ITEMS set (SourceAttributes.isSlayerItem), which
		//      covers slayer-monster drops whose source isn't currently
		//      registered as a boss (Gargoyles, Wyverns, Cockatrice, etc.)
		//      and sourceless drops (Imbued heart, Eternal gem).
		// The old dedupe bandaid ("strip non-SKILLING Slayer before appending
		// SKILLING Slayer") is gone — the OTHER "Slayer" category no longer
		// exists, so there's nothing to collide with.
		boolean needsSlayerTag = SourceAttributes.isSlayerItem(itemId);
		boolean isPet = false;
		for (ItemTag tag : tags)
		{
			if (!needsSlayerTag && SourceAttributes.isSlayerTask(tag.getLabel()))
			{
				needsSlayerTag = true;
			}
			if ("All Pets".equals(tag.getLabel()))
			{
				isPet = true;
			}
		}
		if (needsSlayerTag)
		{
			tags.add(new ItemTag("Slayer", TagCategory.SKILLING));
		}
		if (isPet)
		{
			tags.removeIf(t -> "All Pets".equals(t.getLabel()));
			// SPECIAL category removed; the Pet tag now lives in OTHER
			// with a per-tag pink color override seeded by seedCanonicalSystemTags.
			tags.add(0, new ItemTag("Pet", TagCategory.OTHER));
		}

		return tags;
	}

	/**
	 * Varbit/varp changes drive quest, diary, CA, and account tracking.
	 * Deferred to the next client tick via invokeLater because
	 * Quest.getState() runs a script internally, and VarbitChanged fires
	 * mid-script — calling getState() synchronously would cause a
	 * "scripts are not reentrant" assertion error.
	 */
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Only track when fully logged in — varbits fire during login
		// before the client is ready, which can hang Quest.getState().
		if (client.getGameState() != GameState.LOGGED_IN) return;

		clientThread.invokeLater(() ->
		{
			java.util.List<Goal> goals = goalStore.getGoals();
			boolean updated = questTracker.checkGoals(goals);
			updated |= diaryTracker.checkGoals(goals);
			updated |= combatAchievementTracker.checkGoals(goals);
			updated |= accountTracker.checkGoals(goals);
			flushIfUpdated(updated);
		});
	}

	/**
	 * Build an achievement diary Goal for a given area + tier. Stores the completion
	 * varbit on the Goal so DiaryTracker can poll it (0 if no varbit is exposed, e.g.
	 * Karamja Easy/Medium/Hard — those stay manual).
	 */
	private Goal buildDiaryGoal(String areaDisplayName, AchievementDiaryData.Tier tier)
	{
		if (areaDisplayName == null || tier == null) return null;

		int varbitId = AchievementDiaryData.completionVarbit(areaDisplayName, tier);
		if (varbitId == 0)
		{
			log.info("Diary goal '{} {}' has no tracking varbit; will be manual-only",
				areaDisplayName, tier);
		}

		return Goal.builder()
			.type(GoalType.DIARY)
			.name(areaDisplayName)
			.description(tier.getDisplayName() + " Achievement Diary")
			.targetValue(1)
			.currentValue(0)
			.spriteId(AchievementDiaryData.DIARY_SPRITE_ID)
			.varbitId(varbitId)
			.build();
	}

	/**
	 * Map from Skills-tab widget id (InterfaceID.Stats.&lt;SKILL&gt;) to the
	 * corresponding {@link net.runelite.api.Skill}. Used by the right-click handler
	 * to inject "Add Goal" on a hovered skill row.
	 */
	private static final java.util.Map<Integer, net.runelite.api.Skill> SKILL_TAB_WIDGET_TO_SKILL;
	static
	{
		java.util.Map<Integer, net.runelite.api.Skill> m = new java.util.HashMap<>();
		m.put(net.runelite.api.gameval.InterfaceID.Stats.ATTACK, net.runelite.api.Skill.ATTACK);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.STRENGTH, net.runelite.api.Skill.STRENGTH);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.DEFENCE, net.runelite.api.Skill.DEFENCE);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.RANGED, net.runelite.api.Skill.RANGED);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.PRAYER, net.runelite.api.Skill.PRAYER);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.MAGIC, net.runelite.api.Skill.MAGIC);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.RUNECRAFT, net.runelite.api.Skill.RUNECRAFT);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.CONSTRUCTION, net.runelite.api.Skill.CONSTRUCTION);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.HITPOINTS, net.runelite.api.Skill.HITPOINTS);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.AGILITY, net.runelite.api.Skill.AGILITY);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.HERBLORE, net.runelite.api.Skill.HERBLORE);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.THIEVING, net.runelite.api.Skill.THIEVING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.CRAFTING, net.runelite.api.Skill.CRAFTING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.FLETCHING, net.runelite.api.Skill.FLETCHING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.SLAYER, net.runelite.api.Skill.SLAYER);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.HUNTER, net.runelite.api.Skill.HUNTER);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.MINING, net.runelite.api.Skill.MINING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.SMITHING, net.runelite.api.Skill.SMITHING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.FISHING, net.runelite.api.Skill.FISHING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.COOKING, net.runelite.api.Skill.COOKING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.FIREMAKING, net.runelite.api.Skill.FIREMAKING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.WOODCUTTING, net.runelite.api.Skill.WOODCUTTING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.FARMING, net.runelite.api.Skill.FARMING);
		m.put(net.runelite.api.gameval.InterfaceID.Stats.SAILING, net.runelite.api.Skill.SAILING);
		SKILL_TAB_WIDGET_TO_SKILL = java.util.Collections.unmodifiableMap(m);
	}

	/**
	 * Prompt the user for a Level/XP target and add a skill goal via the API.
	 * Called from the right-click handler on the Skills tab.
	 */
	private void promptAndAddSkillGoal(net.runelite.api.Skill skill)
	{
		com.goaltracker.ui.SkillTargetForm form = new com.goaltracker.ui.SkillTargetForm(99);

		int result = javax.swing.JOptionPane.showConfirmDialog(panel, form,
			"Add " + skill.getName() + " Goal",
			javax.swing.JOptionPane.OK_CANCEL_OPTION,
			javax.swing.JOptionPane.PLAIN_MESSAGE);
		if (result != javax.swing.JOptionPane.OK_OPTION) return;

		int targetXp = form.getTargetXp();
		if (targetXp < 0)
		{
			javax.swing.JOptionPane.showMessageDialog(panel,
				"Enter a valid level (1–99) or XP (0–200,000,000).",
				"Invalid", javax.swing.JOptionPane.WARNING_MESSAGE);
			return;
		}
		goalTrackerApi.addSkillGoal(skill, targetXp);
	}

	private void promptAndAddAccountGoal(com.goaltracker.model.AccountMetric metric)
	{
		if (metric == com.goaltracker.model.AccountMetric.CA_POINTS)
		{
			// CA Points: show tier picker
			String[] options = new String[com.goaltracker.model.AccountMetric.CA_TIER_NAMES.length];
			System.arraycopy(com.goaltracker.model.AccountMetric.CA_TIER_NAMES, 0, options, 0, options.length);
			String choice = (String) javax.swing.JOptionPane.showInputDialog(panel,
				"Select CA tier target:",
				"Add CA Points Goal",
				javax.swing.JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
			if (choice == null) return;
			for (int i = 0; i < com.goaltracker.model.AccountMetric.CA_TIER_NAMES.length; i++)
			{
				if (choice.equals(com.goaltracker.model.AccountMetric.CA_TIER_NAMES[i]))
				{
					goalTrackerApi.addAccountGoal(metric.name(),
						com.goaltracker.model.AccountMetric.CA_TIER_VALUES[i]);
					return;
				}
			}
		}
		else
		{
			// Other metrics: target input with max hint
			String input = javax.swing.JOptionPane.showInputDialog(panel,
				"Target " + metric.getDisplayName() + " (max " + metric.getMaxTarget() + "):",
				"Add " + metric.getDisplayName() + " Goal",
				javax.swing.JOptionPane.PLAIN_MESSAGE);
			if (input == null || input.trim().isEmpty()) return;
			try
			{
				int target = Integer.parseInt(input.trim().replace(",", ""));
				if (target <= 0)
				{
					javax.swing.JOptionPane.showMessageDialog(panel,
						"Target must be greater than 0.",
						"Invalid", javax.swing.JOptionPane.WARNING_MESSAGE);
					return;
				}
				goalTrackerApi.addAccountGoal(metric.name(), target);
			}
			catch (NumberFormatException e)
			{
				javax.swing.JOptionPane.showMessageDialog(panel,
					"Enter a valid number.",
					"Invalid", javax.swing.JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private boolean isQuestHelperAvailable()
	{
		for (net.runelite.client.plugins.Plugin plugin : pluginManager.getPlugins())
		{
			if (plugin.getClass().getSimpleName().equals("QuestHelperPlugin")
				&& pluginManager.isPluginEnabled(plugin))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempt to open a quest in the Quest Helper plugin. Uses reflection
	 * since Quest Helper is a plugin-hub plugin with no compile-time dependency.
	 */
	private void openQuestInHelper(String questEnumName)
	{
		for (net.runelite.client.plugins.Plugin plugin : pluginManager.getPlugins())
		{
			if (!plugin.getClass().getSimpleName().equals("QuestHelperPlugin")) continue;
			if (!pluginManager.isPluginEnabled(plugin)) continue;
			try
			{
				net.runelite.api.Quest quest = net.runelite.api.Quest.valueOf(questEnumName);
				String displayName = quest.getName();

				// QuestHelperQuest.getByName(displayName) → QuestHelper
				Class<?> qhqClass = Class.forName("com.questhelper.questinfo.QuestHelperQuest",
					true, plugin.getClass().getClassLoader());
				java.lang.reflect.Method getByName = qhqClass.getMethod("getByName", String.class);
				Object questHelper = getByName.invoke(null, displayName);
				if (questHelper == null)
				{
					log.warn("Quest Helper has no helper for '{}'", displayName);
					break;
				}

				// questManager.startUpQuest(questHelper, true)
				java.lang.reflect.Method getQm = plugin.getClass().getMethod("getQuestManager");
				Object questManager = getQm.invoke(plugin);
				if (questManager == null) break;

				// Find startUpQuest method by scanning — the parameter type
				// may not match exactly via getMethod due to classloader boundaries.
				java.lang.reflect.Method startUp = null;
				for (java.lang.reflect.Method m : questManager.getClass().getMethods())
				{
					if ("startUpQuest".equals(m.getName()) && m.getParameterCount() == 2)
					{
						startUp = m;
						break;
					}
				}
				if (startUp == null) break;
				startUp.invoke(questManager, questHelper, true);
				log.info("Opened {} in Quest Helper", displayName);
				return;
			}
			catch (Exception e)
			{
				log.warn("Failed to open quest in Quest Helper: {}", e.getMessage(), e);
			}
		}
		log.info("Quest Helper not available for quest {}", questEnumName);
	}

	/** Quest list widget group ID (InterfaceID.QUESTLIST). */
	private static final int QUESTLIST_GROUP_ID = 399;

	/** Widget ID of the clickable list inside the quest list (InterfaceID.Questlist.LIST). */
	private static final int QUESTLIST_LIST_WIDGET = 26148871;

	/** Sprite ID for the blue quest tab icon (SpriteID.SideIcons.QUEST). */
	private static final int QUEST_SPRITE_ID = 899;

	/**
	 * Strip &lt;col=...&gt; tags from a menu target string and trim whitespace.
	 */
	private static String stripColorTags(String raw)
	{
		if (raw == null) return null;
		return raw.replaceAll("<col=[^>]*>", "")
			.replaceAll("</col>", "")
			.trim();
	}

	/**
	 * Reverse-lookup a {@link net.runelite.api.Quest} by display name. Case-insensitive.
	 * Returns null if no quest matches.
	 */
	private static net.runelite.api.Quest findQuestByDisplayName(String displayName)
	{
		if (displayName == null || displayName.isEmpty()) return null;
		for (net.runelite.api.Quest q : net.runelite.api.Quest.values())
		{
			if (displayName.equalsIgnoreCase(q.getName()))
			{
				return q;
			}
		}
		return null;
	}

	/**
	 * Build a quest Goal from a quest-list menu entry's target string.
	 * Returns null if the target can't be mapped to a known Quest.
	 */
	private Goal buildQuestGoal(String menuTarget)
	{
		String displayName = stripColorTags(menuTarget);
		net.runelite.api.Quest quest = findQuestByDisplayName(displayName);
		if (quest == null)
		{
			log.warn("Quest list right-click: unknown quest '{}'", displayName);
			return null;
		}

		return Goal.builder()
			.type(GoalType.QUEST)
			.name(quest.getName())
			.description("Quest")
			.questName(quest.name())
			.targetValue(1)
			.currentValue(0)
			.spriteId(QUEST_SPRITE_ID)
			.build();
	}

	/**
	 * Read a row's child widget from a per-row dynamic-children container.
	 * Returns null if the widget or the row index is out of range.
	 */
	private net.runelite.api.widgets.Widget getCaRowChild(int widgetId, int row)
	{
		net.runelite.api.widgets.Widget w = client.getWidget(widgetId);
		if (w == null) return null;
		net.runelite.api.widgets.Widget[] children = w.getDynamicChildren();
		if (children == null || row < 0 || row >= children.length) return null;
		return children[row];
	}

	/**
	 * Build a combat achievement Goal from the clicked row index in the CA tasks interface.
	 * Returns null if the row can't be resolved.
	 */
	private Goal buildCombatAchievementGoal(int row)
	{
		net.runelite.api.widgets.Widget nameW = getCaRowChild(CombatAchievementData.TASKS_NAME, row);
		net.runelite.api.widgets.Widget tierW = getCaRowChild(CombatAchievementData.TASKS_TIER, row);
		net.runelite.api.widgets.Widget monsterW = getCaRowChild(CombatAchievementData.TASKS_MONSTER, row);

		if (nameW == null || tierW == null)
		{
			log.warn("CA row {} missing name/tier widget", row);
			return null;
		}

		String name = nameW.getText();
		if (name == null || name.isEmpty())
		{
			log.warn("CA row {} has empty name", row);
			return null;
		}

		int tierSpriteId = tierW.getSpriteId();
		Tier tier = CombatAchievementData.tierFromSpriteId(tierSpriteId);
		String monster = monsterW != null ? CombatAchievementData.parseMonsterName(monsterW.getText()) : null;

		// Prefer the wiki repository for description (works for any row, expanded or not).
		// Fall back to the widget if the wiki cache hasn't populated yet — TASKS_DESCRIPTION
		// is single-instance (only for the currently expanded row), so that fallback is
		// best-effort.
		String fullDesc = null;
		WikiCaRepository.CaInfo wiki = wikiCaRepository.get(name);
		if (wiki != null && wiki.task != null && !wiki.task.isEmpty())
		{
			fullDesc = wiki.task;
		}
		else
		{
			net.runelite.api.widgets.Widget descW = client.getWidget(CombatAchievementData.TASKS_DESCRIPTION);
			fullDesc = descW != null ? CombatAchievementData.parseDescription(descW.getText()) : null;
		}
		String tooltip = fullDesc != null ? name + " \u2014 " + fullDesc : null;

		// Wiki provides the canonical CA task id; the tracker uses it to read the
		// bit-packed CA_TASK_COMPLETED varplayers. -1 = unknown.
		int caTaskId = (wiki != null) ? wiki.id : -1;

		java.util.List<String> tagIds = new java.util.ArrayList<>();
		if (monster != null)
		{
			boolean isRaid = CombatAchievementData.isRaidBoss(monster);
			String tagLabel = isRaid ? CombatAchievementData.abbreviateRaid(monster) : monster;
			com.goaltracker.model.Tag bossTag = goalStore.findOrCreateSystemTag(
				tagLabel, isRaid ? TagCategory.RAID : TagCategory.BOSS);
			if (bossTag != null) tagIds.add(bossTag.getId());
			if (SourceAttributes.isSlayerTask(monster))
			{
				com.goaltracker.model.Tag slayerTag = goalStore.findOrCreateSystemTag(
					"Slayer", TagCategory.SKILLING);
				if (slayerTag != null) tagIds.add(slayerTag.getId());
			}
		}

		String description = tier != null
			? tier.getDisplayName() + " Combat Achievement"
			: "Combat Achievement";

		return Goal.builder()
			.type(GoalType.COMBAT_ACHIEVEMENT)
			.name(name)
			.description(description)
			.tooltip(tooltip)
			.targetValue(1)
			.currentValue(0)
			.spriteId(tierSpriteId > 0 ? tierSpriteId : 0)
			.caTaskId(caTaskId)
			.tagIds(new java.util.ArrayList<>(tagIds))
			.defaultTagIds(new java.util.ArrayList<>(tagIds))
			.build();
	}

	/**
	 * Backfill caTaskId for existing CA goals by looking up their name in the wiki repo.
	 * Run on startup once the wiki cache is loaded; no-op if the cache hasn't populated yet.
	 */
	/**
	 * Pre-create the canonical system tags so the Tag Management dialog has
	 * something to display before any goals are added. Idempotent —
	 * findOrCreateSystemTag is a no-op if the tag already exists.
	 *
	 * <p>Seeded set:
	 * <ul>
	 *   <li>One SKILLING tag per Skill enum value (Attack, Strength, ..., Sailing)
	 *       — these are the read-only "skill tags" used for icon-based filtering</li>
	 *   <li>"Pet" in the OTHER category with a pink color override —
	 *       canonical pet collection tag</li>
	 * </ul>
	 *
	 * <p>Other system tags (per-monster Boss/Raid, per-item source tags) are still
	 * created lazily by {@code findOrCreateSystemTag} as goals are added — the
	 * universe is too large to pre-seed.
	 */
	private void seedCanonicalSystemTags()
	{
		// One-time category reclassifications. Run BEFORE the seed so the
		// source BOSS entity gets flipped in place (no merge needed) before
		// the seed would otherwise create a fresh MINIGAME entity alongside
		// the orphaned BOSS one. Idempotent: after the first run, each call
		// finds no source tag in the old category and no-ops.
		applyCategoryReclassifications();

		for (net.runelite.api.Skill skill : net.runelite.api.Skill.values())
		{
			com.goaltracker.model.Tag tag = goalStore.findOrCreateSystemTag(
				skill.getName(), com.goaltracker.model.TagCategory.SKILLING);
			// Skill tags render via the uniform iconKey path.
			// setTagIcon is idempotent on the same value.
			if (tag != null)
			{
				goalStore.setTagIcon(tag.getId(), skill.name());
			}
		}
		// Seed canonical BOSS / RAID / CLUE / MINIGAME tags from TagOptions so the
		// dropdowns are populated up-front instead of trickling in as goals are
		// added. findOrCreateSystemTag is idempotent.
		com.goaltracker.model.TagCategory[] seedCats = {
			com.goaltracker.model.TagCategory.BOSS,
			com.goaltracker.model.TagCategory.RAID,
			com.goaltracker.model.TagCategory.CLUE,
			com.goaltracker.model.TagCategory.MINIGAME,
		};
		for (com.goaltracker.model.TagCategory cat : seedCats)
		{
			for (String label : com.goaltracker.data.TagOptions.getOptions(cat))
			{
				goalStore.findOrCreateSystemTag(label, cat);
			}
		}
		// Pet: OTHER category with a per-tag pink color override. OTHER is the
		// only category that supports per-tag colors — every
		// other category uses a category-wide color, but OTHER tags each
		// carry their own. recolorTag is idempotent on the same value.
		com.goaltracker.model.Tag pet = goalStore.findOrCreateSystemTag(
			"Pet", com.goaltracker.model.TagCategory.OTHER);
		if (pet != null && pet.getColorRgb() < 0)
		{
			goalStore.recolorTag(pet.getId(), 0xFF69B4);
		}
		log.debug("Seeded canonical system tags");
	}

	/**
	 * Apply any one-shot category moves for system tags. Runs on every plugin
	 * start but is idempotent — {@link GoalStore#recategorizeSystemTag} finds
	 * no source tag after the first successful migration and returns false.
	 *
	 * <p>Current migrations:
	 * <ul>
	 *   <li>Tempoross: BOSS → MINIGAME (skilling boss, community treats as minigame)</li>
	 *   <li>Wintertodt: BOSS → MINIGAME (same)</li>
	 * </ul>
	 */
	private void applyCategoryReclassifications()
	{
		String[][] moves = {
			{ "Tempoross",  "BOSS", "MINIGAME" },
			{ "Wintertodt", "BOSS", "MINIGAME" },
		};
		for (String[] move : moves)
		{
			com.goaltracker.model.TagCategory from =
				com.goaltracker.model.TagCategory.valueOf(move[1]);
			com.goaltracker.model.TagCategory to =
				com.goaltracker.model.TagCategory.valueOf(move[2]);
			if (goalStore.recategorizeSystemTag(move[0], from, to))
			{
				log.info("Recategorized system tag '{}': {} → {}", move[0], from, to);
			}
		}
	}

	private void migrateCaTaskIds()
	{
		if (wikiCaRepository.size() == 0) return;
		int filled = 0;
		for (Goal g : goalStore.getGoals())
		{
			if (g.getType() != GoalType.COMBAT_ACHIEVEMENT) continue;
			if (g.getCaTaskId() >= 0) continue;
			WikiCaRepository.CaInfo info = wikiCaRepository.get(g.getName());
			if (info != null)
			{
				g.setCaTaskId(info.id);
				filled++;
			}
		}
		if (filled > 0)
		{
			log.info("Backfilled caTaskId for {} existing CA goals", filled);
			goalStore.save();
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		MenuEntry[] entries = event.getMenuEntries();
		if (entries.length == 0)
		{
			return;
		}

		// Find the first entry to determine context
		MenuEntry first = entries[entries.length - 1];

		// Look for item or combat achievement context
		for (MenuEntry entry : entries)
		{
			int widgetGroupId = entry.getParam1() >> 16;
			int itemId = entry.getItemId();

			// Skills tab: right-click a skill row -> add Skill goal. Each skill has
			// its own widget id under InterfaceID.Stats.<SKILL>; we map param1 → Skill
			// and prompt for Level / XP via a small dialog.
			net.runelite.api.Skill skillFromWidget = SKILL_TAB_WIDGET_TO_SKILL
				.get(entry.getParam1());
			if (skillFromWidget != null)
			{
				final net.runelite.api.Skill skill = skillFromWidget;
				client.createMenuEntry(1)
					.setOption("Add Goal")
					.setTarget("<col=ff9040>" + skill.getName() + "</col>")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> javax.swing.SwingUtilities.invokeLater(() ->
						promptAndAddSkillGoal(skill)));
				break;
			}

			// Achievement diary: right-click an area row -> add 4 "Add Goal: <Tier>" entries
			boolean isDiaryRow = widgetGroupId == AchievementDiaryData.GROUP_ID
				&& entry.getParam1() == AchievementDiaryData.TASKBOX;
			if (isDiaryRow && entry.getOption() != null && entry.getOption().startsWith("Open"))
			{
				String areaDisplayName = AchievementDiaryData.parseAreaFromOption(entry.getOption());
				if (areaDisplayName == null)
				{
					continue;
				}
				final String diaryMenuTarget = "<col=ff9040>" + areaDisplayName + "</col>";
				for (final AchievementDiaryData.Tier tier : AchievementDiaryData.Tier.values())
				{
					final GoalTrackerApi.DiaryTier apiTier;
					switch (tier)
					{
						case EASY:   apiTier = GoalTrackerApi.DiaryTier.EASY; break;
						case MEDIUM: apiTier = GoalTrackerApi.DiaryTier.MEDIUM; break;
						case HARD:   apiTier = GoalTrackerApi.DiaryTier.HARD; break;
						case ELITE:  apiTier = GoalTrackerApi.DiaryTier.ELITE; break;
						default:     continue;
					}

					client.createMenuEntry(1)
						.setOption("Add Goal: " + tier.getDisplayName())
						.setTarget(diaryMenuTarget)
						.setType(MenuAction.RUNELITE)
						.onClick(e ->
							goalTrackerApi.addDiaryGoal(areaDisplayName, apiTier));

					// Secondary entry: "Add Goal with Requirements" when data exists
					if (com.goaltracker.data.DiaryRequirements.hasRequirements(areaDisplayName, tier))
					{
						client.createMenuEntry(1)
							.setOption("Add Goal with Requirements: " + tier.getDisplayName())
							.setTarget(diaryMenuTarget)
							.setType(MenuAction.RUNELITE)
							.onClick(e ->
							{
								// Resolve on the client thread (needs client API),
								// then seed on the EDT so the game doesn't freeze.
								com.goaltracker.data.DiaryRequirementResolver.Resolved live =
									com.goaltracker.data.DiaryRequirementResolver.resolve(
										areaDisplayName, tier, client);
								javax.swing.SwingUtilities.invokeLater(() ->
									goalTrackerApi.addDiaryGoalWithPrereqs(
										areaDisplayName, apiTier, live));
							});
					}
				}
				break;
			}

			// Quest list: right-click a row -> add Quest goal
			boolean isQuestList = widgetGroupId == QUESTLIST_GROUP_ID
				&& entry.getParam1() == QUESTLIST_LIST_WIDGET;
			if (isQuestList)
			{
				final String menuTarget = entry.getTarget();
				// Resolve the quest from the menu target up front so we can skip
				// adding the menu entry if the name doesn't match a known Quest.
				final net.runelite.api.Quest quest = findQuestByDisplayName(stripColorTags(menuTarget));
				if (quest == null)
				{
					continue;
				}

				client.createMenuEntry(1)
					.setOption("Add Goal")
					.setTarget(menuTarget)
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						// Smoke test: route the existing right-click flow through the
						// public API to prove it's the canonical entry point. The API
						// handles validation, duplicate guard, and the UI refresh hook.
						String createdId = goalTrackerApi.addQuestGoal(quest);
						if (createdId == null)
						{
							log.warn("addQuestGoal returned null for {}", quest);
						}
					});

				// Secondary entry: only shown when we have requirement data
				// AND at least one requirement is unmet after resolving against
				// live player state. Uses createMenuEntry(1) so it renders
				// above the plain "Add Goal" entry. Routes through the
				// internal API so quest-requirement queries have a single
				// choke point (future caching / pluggable data sources).
				if (com.goaltracker.data.QuestRequirements.hasRequirements(quest))
				{
					com.goaltracker.data.QuestRequirementResolver.Resolved resolved =
						goalTrackerApi.resolveQuestRequirements(quest);
					if (!resolved.isEmpty())
					{
						client.createMenuEntry(1)
							.setOption("Add Goal with Requirements")
							.setTarget(menuTarget)
							.setType(MenuAction.RUNELITE)
							.onClick(e ->
							{
								// Resolve on the client thread (needs client API),
								// then seed on the EDT so the game doesn't freeze.
								com.goaltracker.data.QuestRequirementResolver.Resolved live =
									goalTrackerApi.resolveQuestRequirements(quest);
								javax.swing.SwingUtilities.invokeLater(() ->
								{
									if (live.skippedSkills > 0 || live.skippedQuests > 0)
									{
										log.info("addQuestGoalWithPrereqs({}): skipped {} already-met skill reqs, {} already-finished quest prereqs",
											quest.getName(), live.skippedSkills, live.skippedQuests);
									}
									String createdId = goalTrackerApi.addQuestGoalWithPrereqs(quest, live.templates);
									if (createdId == null)
									{
										log.warn("addQuestGoalWithPrereqs returned null for {}", quest);
									}
								});
							});
					}
				}
				break;
			}

			// Combat achievements (CA_TASKS widget group 715)
			boolean isCombatAchievement = widgetGroupId == CombatAchievementData.GROUP_ID
				&& entry.getParam1() == CombatAchievementData.TASKS_BACKGROUND;
			if (isCombatAchievement)
			{
				final int row = entry.getParam0();
				final Goal preview = buildCombatAchievementGoal(row);
				if (preview == null)
				{
					continue;
				}

				client.createMenuEntry(1)
					.setOption("Add Goal")
					.setTarget(entry.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						// Re-read at click time in case the list was re-filtered
						Goal goal = buildCombatAchievementGoal(row);
						if (goal == null)
						{
							log.warn("CA goal row {} no longer resolvable at click time", row);
							return;
						}
						// Duplicate guard: prefer caTaskId match (stable across rebuilds);
						// fall back to name match for legacy goals without a task id.
						final String newName = goal.getName();
						final int newTaskId = goal.getCaTaskId();
						if (goalStore.exists(g -> {
							if (g.getType() != GoalType.COMBAT_ACHIEVEMENT) return false;
							boolean sameTask = newTaskId >= 0 && g.getCaTaskId() == newTaskId;
							boolean sameName = newName != null && newName.equalsIgnoreCase(g.getName());
							return sameTask || sameName;
						}))
						{
							log.info("CA goal already exists: {}", newName);
							return;
						}
						addGoalUndoable(goal, "Add CA goal: " + newName);
						refreshItemGoalsNow();
					});
				break;
			}

			// Total level: right-click the "Total level" box in the stats tab
			// (group=320, child=25) OR any child in the stats group that
			// isn't a skill row.
			if (entry.getParam1() == net.runelite.api.gameval.InterfaceID.Stats.TOTAL)
			{
				client.createMenuEntry(1)
					.setOption("Add Goal")
					.setTarget("<col=ff9040>Total Level</col>")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> javax.swing.SwingUtilities.invokeLater(() ->
						promptAndAddAccountGoal(com.goaltracker.model.AccountMetric.TOTAL_LEVEL)));
				break;
			}

			// Character summary pane (group=712, child=3): the tab buttons
			// for quest list, CA overview, etc. Use param0 to distinguish.
			if (widgetGroupId == 712 && (entry.getParam1() & 0xFFFF) == 3)
			{
				int buttonIndex = entry.getParam0();
				// param0=3: Quest List button → QP goal
				if (buttonIndex == 3 && "Quest List".equals(entry.getOption()))
				{
					client.createMenuEntry(1)
						.setOption("Add Goal")
						.setTarget("<col=ff9040>Quest Points</col>")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> javax.swing.SwingUtilities.invokeLater(() ->
							promptAndAddAccountGoal(com.goaltracker.model.AccountMetric.QUEST_POINTS)));
					break;
				}
				// param0=5: CA Overview/Tasks/Bosses/Rewards → one entry per CA tier
				if (buttonIndex == 5 && ("Overview".equals(entry.getOption())
					|| "Tasks".equals(entry.getOption())
					|| "Bosses".equals(entry.getOption())
					|| "Rewards".equals(entry.getOption())))
				{
					// Iterate tiers so Easy is at the top, Grandmaster at the bottom
					for (int ti = 0; ti < com.goaltracker.model.AccountMetric.CA_TIER_NAMES.length; ti++)
					{
						final int tierTarget = com.goaltracker.model.AccountMetric.CA_TIER_VALUES[ti];
						final String tierName = com.goaltracker.model.AccountMetric.CA_TIER_NAMES[ti];
						client.createMenuEntry(1)
							.setOption("Add Goal: " + tierName)
							.setTarget("<col=ff9040>CA Points</col>")
							.setType(MenuAction.RUNELITE)
							.onClick(e -> goalTrackerApi.addAccountGoal(
								com.goaltracker.model.AccountMetric.CA_POINTS.name(), tierTarget));
					}
					break;
				}
			}


			// Check if this is an inventory, bank, or collection log item
			boolean isInventory = widgetGroupId == WidgetID.INVENTORY_GROUP_ID;
			boolean isBank = widgetGroupId == WidgetID.BANK_GROUP_ID;
			boolean isCollectionLog = widgetGroupId == WidgetID.COLLECTION_LOG_ID;

			if (!isInventory && !isBank && !isCollectionLog)
			{
				continue;
			}

			// Collection log: only act on real item slots in the right pane.
			// The left-pane category tabs (Bosses → Vorkath, etc.) share the
			// same widget group id but have entry.getItemId() <= 0, with the
			// identifier holding a tab index. The previous fallback to
			// entry.getIdentifier() picked up tab index 1 → canonical item id
			// 1 ("Toolkit"), producing a phantom Add Goal on tab clicks.
			if (itemId <= 0)
			{
				continue;
			}

			// Get the real item ID (noted items have different IDs)
			final int realItemId = itemManager.canonicalize(itemId);
			String itemName = itemManager.getItemComposition(realItemId).getName();
			final boolean fromCollectionLog = isCollectionLog;

			// Add at index 1 to put it near the bottom of the menu
			client.createMenuEntry(1)
				.setOption("Add Goal")
				.setTarget(entry.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					// Build tags on the client thread (ItemManager requires it),
					// then show the quantity prompt on the EDT so it doesn't
					// block the game's rendering loop.
					java.util.List<ItemTag> autoTags = buildItemTags(realItemId);
					java.util.List<String> autoTagIds = new java.util.ArrayList<>();
					for (ItemTag spec : autoTags)
					{
						com.goaltracker.model.Tag tag =
							goalStore.findOrCreateSystemTag(spec.getLabel(), spec.getCategory());
						if (tag != null) autoTagIds.add(tag.getId());
					}

					javax.swing.SwingUtilities.invokeLater(() ->
					{
						String input = javax.swing.JOptionPane.showInputDialog(
							panel,
							"Target quantity for " + itemName + ":",
							"1"
						);
						if (input == null) return;
						int qty;
						try
						{
							qty = Integer.parseInt(input.trim().replace(",", ""));
							if (qty <= 0) return;
						}
						catch (NumberFormatException ignored) { return; }

						Goal goal = Goal.builder()
							.type(GoalType.ITEM_GRIND)
							.name(itemName)
							.description(FormatUtil.formatNumber(qty) + " total")
							.itemId(realItemId)
							.targetValue(qty)
							.currentValue(-1)
							.tagIds(new java.util.ArrayList<>(autoTagIds))
							.defaultTagIds(new java.util.ArrayList<>(autoTagIds))
							.build();

						addGoalUndoable(goal, "Add goal: " + itemName);
						refreshItemGoalsNow();
					});
				});

			// Only add one "Add Goal" entry
			break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		java.util.List<Goal> goals = goalStore.getGoals();
		boolean updated = skillTracker.checkGoals(goals);
		// Account metrics (combat level, total level) are derived from stats.
		updated |= accountTracker.checkGoals(goals);
		flushIfUpdated(updated);
	}

	/**
	 * Immediately recheck item goals against the currently-cached bank/inventory
	 * containers. Runs on the client thread so ItemContainer reads are safe.
	 * This lets a newly-added ITEM_GRIND goal get its count populated right away
	 * if the bank is already open (otherwise it waits for the next
	 * ItemContainerChanged event, which only fires on open).
	 */
	private void refreshItemGoalsNow()
	{
		clientThread.invokeLater(() ->
			flushIfUpdated(itemTracker.checkGoals(goalStore.getGoals())));
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();
		if (containerId != InventoryID.BANK.getId() && containerId != InventoryID.INVENTORY.getId())
		{
			return;
		}

		flushIfUpdated(itemTracker.checkGoals(goalStore.getGoals()));
	}

	/**
	 * Common flush after tracker updates: reconcile completed goals into
	 * the Completed section, persist, and rebuild the UI on the EDT.
	 */
	private void flushIfUpdated(boolean updated)
	{
		if (updated)
		{
			goalStore.reconcileCompletedSection();
			goalStore.save();
			javax.swing.SwingUtilities.invokeLater(() -> panel.rebuild());
		}
	}

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoalTrackerConfig.class);
	}
}

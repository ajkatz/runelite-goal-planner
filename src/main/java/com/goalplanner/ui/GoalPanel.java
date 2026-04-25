package com.goalplanner.ui;

import com.goalplanner.model.Goal;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.service.GoalReorderingService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar panel — priority list of goals with gradient cards and arrow reordering.
 */
@Slf4j
public class GoalPanel extends PluginPanel
{
	/**
	 * Discord invite for the plugin's community. Exposed via the header
	 * Options menu.
	 */
	private static final String DISCORD_URL = "https://discord.gg/CFQsA3fmh7";

	private final GoalStore goalStore;
	private final GoalReorderingService reorderingService;
	private final com.goalplanner.api.GoalPlannerApiImpl api;
	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;
	private final net.runelite.client.game.SpriteManager spriteManager;
	private final ItemSearchRequest itemSearchCallback;
	private final GoalReorderController reorderController;
	private final GoalDialogFactory dialogFactory;
	private final GoalContextMenuBuilder contextMenuBuilder;

	/**
	 * Callback the panel uses to ask the plugin to open the in-game chatbox
	 * item search and create an item goal. The plugin owns the chatbox + the
	 * client thread; the panel owns the section/position context. Passing
	 * both pieces of state through the callback lets the plugin place the
	 * created goal in the right slot — without this, item goals always
	 * landed in the default Incomplete section regardless of which section
	 * the user right-clicked from.
	 */
	@FunctionalInterface
	public interface ItemSearchRequest
	{
		/**
		 * @param qty               target quantity for the new item goal
		 * @param preferredSectionId section the goal should land in, or null
		 *                           for the default Incomplete section
		 * @param positionInSection in-section index to place the goal at,
		 *                           or -1 for "append to bottom"
		 */
		void accept(int qty, String preferredSectionId, int positionInSection);
	}
	private Client client;
	private final JPanel goalListPanel;
	private final Map<String, GoalCard> cardMap = new HashMap<>();
	/** Free-text filter applied to the goal list. Empty = show all. */
	private String searchFilter = "";
	/** Most recent simple-click goal id, used as the anchor for shift-click range
	 *  selection. Cleared on rebuilds when the goal no longer exists. */
	private String selectionAnchorId = null;
	/** Id of the goal the user initiated a relation-pick from,
	 *  or null when not in relation-pick mode. Set by the Requires.../Required
	 *  by... context-menu items; cleared on successful target click, cancel,
	 *  or ESC. */
	String pendingRelationSourceId = null;
	/** Direction flag for relation-pick mode. When true, the next
	 *  clicked card becomes a REQUIREMENT of {@link #pendingRelationSourceId}
	 *  (edge source → target). When false, the source becomes a DEPENDENT of
	 *  the target (edge target → source). */
	private boolean pendingRelationSourceRequiresTarget = true;
	/** Instruction banner shown at the top of the panel while
	 *  relation-pick mode is active. Hidden otherwise. */
	private JPanel relationModeBanner;
	private JLabel relationModeLabel;
	/** Toolbar undo/redo buttons. Refreshed on every rebuild. */
	private JButton undoButton;
	private JButton redoButton;

	/** Callback to open a quest in Quest Helper. */
	java.util.function.Consumer<String> questHelperCallback;
	/** Checks whether Quest Helper is currently available. */
	java.util.function.Supplier<Boolean> questHelperAvailable;

	public void setQuestHelperCallback(java.util.function.Consumer<String> callback,
									   java.util.function.Supplier<Boolean> available)
	{
		this.questHelperCallback = callback;
		this.questHelperAvailable = available;
	}

	/** Callback to open an achievement diary in Quest Helper. Args: (areaName, tierDisplayName). */
	java.util.function.BiConsumer<String, String> diaryHelperCallback;

	public void setDiaryHelperCallback(java.util.function.BiConsumer<String, String> callback)
	{
		this.diaryHelperCallback = callback;
	}

	public GoalPanel(GoalStore goalStore, SkillIconManager skillIconManager, ItemManager itemManager,
					 net.runelite.client.game.SpriteManager spriteManager,
					 com.goalplanner.api.GoalPlannerApiImpl api,
					 GoalReorderingService reorderingService,
					 ItemSearchRequest itemSearchCallback)
	{
		super(false);
		this.goalStore = goalStore;
		this.reorderingService = reorderingService;
		this.api = api;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.itemSearchCallback = itemSearchCallback;
		this.reorderController = new GoalReorderController(api, goalStore);
		this.dialogFactory = new GoalDialogFactory(api, goalStore, skillIconManager,
			itemManager, spriteManager, itemSearchCallback, this);
		this.contextMenuBuilder = new GoalContextMenuBuilder(api, goalStore, this,
			dialogFactory, reorderController);

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header with add button
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Goal Planner");
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

		JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		headerButtons.setOpaque(false);

		// + goal and + section buttons removed. Adding is now
		// contextual via section header / goal card right-click menus.

		// Options menu — opens a small popup with plugin-wide actions
		// (Discord link, and future general options). Single-goal removal
		// is still available via right-click context menu on each card;
		// bulk / "remove all" entry points were dropped in v0.1.0 in
		// favor of relying on right-click + undo/redo for reversibility.
		JButton optionsButton = new JButton(ShapeIcons.moreDots(10, new Color(180, 180, 220)));
		optionsButton.setToolTipText("Options\u2026");
		optionsButton.setMargin(new Insets(3, 6, 3, 6));
		optionsButton.addActionListener(e -> {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem joinDiscord = new JMenuItem("Join our Discord");
			joinDiscord.addActionListener(ev -> openDiscordInvite());
			popup.add(joinDiscord);
			popup.show(optionsButton, 0, optionsButton.getHeight());
		});

		JButton manageTagsButton = new JButton(ShapeIcons.tag(12, new Color(220, 180, 140)));
		manageTagsButton.setToolTipText("Manage tags");
		manageTagsButton.setMargin(new Insets(3, 6, 3, 6));
		manageTagsButton.addActionListener(e -> {
			java.awt.Window window = SwingUtilities.getWindowAncestor(GoalPanel.this);
			java.awt.Frame owner = window instanceof java.awt.Frame ? (java.awt.Frame) window : null;
			TagManagementDialog dialog = new TagManagementDialog(owner, api, skillIconManager, itemManager);
			dialog.setVisible(true);
		});

		// Undo/redo buttons. Tooltip + enable state refreshed on
		// each panel rebuild via refreshUndoRedoButtons() (called from rebuild()).
		undoButton = new JButton(ShapeIcons.undoArrow(12, new Color(180, 180, 220)));
		undoButton.setMargin(new Insets(3, 6, 3, 6));
		undoButton.addActionListener(e -> api.undo());
		redoButton = new JButton(ShapeIcons.redoArrow(12, new Color(180, 180, 220)));
		redoButton.setMargin(new Insets(3, 6, 3, 6));
		redoButton.addActionListener(e -> api.redo());

		headerButtons.add(optionsButton);
		headerButtons.add(Box.createHorizontalStrut(6));
		headerButtons.add(undoButton);
		headerButtons.add(redoButton);
		headerButtons.add(Box.createHorizontalStrut(6));
		headerButtons.add(manageTagsButton);

		header.add(title, BorderLayout.WEST);
		header.add(headerButtons, BorderLayout.EAST);

		// Free-text search row beneath the toolbar. Filters
		// goals by name/description/tags/category/type/section title.
		JPanel searchRow = new JPanel(new BorderLayout(4, 0));
		searchRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		searchRow.setBorder(new EmptyBorder(0, 8, 8, 8));
		final javax.swing.JTextField searchField = new javax.swing.JTextField();
		searchField.setToolTipText("Search goals by name, description, tag, category, type, or section");
		searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
		{
			private void update()
			{
				searchFilter = searchField.getText();
				rebuild();
			}
			@Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
			@Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
			@Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
		});
		JButton clearSearchBtn = new JButton(ShapeIcons.closeX(10, new Color(200, 200, 200)));
		clearSearchBtn.setToolTipText("Clear search");
		clearSearchBtn.setMargin(new Insets(3, 6, 3, 6));
		clearSearchBtn.addActionListener(e -> searchField.setText(""));
		searchRow.add(searchField, BorderLayout.CENTER);
		searchRow.add(clearSearchBtn, BorderLayout.EAST);

		// Relation-pick mode banner. Hidden by default, shown
		// when the user initiates "Requires..." / "Required by..." from the
		// context menu. Tells the user what to do next and how to cancel.
		relationModeBanner = new JPanel(new BorderLayout());
		relationModeBanner.setBackground(new Color(0xB8, 0x60, 0x20));
		relationModeBanner.setBorder(new EmptyBorder(4, 8, 4, 8));
		relationModeLabel = new JLabel();
		relationModeLabel.setForeground(Color.WHITE);
		relationModeLabel.setFont(relationModeLabel.getFont().deriveFont(11f));
		relationModeBanner.add(relationModeLabel, BorderLayout.CENTER);
		JButton relationCancelBtn = new JButton("\u2715");
		relationCancelBtn.setMargin(new Insets(0, 4, 0, 4));
		relationCancelBtn.setToolTipText("Cancel (ESC)");
		relationCancelBtn.addActionListener(e -> exitRelationMode());
		relationModeBanner.add(relationCancelBtn, BorderLayout.EAST);
		relationModeBanner.setVisible(false);

		JPanel headerStack = new JPanel(new BorderLayout());
		headerStack.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel headerTop = new JPanel(new BorderLayout());
		headerTop.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerTop.add(header, BorderLayout.NORTH);
		headerTop.add(searchRow, BorderLayout.CENTER);
		headerStack.add(headerTop, BorderLayout.NORTH);
		headerStack.add(relationModeBanner, BorderLayout.SOUTH);

		// Scrollable goal list
		goalListPanel = new JPanel();
		goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
		goalListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		goalListPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

		JScrollPane scrollPane = new JScrollPane(goalListPanel);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		add(headerStack, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		// ESC cancels relation-pick mode. Registered on the
		// whole panel so the key fires regardless of focus within the
		// scrollable goal list.
		javax.swing.KeyStroke escStroke =
			javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(escStroke, "cancelRelationMode");
		getActionMap().put("cancelRelationMode", new javax.swing.AbstractAction()
		{
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e)
			{
				if (pendingRelationSourceId != null) exitRelationMode();
			}
		});

		rebuild();
	}

	public void setClient(Client client)
	{
		this.client = client;
		dialogFactory.setClient(client);
	}

	/**
	 * Lightweight selection refresh — updates card borders without
	 * rebuilding the entire panel. O(cards) repaint vs O(goals * sections)
	 * full rebuild.
	 */
	/**
	 * Incrementally update progress on specific cards without a full rebuild.
	 * O(dirtyIds) — looks up each card in the map and refreshes its view.
	 * Falls back to full rebuild if a card isn't found (goal was added/removed).
	 */
	public void refreshProgress(java.util.Set<String> dirtyGoalIds)
	{
		if (dirtyGoalIds == null || dirtyGoalIds.isEmpty()) return;
		for (String goalId : dirtyGoalIds)
		{
			GoalCard card = cardMap.get(goalId);
			if (card == null)
			{
				// Card not in map — goal was added/removed, need full rebuild
				rebuild();
				return;
			}
			com.goalplanner.api.GoalView view = api.queryGoalView(goalId);
			if (view == null)
			{
				// Goal was removed — need full rebuild
				rebuild();
				return;
			}
			card.update(view);
		}
	}

	public void refreshSelection()
	{
		java.util.Set<String> selected = api.getSelectedGoalIds();
		for (java.util.Map.Entry<String, GoalCard> entry : cardMap.entrySet())
		{
			entry.getValue().setSelected(selected.contains(entry.getKey()));
		}
		refreshUndoRedoButtons();
	}

	public void rebuild()
	{
		long start = System.currentTimeMillis();
		goalListPanel.removeAll();
		cardMap.clear();
		refreshUndoRedoButtons();

		// Read path goes through the public API — the panel is now a consumer of
		// GoalPlannerApi just like external plugins would be.
		//
		// Search filter. When active, goalViews is the filtered flat
		// list. Within each section we re-order via topological
		// sort of the relation DAG (leaves first, priority tiebreaks within a
		// tier). The flat goalViews is kept for search-filter matching and for
		// the flat-priority `index` values the arrow buttons use.
		//
		// Arrow-button limitation: in sections that contain relation edges,
		// the flat-priority index the arrows act on may not correspond to the
		// visually-adjacent card in the topo-sorted view, so clicking up/down
		// on a related goal can be a visual no-op (topo sort re-applies after
		// the priority change). This is a known limitation of the session 2
		// checkpoint; topo-aware reordering is a follow-up.
		boolean filterActive = searchFilter != null && !searchFilter.trim().isEmpty();
		java.util.List<com.goalplanner.api.GoalView> goalViews = filterActive
			? api.searchGoals(searchFilter) : api.queryAllGoals();
		java.util.List<com.goalplanner.api.SectionView> sectionViews = api.queryAllSections();

		// Flat-priority index lookup for arrow-button bounds.
		java.util.Map<String, Integer> flatIndexById = new java.util.HashMap<>();
		for (int i = 0; i < goalViews.size(); i++)
		{
			flatIndexById.put(goalViews.get(i).id, i);
		}
		java.util.Set<String> visibleIds = flatIndexById.keySet();

		// Batch topo-sort all sections in one pass.
		java.util.Map<String, java.util.List<com.goalplanner.api.GoalView>> allTopoOrders =
			api.queryAllGoalsTopologicallySorted();

		for (com.goalplanner.api.SectionView section : sectionViews)
		{
			int sectionStart = -1;
			int sectionEnd = -1;
			for (int i = 0; i < goalViews.size(); i++)
			{
				if (section.id.equals(goalViews.get(i).sectionId))
				{
					if (sectionStart == -1) sectionStart = i;
					sectionEnd = i;
				}
			}
			int sectionCount = (sectionStart == -1) ? 0 : (sectionEnd - sectionStart + 1);

			// Use pre-computed topo order for this section.
			java.util.List<com.goalplanner.api.GoalView> topoOrder =
				allTopoOrders.getOrDefault(section.id, java.util.Collections.emptyList());
			if (filterActive)
			{
				java.util.List<com.goalplanner.api.GoalView> filtered = new java.util.ArrayList<>();
				for (com.goalplanner.api.GoalView v : topoOrder)
				{
					if (visibleIds.contains(v.id)) filtered.add(v);
				}
				topoOrder = filtered;
			}

			// Built-in section headers (Incomplete, Completed) are
			// always visible now so the user can right-click them for the Add
			// Section action even when they're empty. User sections were
			// always visible. When filtering, still hide empty sections so
			// the result list stays focused.
			if (sectionCount == 0 && filterActive) continue;
			final String sectionIdRef = section.id;
			SectionHeaderRow headerRow = new SectionHeaderRow(section, sectionCount, () -> {
				api.toggleSectionCollapsed(sectionIdRef);
				// API callback rebuilds the panel.
			});
			// All sections get a right-click menu. User sections get the full
			// rename/move/delete/color menu; built-ins get only Change Color.
			contextMenuBuilder.attachSectionContextMenu(headerRow, section, sectionViews);
			goalListPanel.add(headerRow);
			goalListPanel.add(Box.createVerticalStrut(2));

			// Empty user-section placeholder: a single italic hint row directly under
			// the header, so a freshly created section doesn't look broken.
			if (sectionCount == 0 && !section.builtIn && !section.collapsed)
			{
				JLabel placeholder = new JLabel("Empty — right-click goals to move them here");
				placeholder.setForeground(new Color(120, 120, 120));
				placeholder.setFont(placeholder.getFont().deriveFont(Font.ITALIC, 10f));
				placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);
				placeholder.setBorder(new EmptyBorder(2, 4, 6, 4));
				goalListPanel.add(placeholder);
				continue;
			}

			// Skip rendering goal cards while the section is collapsed, or when
			// the section is empty (sectionStart == -1 → guard against the
			// goalViews.get(i) loop below running with i = -1).
			if (section.collapsed || sectionCount == 0)
			{
				continue;
			}

			boolean isCompletedSection = "COMPLETED".equals(section.kind);

			// Iterate topo-order for rendering, but resolve each
			// goal's flat-priority index for the arrow buttons. Arrows target
			// the VISUALLY adjacent card in the topo view, but only when that
			// card is in the SAME topo tier — otherwise the move would fight
			// with the DAG constraint (topo sort would put the cards back).
			// When the adjacent card is in a different tier, the arrow is
			// hidden (via firstInList/lastInList).
			for (int topoPos = 0; topoPos < topoOrder.size(); topoPos++)
			{
				com.goalplanner.api.GoalView view = topoOrder.get(topoPos);
				Goal goal = goalStore.findGoalById(view.id);
				if (goal == null) continue; // shouldn't happen but defensive
				Integer flatIdx = flatIndexById.get(view.id);
				if (flatIdx == null) continue; // search-filtered out
				final int index = flatIdx;

				final int secStart = sectionStart;
				final int secEnd = sectionEnd;

				// Arrows always fire. The handler walks the
				// topo list from the clicked card, collects any direct
				// prereq/dependent chain that needs to move with it, and
				// shifts the whole block by one position. No-ops if the
				// chain is already at the edge of the section.
				final String goalIdRef = view.id;
				final String arrowSectionId = section.id;
				GoalCard card = new GoalCard(
					view,
					e -> reorderController.moveChainInTopo(goalIdRef, arrowSectionId, /*up=*/true),
					e -> reorderController.moveChainInTopo(goalIdRef, arrowSectionId, /*up=*/false),
					skillIconManager,
					itemManager,
					spriteManager
				);

				// Completed section is read-only ordering — no reorder arrows.
				// Otherwise arrows are visible at all non-edge positions; the
				// handler itself decides whether a move is actually possible
				// (e.g. a chain that already hits the top of the section is
				// a no-op).
				if (isCompletedSection)
				{
					card.setFirstInList(true);
					card.setLastInList(true);
				}
				else
				{
					card.setFirstInList(topoPos == 0);
					card.setLastInList(topoPos == topoOrder.size() - 1);
				}

				contextMenuBuilder.addContextMenu(card, goal, index, sectionStart, sectionEnd);
				attachSelectionClick(card, view);
				cardMap.put(goal.getId(), card);

				// Highlight the relation-pick source card with
				// an orange border so the user can see which goal they're
				// pairing from while they search for a target.
				if (goal.getId().equals(pendingRelationSourceId))
				{
					card.setBorder(javax.swing.BorderFactory.createLineBorder(
						new Color(0xFF, 0x99, 0x33), 2));
				}

				goalListPanel.add(card);
				goalListPanel.add(Box.createVerticalStrut(4));
			}
		}

		if (goalViews.isEmpty())
		{
			JPanel emptyPanel = new JPanel();
			emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
			emptyPanel.setOpaque(false);
			emptyPanel.setBorder(new EmptyBorder(32, 8, 8, 8));
			emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel headline = new JLabel("No goals yet");
			headline.setForeground(new Color(180, 180, 180));
			headline.setFont(headline.getFont().deriveFont(Font.BOLD, 13f));
			headline.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel hintGoal = new JLabel("Click + to add a goal");
			hintGoal.setForeground(new Color(130, 130, 130));
			hintGoal.setFont(hintGoal.getFont().deriveFont(11f));
			hintGoal.setAlignmentX(Component.CENTER_ALIGNMENT);
			hintGoal.setBorder(new EmptyBorder(8, 0, 0, 0));

			JLabel hintSection = new JLabel("Or + to add a custom section");
			hintSection.setForeground(new Color(130, 130, 130));
			hintSection.setFont(hintSection.getFont().deriveFont(11f));
			hintSection.setAlignmentX(Component.CENTER_ALIGNMENT);
			hintSection.setBorder(new EmptyBorder(2, 0, 0, 0));

			emptyPanel.add(headline);
			emptyPanel.add(hintGoal);
			emptyPanel.add(hintSection);
			goalListPanel.add(emptyPanel);
		}

		goalListPanel.revalidate();
		goalListPanel.repaint();
		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > 50)
		{
			log.warn("rebuild() took {}ms ({} cards)", elapsed, cardMap.size());
		}
	}

	// ------------------------------------------------------------------
	// Relation-pick mode
	// ------------------------------------------------------------------

	/**
	 * Enter relation-pick mode. The next left-click on any goal card will
	 * complete a new relation edge between {@code sourceGoalId} and the
	 * clicked goal. Shows an instruction banner at the top of the panel.
	 *
	 * @param sourceGoalId            the goal the user right-clicked
	 * @param sourceRequiresTarget    true to make the clicked goal a
	 *                                requirement of the source (edge
	 *                                source → target); false to make the
	 *                                source a dependent of the target
	 *                                (edge target → source)
	 */
	void enterRelationMode(String sourceGoalId, boolean sourceRequiresTarget)
	{
		pendingRelationSourceId = sourceGoalId;
		pendingRelationSourceRequiresTarget = sourceRequiresTarget;
		String sourceName = reorderController.goalNameById(sourceGoalId);
		String verb = sourceRequiresTarget
			? "Click a goal to add as a requirement of \"" + sourceName + "\""
			: "Click a goal that should require \"" + sourceName + "\"";
		relationModeLabel.setText("<html>" + verb + " &mdash; ESC to cancel</html>");
		relationModeBanner.setVisible(true);
		// Rebuild so the orange source-card border gets applied.
		rebuild();
	}

	/** Exit relation-pick mode without adding an edge. */
	void exitRelationMode()
	{
		if (pendingRelationSourceId == null) return;
		pendingRelationSourceId = null;
		relationModeBanner.setVisible(false);
		// Rebuild so the orange source-card border goes away.
		rebuild();
	}

	/**
	 * Handle a left-click on a card while relation-pick mode is active.
	 * Clicking the source card cancels; any other click attempts to add
	 * the edge in the pending direction. Cycle / duplicate rejections
	 * surface as a warning dialog. Always exits the mode (successful or
	 * not) so the user isn't stranded in a "nothing I click works" state.
	 */
	private void handleRelationPickTarget(String clickedGoalId)
	{
		if (pendingRelationSourceId == null) return;
		if (clickedGoalId.equals(pendingRelationSourceId))
		{
			exitRelationMode();
			return;
		}
		boolean ok;
		String fromId;
		String toId;
		if (pendingRelationSourceRequiresTarget)
		{
			fromId = pendingRelationSourceId;
			toId = clickedGoalId;
		}
		else
		{
			fromId = clickedGoalId;
			toId = pendingRelationSourceId;
		}
		ok = api.addRequirement(fromId, toId);
		exitRelationMode();
		if (!ok)
		{
			JOptionPane.showMessageDialog(this,
				"Could not add relation \u2014 it may already exist or would create a cycle.",
				"Add Relation", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Rule 1 enforcement: any action on an unselected card auto-deselects the
	 * existing multi-selection first. Used by arrow-button moves; the right-
	 * click menu show path applies the same rule inline.
	 */
	void clearSelectionIfNotMember(String goalId)
	{
		java.util.Set<String> sel = api.getSelectedGoalIds();
		if (!sel.isEmpty() && !sel.contains(goalId))
		{
			api.clearGoalSelection();
		}
	}


	/**
	 * Attach a left-click MouseListener that routes selection clicks through
	 * the API. Coexists with the existing right-click context menu — only
	 * BUTTON1 events are handled here, BUTTON3 falls through to the popup.
	 *
	 * <p>Click semantics:
	 * <ul>
	 *   <li>Plain click on UNSELECTED → replace selection with just this card</li>
	 *   <li>Plain click on SELECTED → clear selection entirely</li>
	 *   <li>Cmd/Ctrl+click on UNSELECTED → add this card to selection</li>
	 *   <li>Cmd/Ctrl+click on SELECTED → remove this card from selection</li>
	 * </ul>
	 */
	private void attachSelectionClick(GoalCard card, com.goalplanner.api.GoalView view)
	{
		final String goalId = view.id;
		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1) return;
				if (pendingRelationSourceId != null)
				{
					handleRelationPickTarget(goalId);
					return;
				}
				// Check LIVE selection state, not the stale build-time value.
				boolean isSelected = api.getSelectedGoalIds().contains(goalId);
				boolean cmdCtrl = e.isMetaDown() || e.isControlDown();
				boolean shift = e.isShiftDown();
				if (shift && selectionAnchorId != null && !cmdCtrl)
				{
					java.util.Set<String> range = computeRangeSelection(selectionAnchorId, goalId);
					if (!range.isEmpty())
					{
						java.util.Set<String> current = new java.util.LinkedHashSet<>(api.getSelectedGoalIds());
						if (isSelected)
						{
							// Shift-click on selected goal: deselect the range.
							current.removeAll(range);
						}
						else
						{
							// Shift-click on unselected goal: add the range.
							current.addAll(range);
						}
						api.replaceGoalSelection(current);
					}
					selectionAnchorId = goalId;
					return;
				}
				if (cmdCtrl)
				{
					if (isSelected) api.removeFromGoalSelection(goalId);
					else api.addToGoalSelection(goalId);
					selectionAnchorId = goalId;
				}
				else
				{
					if (isSelected) api.clearGoalSelection();
					else api.replaceGoalSelection(java.util.Collections.singleton(goalId));
					selectionAnchorId = goalId;
				}
			}
		});
	}

	/**
	 * Refresh the enabled state + tooltip on the undo/redo buttons
	 * to reflect the current command history. Called from {@link #rebuild()}.
	 */
	private static final Color UNDO_REDO_ENABLED = new Color(180, 180, 220);
	private static final Color UNDO_REDO_DISABLED = new Color(80, 80, 90);

	/**
	 * Open the Discord invite in the user's default browser. Falls back to
	 * a no-op (with a log warning) if Desktop browse isn't supported — on
	 * a headless system there's nothing useful we can do.
	 */
	private void openDiscordInvite()
	{
		try
		{
			if (java.awt.Desktop.isDesktopSupported()
				&& java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE))
			{
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(DISCORD_URL));
			}
			else
			{
				log.warn("Desktop browse not supported; cannot open Discord invite");
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to open Discord invite: {}", ex.getMessage());
		}
	}

	private void refreshUndoRedoButtons()
	{
		if (undoButton == null || redoButton == null) return;
		boolean canUndo = api.canUndo();
		boolean canRedo = api.canRedo();
		undoButton.setEnabled(canUndo);
		redoButton.setEnabled(canRedo);
		// ShapeIcons don't react to component enabled state, so
		// swap the icon color to make the disabled state visible.
		undoButton.setIcon(ShapeIcons.undoArrow(12,
			canUndo ? UNDO_REDO_ENABLED : UNDO_REDO_DISABLED));
		redoButton.setIcon(ShapeIcons.redoArrow(12,
			canRedo ? UNDO_REDO_ENABLED : UNDO_REDO_DISABLED));
		undoButton.setToolTipText(canUndo
			? "Undo: " + api.peekUndoDescription() : "Nothing to undo");
		redoButton.setToolTipText(canRedo
			? "Redo: " + api.peekRedoDescription() : "Nothing to redo");
	}

	/**
	 * Two-step yes/no confirmation guard for destructive actions.
	 * The action only runs if the user clicks Yes on BOTH dialogs in sequence.
	 */
	/**
	 * Walk the canonical goal order from the API and return the slice of ids
	 * between (and including) anchorId and clickedId. The order is the same
	 * one used to render the panel — sections in section.order, goals within
	 * each section in priority order. Returns an empty set if either id is
	 * missing from the canonical list (e.g. just deleted).
	 */
	/**
	 * Compute the range of goals between anchor and clicked in the
	 * RENDERED card order (topo-sorted per section), not flat priority.
	 * Uses the cardMap insertion order which matches the visual layout.
	 */
	private java.util.Set<String> computeRangeSelection(String anchorId, String clickedId)
	{
		// Walk the rendered card order (cardMap is LinkedHashMap-like via
		// insertion order during rebuild). Use goalListPanel's components
		// to get the actual visual order.
		java.util.List<String> renderedOrder = new java.util.ArrayList<>();
		for (java.awt.Component comp : goalListPanel.getComponents())
		{
			if (comp instanceof GoalCard)
			{
				GoalCard card = (GoalCard) comp;
				renderedOrder.add(card.getGoalId());
			}
		}
		int aIdx = -1, bIdx = -1;
		for (int i = 0; i < renderedOrder.size(); i++)
		{
			String id = renderedOrder.get(i);
			if (id.equals(anchorId)) aIdx = i;
			if (id.equals(clickedId)) bIdx = i;
		}
		if (aIdx < 0 || bIdx < 0) return java.util.Collections.emptySet();
		int lo = Math.min(aIdx, bIdx), hi = Math.max(aIdx, bIdx);
		java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
		for (int i = lo; i <= hi; i++) out.add(renderedOrder.get(i));
		return out;
	}


}

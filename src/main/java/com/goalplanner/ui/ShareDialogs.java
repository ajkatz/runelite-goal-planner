package com.goalplanner.ui;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.goalplanner.share.ShareFormatException;
import com.goalplanner.share.ShareText;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JOptionPane;

/**
 * Swing dialogs for sharing/importing goals (export to clipboard, paste-import,
 * share-to-party). EDT-only and interactive, so there are no unit tests — these
 * are verified in-client. The non-interactive engine they drive (export, codec,
 * import) is covered by tests in the api/share packages.
 */
public final class ShareDialogs
{
	private ShareDialogs()
	{
	}

	/** Paste a share code → import into a new section. {@code onDone} (e.g. a
	 *  panel rebuild) runs on a successful import. */
	public static void promptImport(Component parent, GoalPlannerApiImpl api, ShareCodec codec, Runnable onDone)
	{
		String text = JOptionPane.showInputDialog(parent,
			"Paste a Goal Planner share code:", "Import shared goals", JOptionPane.PLAIN_MESSAGE);
		if (text == null || text.trim().isEmpty())
		{
			return;
		}
		ShareBundle bundle;
		try
		{
			bundle = codec.decode(text);
		}
		catch (ShareFormatException e)
		{
			JOptionPane.showMessageDialog(parent,
				"That doesn't look like a valid Goal Planner share code.",
				"Import failed", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String sectionId = api.importShareBundle(bundle);
		if (sectionId == null)
		{
			JOptionPane.showMessageDialog(parent, "Nothing to import.", "Import",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (onDone != null)
		{
			onDone.run();
		}
		int n = bundle.getGoals() != null ? bundle.getGoals().size() : 0;
		JOptionPane.showMessageDialog(parent, "Imported " + n + " goal(s).", "Import",
			JOptionPane.INFORMATION_MESSAGE);
	}

	/** Copy a paste-ready share line for a section to the clipboard. */
	public static void copySection(Component parent, GoalPlannerApiImpl api,
		ShareCodec codec, Supplier<String> playerName, String sectionId)
	{
		ShareBundle bundle = api.exportSectionBundle(sectionId, safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "That section has no goals to share.",
				"Share", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line = ShareText.invite(bundle, codec.encode(bundle));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(line), null);
		JOptionPane.showMessageDialog(parent,
			"Copied a share code for \"" + bundle.getSectionName() + "\" to your clipboard.\n"
				+ "Paste it in Discord or chat — anyone with the plugin can import it.",
			"Share", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Broadcast a section to the current RuneLite party. */
	public static void shareSectionToParty(Component parent, GoalPlannerApiImpl api,
		Supplier<String> playerName, BooleanSupplier inParty, Consumer<ShareBundle> shareToParty,
		String sectionId)
	{
		if (!inParty.getAsBoolean())
		{
			JOptionPane.showMessageDialog(parent,
				"You're not in a RuneLite party.\n"
					+ "Open the Party panel and create or join one, then try again.",
				"Share to party", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ShareBundle bundle = api.exportSectionBundle(sectionId, safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "That section has no goals to share.",
				"Share to party", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		shareToParty.accept(bundle);
		JOptionPane.showMessageDialog(parent,
			"Shared \"" + bundle.getSectionName() + "\" to your party.",
			"Share to party", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Copy a paste-ready share line for the given goals to the clipboard. */
	public static void copyGoals(Component parent, GoalPlannerApiImpl api, ShareCodec codec,
		Supplier<String> playerName, List<String> goalIds)
	{
		ShareBundle bundle = api.exportGoalsBundle(goalIds, safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "Nothing to share.", "Share",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line = ShareText.invite(bundle, codec.encode(bundle));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(line), null);
		int n = bundle.getGoals().size();
		JOptionPane.showMessageDialog(parent,
			"Copied a share code for " + n + " goal" + (n == 1 ? "" : "s") + " to your clipboard.\n"
				+ "Paste it in Discord or chat — anyone with the plugin can import it.",
			"Share", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Broadcast the given goals to the current RuneLite party. */
	public static void shareGoalsToParty(Component parent, GoalPlannerApiImpl api,
		Supplier<String> playerName, BooleanSupplier inParty, Consumer<ShareBundle> shareToParty,
		List<String> goalIds)
	{
		if (!inParty.getAsBoolean())
		{
			JOptionPane.showMessageDialog(parent,
				"You're not in a RuneLite party.\n"
					+ "Open the Party panel and create or join one, then try again.",
				"Share to party", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ShareBundle bundle = api.exportGoalsBundle(goalIds, safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "Nothing to share.", "Share to party",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		shareToParty.accept(bundle);
		int n = bundle.getGoals().size();
		JOptionPane.showMessageDialog(parent,
			"Shared " + n + " goal" + (n == 1 ? "" : "s") + " to your party.",
			"Share to party", JOptionPane.INFORMATION_MESSAGE);
	}

	private static String safeName(Supplier<String> playerName)
	{
		try
		{
			String n = playerName != null ? playerName.get() : null;
			return n != null && !n.isEmpty() ? n : "Someone";
		}
		catch (RuntimeException e)
		{
			return "Someone";
		}
	}
}

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
import java.util.function.Supplier;
import javax.swing.JOptionPane;

/**
 * Swing dialogs for sharing/importing goals (export to clipboard, paste-import,
 * copy-code flows). EDT-only and interactive, so there are no unit tests — these
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
		int n = 0;
		int sections = 0;
		for (com.goalplanner.share.SectionShareDto sec : bundle.effectiveSections())
		{
			if (sec.getGoals() != null && !sec.getGoals().isEmpty())
			{
				sections++;
				n += sec.getGoals().size();
			}
		}
		String where = sections > 1 ? " across " + sections + " sections" : "";
		JOptionPane.showMessageDialog(parent, "Imported " + n + " goal(s)" + where + ".", "Import",
			JOptionPane.INFORMATION_MESSAGE);
	}

	/** Copy a paste-ready share line carrying EVERY user section (one v2 code). */
	public static void copyAllSections(Component parent, GoalPlannerApiImpl api,
		ShareCodec codec, Supplier<String> playerName)
	{
		ShareBundle bundle = api.exportAllSectionsBundle(safeName(playerName));
		if (bundle == null)
		{
			JOptionPane.showMessageDialog(parent, "No user sections with goals to share.",
				"Share", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line = ShareText.invite(bundle, codec.encode(bundle));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(line), null);
		int sections = bundle.effectiveSections().size();
		JOptionPane.showMessageDialog(parent,
			"Copied a share code for " + sections + " section" + (sections == 1 ? "" : "s")
				+ " to your clipboard.\n"
				+ "Paste it in Discord or chat — anyone with the plugin can import it.\n"
				+ "(Multi-section codes need a recent plugin version to import.)",
			"Share", JOptionPane.INFORMATION_MESSAGE);
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

	/** Copy a paste-ready share line for the given goals to the clipboard. */
	public static void copyGoals(Component parent, GoalPlannerApiImpl api, ShareCodec codec,
		Supplier<String> playerName, List<String> goalIds)
	{
		ShareBundle bundle = api.exportGoalsBundle(goalIds, safeName(playerName));
		if (bundle == null || bundle.totalGoalCount() == 0)
		{
			JOptionPane.showMessageDialog(parent, "Nothing to share.", "Share",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line = ShareText.invite(bundle, codec.encode(bundle));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(line), null);
		int n = bundle.totalGoalCount();
		// A selection spanning sections exports per-section (v2 wire).
		String multiNote = bundle.needsV2()
			? "\n(The selection spans sections, which are preserved — importing"
				+ " needs a recent plugin version.)"
			: "";
		JOptionPane.showMessageDialog(parent,
			"Copied a share code for " + n + " goal" + (n == 1 ? "" : "s") + " to your clipboard.\n"
				+ "Paste it in Discord or chat — anyone with the plugin can import it."
				+ multiNote,
			"Share", JOptionPane.INFORMATION_MESSAGE);
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

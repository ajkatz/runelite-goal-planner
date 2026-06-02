package com.goalplanner.ui;

import com.goalplanner.api.GoalPlannerApiImpl;
import com.goalplanner.model.Section;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.goalplanner.share.ShareFormatException;
import com.goalplanner.share.ShareText;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
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

	/** Pick a user section → copy a paste-ready share line to the clipboard. */
	public static void promptCopySection(Component parent, GoalStore store, GoalPlannerApiImpl api,
		ShareCodec codec, Supplier<String> playerName)
	{
		Section section = pickUserSection(parent, store, "Copy share code");
		if (section == null)
		{
			return;
		}
		ShareBundle bundle = api.exportSectionBundle(section.getId(), safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "That section has no goals to share.",
				"Share", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line = ShareText.invite(bundle, codec.encode(bundle));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(line), null);
		JOptionPane.showMessageDialog(parent,
			"Copied a share code for \"" + section.getName() + "\" to your clipboard.\n"
				+ "Paste it in Discord or chat — anyone with the plugin can import it.",
			"Share", JOptionPane.INFORMATION_MESSAGE);
	}

	/** Pick a user section → broadcast it to the current RuneLite party. */
	public static void promptShareToParty(Component parent, GoalStore store, GoalPlannerApiImpl api,
		Supplier<String> playerName, BooleanSupplier inParty, Consumer<ShareBundle> shareToParty)
	{
		if (!inParty.getAsBoolean())
		{
			JOptionPane.showMessageDialog(parent,
				"You're not in a RuneLite party.\n"
					+ "Open the Party panel and create or join one, then try again.",
				"Share to party", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Section section = pickUserSection(parent, store, "Share to party");
		if (section == null)
		{
			return;
		}
		ShareBundle bundle = api.exportSectionBundle(section.getId(), safeName(playerName));
		if (bundle == null || bundle.getGoals().isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "That section has no goals to share.",
				"Share to party", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		shareToParty.accept(bundle);
		JOptionPane.showMessageDialog(parent,
			"Shared \"" + section.getName() + "\" to your party.",
			"Share to party", JOptionPane.INFORMATION_MESSAGE);
	}

	private static Section pickUserSection(Component parent, GoalStore store, String title)
	{
		List<Section> userSections = new ArrayList<>();
		for (Section s : store.getSections())
		{
			if (s != null && !s.isBuiltIn())
			{
				userSections.add(s);
			}
		}
		if (userSections.isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "You have no custom sections to share.",
				title, JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		String[] names = userSections.stream().map(Section::getName).toArray(String[]::new);
		String chosen = (String) JOptionPane.showInputDialog(parent, "Which section?", title,
			JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
		if (chosen == null)
		{
			return null;
		}
		for (Section s : userSections)
		{
			if (chosen.equals(s.getName()))
			{
				return s;
			}
		}
		return null;
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

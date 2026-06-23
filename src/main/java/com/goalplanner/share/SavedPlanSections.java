package com.goalplanner.share;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for the "Saved Plans" feature: read and override the section display
 * names a {@link ShareBundle} will create when imported. A saved plan stores a
 * list of desired names parallel to {@link ShareBundle#effectiveSections()};
 * these helpers read the current names (to populate the editor) and apply the
 * overrides (just before the plan is imported).
 */
public final class SavedPlanSections
{
	private SavedPlanSections()
	{
	}

	/**
	 * The section names this bundle would import as, parallel to
	 * {@link ShareBundle#effectiveSections()}. An entry may be {@code null} — a
	 * loose-goals bundle has no section name until one is assigned.
	 */
	public static List<String> sectionNamesOf(ShareBundle bundle)
	{
		List<String> names = new ArrayList<>();
		if (bundle == null)
		{
			return names;
		}
		for (SectionShareDto s : bundle.effectiveSections())
		{
			names.add(s.getName());
		}
		return names;
	}

	/**
	 * Override the bundle's section display names with {@code names} (parallel to
	 * {@link ShareBundle#effectiveSections()}). A null/blank entry leaves that
	 * section's original name unchanged. Mutates {@code bundle} in place.
	 *
	 * <p>For a loose-goals bundle, assigning a name promotes it to a named
	 * section so the chosen name actually shows on import.
	 */
	public static void applySectionNames(ShareBundle bundle, List<String> names)
	{
		if (bundle == null || names == null || names.isEmpty())
		{
			return;
		}
		List<SectionShareDto> secs = bundle.getSections();
		if (secs != null && !secs.isEmpty())
		{
			for (int i = 0; i < secs.size() && i < names.size(); i++)
			{
				String nm = clean(names.get(i));
				if (nm != null)
				{
					secs.get(i).setName(nm);
				}
			}
			return;
		}
		// v1 single-section path: the lone effective section maps to sectionName.
		String nm = clean(names.get(0));
		if (nm != null)
		{
			bundle.setSectionName(nm);
			// effectiveSections() only surfaces sectionName for SECTION-kind
			// bundles, so promote loose goals to a named section.
			if (bundle.getKind() != ShareBundle.Kind.SECTION)
			{
				bundle.setKind(ShareBundle.Kind.SECTION);
			}
		}
	}

	private static String clean(String s)
	{
		if (s == null)
		{
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}

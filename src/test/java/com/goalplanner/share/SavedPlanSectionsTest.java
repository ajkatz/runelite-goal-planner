package com.goalplanner.share;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SavedPlanSections — read & override a bundle's section display names")
class SavedPlanSectionsTest
{
	private static SectionShareDto section(String name)
	{
		SectionShareDto s = new SectionShareDto();
		s.setName(name);
		s.setGoals(new ArrayList<>());
		return s;
	}

	@Test
	@DisplayName("reads names from a v2 multi-section bundle")
	void readsV2Names()
	{
		ShareBundle b = new ShareBundle();
		b.setSections(Arrays.asList(section("Slayer"), section("Bossing")));
		assertEquals(Arrays.asList("Slayer", "Bossing"), SavedPlanSections.sectionNamesOf(b));
	}

	@Test
	@DisplayName("overrides per-section names in a v2 bundle; blank entry keeps original")
	void overridesV2()
	{
		ShareBundle b = new ShareBundle();
		b.setSections(Arrays.asList(section("Slayer"), section("Bossing")));
		SavedPlanSections.applySectionNames(b, Arrays.asList("My Slayer", "  "));
		assertEquals(Arrays.asList("My Slayer", "Bossing"), SavedPlanSections.sectionNamesOf(b));
	}

	@Test
	@DisplayName("overrides the section name of a v1 single-section bundle")
	void overridesV1Section()
	{
		ShareBundle b = new ShareBundle();
		b.setKind(ShareBundle.Kind.SECTION);
		b.setSectionName("Original");
		SavedPlanSections.applySectionNames(b, Arrays.asList("Renamed"));
		assertEquals(List.of("Renamed"), SavedPlanSections.sectionNamesOf(b));
		assertEquals("Renamed", b.getSectionName());
	}

	@Test
	@DisplayName("naming a loose-goals bundle promotes it to a named section")
	void promotesGoalsToNamedSection()
	{
		ShareBundle b = new ShareBundle();
		b.setKind(ShareBundle.Kind.GOALS);
		b.setGoals(new ArrayList<>());
		// before: effective section name is null (loose goals)
		assertEquals(java.util.Collections.singletonList(null), SavedPlanSections.sectionNamesOf(b));
		SavedPlanSections.applySectionNames(b, Arrays.asList("Banked Plan"));
		assertEquals(ShareBundle.Kind.SECTION, b.getKind());
		assertEquals(List.of("Banked Plan"), SavedPlanSections.sectionNamesOf(b));
	}

	@Test
	@DisplayName("null/empty overrides are a no-op")
	void noOpOnEmpty()
	{
		ShareBundle b = new ShareBundle();
		b.setSections(Arrays.asList(section("Keep")));
		SavedPlanSections.applySectionNames(b, null);
		SavedPlanSections.applySectionNames(b, new ArrayList<>());
		assertEquals(List.of("Keep"), SavedPlanSections.sectionNamesOf(b));
	}
}

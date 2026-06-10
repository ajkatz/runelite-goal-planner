package com.goalplanner.share;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Schema-v2 codec behaviour: version selection on encode (plain single-section
 * bundles stay on the v1 wire for older builds), dual-version decode, and the
 * version-neutral {@link ShareBundle#effectiveSections()} view.
 */
class ShareCodecV2Test
{
	private final ShareCodec codec = new ShareCodec(new Gson());

	private static GoalShareDto goal(int ref, String name)
	{
		GoalShareDto dto = new GoalShareDto();
		dto.setRef(ref);
		dto.setType("SKILL");
		dto.setName(name);
		dto.setSkillName("ATTACK");
		dto.setTargetValue(1_000_000);
		return dto;
	}

	private static SectionShareDto section(String name, boolean targetDefault, GoalShareDto... goals)
	{
		SectionShareDto s = new SectionShareDto();
		s.setName(name);
		s.setTargetDefault(targetDefault);
		s.setGoals(Arrays.asList(goals));
		return s;
	}

	@Test
	@DisplayName("a plain single-section bundle still emits the v1 wire so older builds import it")
	void plainSingleSectionStaysV1()
	{
		ShareBundle bundle = new ShareBundle();
		bundle.setSections(Collections.singletonList(section("Inferno", false, goal(0, "Ranged"))));

		String code = codec.encode(bundle);

		assertTrue(code.startsWith("GPSHARE1:"), code.substring(0, 12));
		ShareBundle decoded = codec.decode(code);
		assertEquals(1, decoded.getV());
		assertEquals(ShareBundle.Kind.SECTION, decoded.getKind());
		assertEquals("Inferno", decoded.getSectionName());
		assertEquals(1, decoded.effectiveSections().size());
		assertEquals("Inferno", decoded.effectiveSections().get(0).getName());
	}

	@Test
	@DisplayName("a legacy-built bundle (kind + flat goals) round-trips on the v1 wire")
	void legacyBuiltBundleRoundTripsV1()
	{
		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.GOALS);
		bundle.setGoals(Collections.singletonList(goal(0, "Attack 99")));

		String code = codec.encode(bundle);

		assertTrue(code.startsWith("GPSHARE1:"));
		ShareBundle decoded = codec.decode(code);
		assertEquals(1, decoded.getGoals().size());
		assertNull(decoded.getSections());
	}

	@Test
	@DisplayName("a multi-section bundle emits GPSHARE2 and round-trips all sections")
	void multiSectionRoundTripsV2()
	{
		ShareBundle bundle = ShareMapper.toMultiBundle(Arrays.asList(
			section("Slayer", false, goal(0, "Slayer 93")),
			section("Raids", false, goal(0, "Ranged 90"), goal(1, "Defence 70"))), "Andrew");

		String code = codec.encode(bundle);

		assertTrue(code.startsWith("GPSHARE2:"), code.substring(0, 12));
		ShareBundle decoded = codec.decode(code);
		assertEquals(2, decoded.getV());
		assertEquals("Andrew", decoded.getSharedBy());
		assertEquals(2, decoded.getSections().size());
		assertEquals("Slayer", decoded.getSections().get(0).getName());
		assertEquals(2, decoded.getSections().get(1).getGoals().size());
	}

	@Test
	@DisplayName("a single default-target section needs the v2 wire")
	void defaultTargetNeedsV2()
	{
		ShareBundle bundle = new ShareBundle();
		bundle.setSections(Collections.singletonList(section(null, true, goal(0, "Mining 99"))));

		String code = codec.encode(bundle);

		assertTrue(code.startsWith("GPSHARE2:"));
		assertTrue(codec.decode(code).getSections().get(0).isTargetDefault());
	}

	@Test
	@DisplayName("a v2 code embedded in chat text decodes (marker scan)")
	void v2EmbeddedInTextDecodes()
	{
		ShareBundle bundle = ShareMapper.toMultiBundle(Arrays.asList(
			section("A", false, goal(0, "x")),
			section("B", false, goal(0, "y"))), null);
		String code = codec.encode(bundle);
		String chat = "[Goal Planner] someone shared 2 sections — import it: " + code + " enjoy!";

		assertEquals(2, codec.decode(chat).getSections().size());
	}

	@Test
	@DisplayName("a v2 payload with no sections is rejected")
	void emptyV2Rejected()
	{
		// Hand-build a v2 wire payload with an empty section list.
		ShareBundle bad = new ShareBundle();
		bad.setV(ShareBundle.SCHEMA_VERSION);
		bad.setGoals(null);
		bad.setSections(Collections.emptyList());
		String code = "GPSHARE2:" + java.util.Base64.getUrlEncoder().withoutPadding()
			.encodeToString(gzip(new Gson().toJson(bad)));

		assertThrows(ShareFormatException.class, () -> codec.decode(code));
	}

	@Test
	@DisplayName("the import-count description counts goals across every section")
	void inviteLineDescribesMultiSection()
	{
		ShareBundle bundle = ShareMapper.toMultiBundle(Arrays.asList(
			section("A", false, goal(0, "x")),
			section("B", false, goal(0, "y"), goal(1, "z"))), "Andrew");

		String line = ShareText.invite(bundle, codec.encode(bundle));

		assertTrue(line.contains("2 sections (3 goals)"), line);
	}

	private static byte[] gzip(String json)
	{
		try
		{
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			try (java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(out))
			{
				gz.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			}
			return out.toByteArray();
		}
		catch (java.io.IOException e)
		{
			throw new AssertionError(e);
		}
	}
}

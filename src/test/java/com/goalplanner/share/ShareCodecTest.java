package com.goalplanner.share;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Round-trip and rejection behaviour for the share-code codec — the
 * compress/uncompress layer that the copy/paste
 * export sit on top of.
 */
public class ShareCodecTest
{
	// new Gson() is forbidden in shipped plugin code but fine in tests.
	private final ShareCodec codec = new ShareCodec(new Gson());

	private static ShareBundle sampleSection()
	{
		TagShareDto tag = new TagShareDto();
		tag.setLabel("Inferno");
		tag.setCategory("OTHER");
		tag.setColorRgb(0xAA3322);

		GoalShareDto ranged = new GoalShareDto();
		ranged.setRef(0);
		ranged.setType("SKILL");
		ranged.setName("Ranged - Level 75");
		ranged.setSkillName("RANGED");
		ranged.setTargetValue(1_210_421);
		ranged.setTags(Collections.singletonList(tag));

		GoalShareDto zuk = new GoalShareDto();
		zuk.setRef(1);
		zuk.setType("BOSS");
		zuk.setName("TzKal-Zuk");
		zuk.setBossName("TzKal-Zuk");
		zuk.setTargetValue(1);
		zuk.setOptional(false);
		// Zuk depends on the Ranged goal — encoded as a bundle-local ref.
		zuk.setRequires(Collections.singletonList(0));

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSharedBy("Andrew");
		bundle.setSectionName("Inferno Prep");
		bundle.setSectionColorRgb(0x112233);
		bundle.setGoals(Arrays.asList(ranged, zuk));
		return bundle;
	}

	@Test
	public void roundTripsASectionBundleLosslessly()
	{
		ShareBundle original = sampleSection();
		ShareBundle decoded = codec.decode(codec.encode(original));
		assertEquals(original, decoded);
	}

	@Test
	public void decodesDespiteEmbeddedWhitespaceFromWrappedPaste()
	{
		// A long code pasted from chat often wraps; the body arrives with newlines
		// and spaces interspersed. Decode must skip them, not truncate at the first.
		String code = codec.encode(sampleSection());
		int p = code.indexOf(':') + 1;
		String wrapped = code.substring(0, p)
			+ code.substring(p, p + 10) + "\n"
			+ code.substring(p + 10, p + 25) + "  \r\n"
			+ code.substring(p + 25) + "\n";
		ShareBundle decoded = codec.decode(wrapped);
		assertNotNull(decoded);
		assertEquals(codec.decode(code), decoded);
	}

	@Test
	public void encodedStringCarriesTheMagicVersionPrefix()
	{
		String code = codec.encode(sampleSection());
		assertTrue("expected GPSHARE1: prefix, got: " + code.substring(0, 12),
			code.startsWith("GPSHARE1:"));
	}

	@Test
	public void compressesRatherThanInflating()
	{
		// gzip should make the encoded blob meaningfully smaller than the raw JSON.
		ShareBundle bundle = sampleSection();
		int rawJsonLen = new Gson().toJson(bundle).length();
		int encodedLen = codec.encode(bundle).length();
		assertTrue("encoded (" + encodedLen + ") should be smaller than raw JSON (" + rawJsonLen + ")",
			encodedLen < rawJsonLen);
	}

	@Test
	public void emptyGoalListIsValid()
	{
		ShareBundle empty = new ShareBundle();
		empty.setKind(ShareBundle.Kind.GOALS);
		ShareBundle decoded = codec.decode(codec.encode(empty));
		assertNotNull(decoded.getGoals());
		assertTrue(decoded.getGoals().isEmpty());
	}

	@Test
	public void rejectsNullAndBlankInput()
	{
		assertRejected(null);
		assertRejected("");
		assertRejected("   ");
	}

	@Test
	public void rejectsTextWithoutThePrefix()
	{
		assertRejected("hello, this is not a share code");
		// Right payload, missing/foreign prefix.
		assertRejected("GPSHARE9:" + "abc");
	}

	@Test
	public void rejectsCorruptBase64AfterPrefix()
	{
		// A lone marker followed by a single base64 char can't form a valid
		// group — the surrounding text is ignored, the token "A" is corrupt.
		assertRejected("GPSHARE1:A and then some words");
	}

	@Test
	public void extractsCodeFromSurroundingInstructionText()
	{
		// The "get the plugin to import this" chat/Discord line wraps the code
		// in human text — decode must still find and read it.
		String code = codec.encode(sampleSection());
		String line = "Get the Goal Planner RuneLite plugin to import this: " + code + " :)";
		assertEquals(sampleSection(), codec.decode(line));
	}

	@Test
	public void rejectsValidBase64ThatIsNotGzip()
	{
		String notGzip = Base64.getUrlEncoder().withoutPadding()
			.encodeToString("just some bytes".getBytes());
		assertRejected("GPSHARE1:" + notGzip);
	}

	@Test
	public void rejectsAnIncompatibleSchemaVersion()
	{
		// encode() normalizes the version from the bundle shape, so a forged
		// version has to be smuggled in as a hand-built wire payload.
		ShareBundle bundle = sampleSection();
		bundle.setV(999);  // payload claims a future schema version
		byte[] json = new Gson().toJson(bundle).getBytes(java.nio.charset.StandardCharsets.UTF_8);
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		try (java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(out))
		{
			gz.write(json);
		}
		catch (java.io.IOException e)
		{
			throw new AssertionError(e);
		}
		String code = "GPSHARE1:" + Base64.getUrlEncoder().withoutPadding()
			.encodeToString(out.toByteArray());
		assertRejected(code);
	}

	private void assertRejected(String input)
	{
		try
		{
			codec.decode(input);
			fail("expected ShareFormatException for: " + input);
		}
		catch (ShareFormatException expected)
		{
			// good
		}
	}
}

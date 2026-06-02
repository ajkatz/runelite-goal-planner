package com.goalplanner.share;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes a {@link ShareBundle} to (and from) a compact, copy-safe string:
 *
 * <pre>  ShareBundle → Gson JSON → gzip → URL-safe Base64, prefixed "GPSHARE1:"</pre>
 *
 * <p>The {@code GPSHARE&lt;n&gt;:} prefix is a magic marker + schema version: it
 * lets {@link #decode(String)} reject arbitrary pasted text and incompatible
 * versions before doing any work. The same encoded string is the payload for
 * both the in-client Party transport and a copy/paste export.
 *
 * <p>Gzip matters because goal data is highly repetitive (enum names, level
 * targets) and the Party WebSocket caps message size — compression keeps a
 * typical section inside a single message.
 *
 * <p>Takes an injected {@link Gson}; the plugin hub rejects {@code new Gson()},
 * so production wires the shared client instance. Pure and side-effect free.
 */
public final class ShareCodec
{
	/** Magic marker + schema version. Decode rejects anything without it. */
	static final String PREFIX = "GPSHARE" + ShareBundle.SCHEMA_VERSION + ":";

	private final Gson gson;

	public ShareCodec(Gson gson)
	{
		this.gson = gson;
	}

	/** Encode a bundle to a prefixed, gzipped, URL-safe-Base64 string. */
	public String encode(ShareBundle bundle)
	{
		if (bundle == null)
		{
			throw new ShareFormatException("nothing to share");
		}
		byte[] json = gson.toJson(bundle).getBytes(StandardCharsets.UTF_8);
		byte[] gz = gzip(json);
		return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(gz);
	}

	/**
	 * Decode a string produced by {@link #encode(ShareBundle)}.
	 *
	 * @throws ShareFormatException if the input is null/blank, lacks the
	 *         {@link #PREFIX} (wrong tool or version), or is corrupt.
	 */
	public ShareBundle decode(String text)
	{
		if (text == null || text.trim().isEmpty())
		{
			throw new ShareFormatException("empty share code");
		}
		// The code may be embedded in surrounding instruction text — e.g. a
		// "get the plugin to import this: <code>" chat/Discord line — so locate
		// the marker and read the base64url token that follows it, rather than
		// requiring the code to sit alone.
		int marker = text.indexOf(PREFIX);
		if (marker < 0)
		{
			throw new ShareFormatException("unrecognised or wrong-version share code");
		}
		int start = marker + PREFIX.length();
		int end = start;
		while (end < text.length() && isBase64Url(text.charAt(end)))
		{
			end++;
		}
		String b64 = text.substring(start, end);

		byte[] gz;
		try
		{
			gz = Base64.getUrlDecoder().decode(b64);
		}
		catch (IllegalArgumentException e)
		{
			throw new ShareFormatException("corrupt share code", e);
		}

		String json = new String(gunzip(gz), StandardCharsets.UTF_8);

		ShareBundle bundle;
		try
		{
			bundle = gson.fromJson(json, ShareBundle.class);
		}
		catch (JsonSyntaxException e)
		{
			throw new ShareFormatException("invalid share payload", e);
		}
		if (bundle == null || bundle.getGoals() == null)
		{
			throw new ShareFormatException("empty share payload");
		}
		if (bundle.getV() != ShareBundle.SCHEMA_VERSION)
		{
			throw new ShareFormatException(
				"share code is from an incompatible plugin version (v" + bundle.getV() + ")");
		}
		return bundle;
	}

	/** URL-safe Base64 alphabet (no padding): {@code [A-Za-z0-9_-]}. */
	private static boolean isBase64Url(char c)
	{
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
			|| (c >= '0' && c <= '9') || c == '-' || c == '_';
	}

	private static byte[] gzip(byte[] data)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPOutputStream gz = new GZIPOutputStream(out))
		{
			gz.write(data);
		}
		catch (IOException e)
		{
			// In-memory streams don't do real IO; treat as unexpected.
			throw new ShareFormatException("failed to compress share payload", e);
		}
		return out.toByteArray();
	}

	private static byte[] gunzip(byte[] data)
	{
		try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data)))
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int n;
			while ((n = gz.read(buf)) != -1)
			{
				out.write(buf, 0, n);
			}
			return out.toByteArray();
		}
		catch (IOException e)
		{
			throw new ShareFormatException("corrupt share code", e);
		}
	}
}

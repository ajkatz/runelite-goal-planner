package com.goalplanner.share;

/**
 * Thrown by {@link ShareCodec#decode(String)} when the input is not a
 * recognisable, current-version, well-formed share code. Callers (paste
 * dialog, party receive handler) catch this to show a friendly "couldn't read
 * that share code" message instead of failing hard.
 */
public class ShareFormatException extends RuntimeException
{
	public ShareFormatException(String message)
	{
		super(message);
	}

	public ShareFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

package com.goalplanner.share;

import net.runelite.client.party.messages.PartyMemberMessage;

/**
 * Party-transport carrier for a share code. The sharer broadcasts one of these
 * to party members via {@code PartyService.send(...)}; recipients receive it via
 * an {@code @Subscribe} handler, decode the {@link #getCode() code} with
 * {@link ShareCodec}, and are prompted to import.
 *
 * <p>Mirrors RuneLite's built-in {@code PartyChatMessage}: a single value field
 * carried over the party WebSocket and (de)serialized by its Gson. The no-arg
 * constructor exists for that deserialization. Extending the core client's
 * {@link PartyMemberMessage} is a compile-time dependency on the RuneLite client
 * API (available via {@code compileOnly}) — not cross-plugin interop, so it does
 * not need reflection.
 */
public class GoalSharePartyMessage extends PartyMemberMessage
{
	private String code;

	public GoalSharePartyMessage()
	{
	}

	public GoalSharePartyMessage(String code)
	{
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}
}

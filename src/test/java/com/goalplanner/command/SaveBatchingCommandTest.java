package com.goalplanner.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SaveBatchingCommand")
class SaveBatchingCommandTest
{
	/** Records the call order of begin/end/apply/revert. */
	private static final class Trace
	{
		final List<String> log = new ArrayList<>();
	}

	private static Command tracingInner(Trace t, boolean applyResult)
	{
		return new Command()
		{
			@Override public boolean apply() { t.log.add("apply"); return applyResult; }
			@Override public boolean revert() { t.log.add("revert"); return true; }
			@Override public String getDescription() { return "inner"; }
		};
	}

	@Test
	@DisplayName("brackets apply with begin/end and returns the inner result")
	void bracketsApply()
	{
		Trace t = new Trace();
		Command cmd = new SaveBatchingCommand(
			() -> t.log.add("begin"), () -> t.log.add("end"), tracingInner(t, true));

		assertTrue(cmd.apply());
		assertEquals(List.of("begin", "apply", "end"), t.log);
	}

	@Test
	@DisplayName("brackets revert with begin/end")
	void bracketsRevert()
	{
		Trace t = new Trace();
		Command cmd = new SaveBatchingCommand(
			() -> t.log.add("begin"), () -> t.log.add("end"), tracingInner(t, true));

		assertTrue(cmd.revert());
		assertEquals(List.of("begin", "revert", "end"), t.log);
	}

	@Test
	@DisplayName("runs end even when the inner command throws (no stuck suspend)")
	void endRunsOnThrow()
	{
		Trace t = new Trace();
		Command throwing = new Command()
		{
			@Override public boolean apply() { t.log.add("apply"); throw new RuntimeException("boom"); }
			@Override public boolean revert() { return true; }
			@Override public String getDescription() { return "boom"; }
		};
		Command cmd = new SaveBatchingCommand(
			() -> t.log.add("begin"), () -> t.log.add("end"), throwing);

		assertThrows(RuntimeException.class, cmd::apply);
		assertEquals(List.of("begin", "apply", "end"), t.log);
	}

	@Test
	@DisplayName("delegates description to the inner command")
	void delegatesDescription()
	{
		Command cmd = new SaveBatchingCommand(() -> {}, () -> {}, tracingInner(new Trace(), true));
		assertEquals("inner", cmd.getDescription());
	}
}

package com.goalplanner.command;

/**
 * Wraps another {@link Command} so its apply()/revert() run with persistence
 * suspended, flushing once at the end instead of writing per mutation. Use for
 * bulk commands (e.g. importing many goals/sections) whose undo/redo would
 * otherwise issue one config write per entity - the dominant cost on a large
 * import's undo/redo.
 *
 * <p>The begin/end hooks are passed as Runnables (typically {@code
 * GoalStore::suspendSave} / {@code GoalStore::resumeSave}) so this stays in the
 * command layer without depending on persistence. {@code end} runs in a finally
 * so a throwing inner command can't leave saves permanently suspended.
 */
public final class SaveBatchingCommand implements Command
{
	private final Runnable begin;
	private final Runnable end;
	private final Command inner;

	public SaveBatchingCommand(Runnable begin, Runnable end, Command inner)
	{
		this.begin = begin;
		this.end = end;
		this.inner = inner;
	}

	@Override
	public boolean apply()
	{
		begin.run();
		try
		{
			return inner.apply();
		}
		finally
		{
			end.run();
		}
	}

	@Override
	public boolean revert()
	{
		begin.run();
		try
		{
			return inner.revert();
		}
		finally
		{
			end.run();
		}
	}

	@Override
	public String getDescription()
	{
		return inner.getDescription();
	}
}

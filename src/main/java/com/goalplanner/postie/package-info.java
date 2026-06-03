/**
 * Postie — a self-contained cross-plugin action-sharing module over RuneLite's
 * {@link net.runelite.client.events.PluginMessage}, vendored inside Goal Planner.
 *
 * <p>Everything in this package depends <b>only on RuneLite core types</b>
 * ({@code PluginMessage}, {@code EventBus}) — never on Goal Planner classes — so
 * the whole package can be lifted out into a standalone library or plugin later
 * with no changes. Goal Planner is just its first consumer (it accepts the
 * {@code goalplanner:import-share} action and answers discovery).
 *
 * @see com.goalplanner.postie.Postie
 */
package com.goalplanner.postie;

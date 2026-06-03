package com.goalplanner.postie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.client.events.PluginMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the self-contained {@link Postie} module — the action convention,
 * version stamp, receiver matching, and discovery handshake.
 */
public class PostieTest
{
	@Test
	public void actionAddressesTheTargetNamespaceAndCarriesData()
	{
		PluginMessage msg = Postie.action("goalplanner", "import-share", Map.of("code", "GPSHARE1:abc"));
		assertEquals("goalplanner", msg.getNamespace());
		assertEquals("import-share", msg.getName());
		assertEquals("GPSHARE1:abc", msg.getData().get("code"));
		assertEquals(Postie.VERSION, msg.getData().get(Postie.VERSION_KEY));
	}

	@Test
	public void isActionMatchesNamespaceAndName()
	{
		PluginMessage msg = Postie.action("goalplanner", "import-share", null);
		assertTrue(Postie.isAction(msg, "goalplanner", "import-share"));
		assertFalse(Postie.isAction(msg, "goalplanner", "other"));
		assertFalse(Postie.isAction(msg, "other", "import-share"));
	}

	@Test
	public void recognisesDiscover()
	{
		PluginMessage msg = new PluginMessage(Postie.POSTIE_NAMESPACE, Postie.DISCOVER, Map.of());
		assertTrue(Postie.isDiscover(msg));
		assertFalse(Postie.isAnnounce(msg));
	}

	@Test
	public void recognisesAndParsesAnnounce()
	{
		Map<String, Object> data = new HashMap<>();
		data.put(Postie.KEY_NAMESPACE, "goalplanner");
		data.put(Postie.KEY_ACTIONS, List.of("import-share", "open"));
		PluginMessage msg = new PluginMessage(Postie.POSTIE_NAMESPACE, Postie.ANNOUNCE, data);

		assertTrue(Postie.isAnnounce(msg));
		assertEquals("goalplanner", Postie.announcedNamespace(msg));
		assertEquals(List.of("import-share", "open"), Postie.announcedActions(msg));
	}

	@Test
	public void announceParsersAreNullSafe()
	{
		PluginMessage msg = new PluginMessage(Postie.POSTIE_NAMESPACE, Postie.ANNOUNCE, Map.of());
		assertEquals(null, Postie.announcedNamespace(msg));
		assertTrue(Postie.announcedActions(msg).isEmpty());
	}
}

package com.goaltracker.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Attributes for drop sources (e.g., "requires slayer task").
 * Items from sources with attributes inherit those attributes as additional tags.
 */
public final class SourceAttributes
{
	private SourceAttributes() {}

	public enum Attribute
	{
		SLAYER_TASK("Slayer Task");

		private final String displayName;

		Attribute(String displayName)
		{
			this.displayName = displayName;
		}

		public String getDisplayName()
		{
			return displayName;
		}
	}

	private static final Map<String, Set<Attribute>> SOURCE_ATTRS = new HashMap<>();

	/**
	 * Direct slayer-item inheritance: item IDs that should receive the
	 * SKILLING Slayer tag regardless of which boss sources they're attached
	 * to. Used for slayer-monster drops whose source monster isn't currently
	 * registered as a boss in {@link ItemSourceData} (e.g. Cockatrice,
	 * Basilisk, Kurask, Gargoyles, Wyverns), and for drops that don't have
	 * a single source at all (Imbued heart, Eternal gem — drop from any
	 * superior slayer creature).
	 *
	 * <p>Complements the source-based {@link #isSlayerTask(String)}
	 * inheritance in {@code buildItemTags}: an item gets the SKILLING
	 * Slayer tag if EITHER any of its sources is a registered slayer-task
	 * boss OR its id is in this set.
	 */
	private static final Set<Integer> DIRECT_SLAYER_ITEMS = new HashSet<>();

	static
	{
		slayerTask("Abyssal Sire");
		slayerTask("Alchemical Hydra");
		slayerTask("Cerberus");
		slayerTask("Thermonuclear Smoke Devil");
		slayerTask("Kraken");
		slayerTask("Grotesque Guardians");
		slayerTask("Araxxor");
		slayerTask("Shellbane Gryphon");

		// Direct slayer-item inheritance list (replaces the old OTHER "Slayer"
		// category in ItemSourceData). Every item below drops from a slayer
		// monster or is otherwise a slayer-themed reward. Items whose boss
		// source IS already a registered slayer task above will receive the
		// SKILLING Slayer tag via inheritance — listing them here is
		// harmless (idempotent) and serves as documentation.
		slayerItem(7975);   // Crawling hand (item)
		slayerItem(7976);   // Cockatrice head
		slayerItem(7977);   // Basilisk head
		slayerItem(7978);   // Kurask head
		slayerItem(7979);   // Abyssal head
		slayerItem(20724);  // Imbued heart (drops from any superior slayer creature)
		slayerItem(21270);  // Eternal gem (drops from any superior slayer creature)
		slayerItem(20736);  // Dust battlestaff (Dust devils)
		slayerItem(20730);  // Mist battlestaff (Skeletal Wyverns)
		slayerItem(4151);   // Abyssal whip (Abyssal demons / Sire)
		slayerItem(4153);   // Granite maul (Gargoyles)
		slayerItem(12848);  // Granite maul (ornament kit variant)
		slayerItem(6665);   // Mudskipper hat (Mogres)
		slayerItem(6666);   // Flippers (Mogres)
		slayerItem(11037);  // Brine sabre (Brine rats)
		slayerItem(11902);  // Leaf-bladed sword (Turoths / Kurask)
		slayerItem(20727);  // Leaf-bladed battleaxe (Turoths / Kurask)
		slayerItem(21646);  // Granite longsword (Gargoyles)
		slayerItem(21643);  // Granite boots (Gargoyles)
		slayerItem(21637);  // Wyvern visage (Wyverns)
		slayerItem(6809);   // Granite legs (Gargoyles)
		slayerItem(10589);  // Granite helm (Gargoyles)
		slayerItem(11286);  // Draconic visage (Steel/Iron/Mithril dragons)
		slayerItem(4119);   // Bronze boots
		slayerItem(4121);   // Iron boots
		slayerItem(4123);   // Steel boots
		slayerItem(4125);   // Black boots
		slayerItem(4127);   // Mithril boots
		slayerItem(4129);   // Adamant boots
		slayerItem(4131);   // Rune boots
		slayerItem(11840);  // Dragon boots (Spiritual warriors)
		slayerItem(13265);  // Abyssal dagger
		slayerItem(13267);  // Abyssal dagger
		slayerItem(13269);  // Abyssal dagger
		slayerItem(13271);  // Abyssal dagger
		slayerItem(12004);  // Kraken tentacle (Cave kraken)
		slayerItem(11235);  // Dark bow (Dark beasts)
		slayerItem(12765);  // Dark bow
		slayerItem(12766);  // Dark bow
		slayerItem(12767);  // Dark bow
		slayerItem(12768);  // Dark bow
		slayerItem(12002);  // Occult necklace (Cave kraken / Smoke devils)
		slayerItem(3140);   // Dragon chainbody (Steel/Mithril dragons)
		slayerItem(20849);  // Dragon thrownaxe
		slayerItem(21028);  // Dragon harpoon (Kraken / Wyrms)
		slayerItem(21009);  // Dragon sword (Wyrms)
		slayerItem(22804);  // Dragon knife (Wyrms)
		slayerItem(22806);  // Dragon knife
		slayerItem(22808);  // Dragon knife
		slayerItem(22810);  // Dragon knife
		slayerItem(22963);  // Broken dragon hasta (Drakes)
		slayerItem(22960);  // Drake's tooth
		slayerItem(22957);  // Drake's claw
		slayerItem(22988);  // Hydra tail
		slayerItem(22971);  // Hydra's fang
		slayerItem(22973);  // Hydra's eye
		slayerItem(22969);  // Hydra's heart
		slayerItem(4109);   // Mystic hat (light) — Dagannoth (non-king) / Fire giant
		slayerItem(4111);   // Mystic robe top (light)
		slayerItem(4113);   // Mystic robe bottom (light)
		slayerItem(4115);   // Mystic gloves (light)
		slayerItem(4117);   // Mystic boots (light)
		slayerItem(4099);   // Mystic hat (dark)
		slayerItem(4101);   // Mystic robe top (dark)
		slayerItem(4103);   // Mystic robe bottom (dark)
		slayerItem(4105);   // Mystic gloves (dark)
		slayerItem(4107);   // Mystic boots (dark)
		slayerItem(23047);  // Mystic hat (dusk)
		slayerItem(23050);  // Mystic robe top (dusk)
		slayerItem(23053);  // Mystic robe bottom (dusk)
		slayerItem(23056);  // Mystic gloves (dusk)
		slayerItem(23059);  // Mystic boots (dusk)
		slayerItem(24268);  // Basilisk jaw (Basilisk knights)
		slayerItem(32876);  // Aquanite tendon
		slayerItem(24288);  // Dagon'hai hat (Ancient wizards)
		slayerItem(24291);  // Dagon'hai robe top
		slayerItem(24294);  // Dagon'hai robe bottom
		slayerItem(24777);  // Blood shard (Vyrewatch sentinels)
		slayerItem(26225);  // Ancient ceremonial mask (Warped creatures)
		slayerItem(26221);  // Ancient ceremonial top
		slayerItem(26223);  // Ancient ceremonial legs
		slayerItem(26227);  // Ancient ceremonial gloves
		slayerItem(26229);  // Ancient ceremonial boots
		slayerItem(29084);  // Sulphur blades
		slayerItem(29455);  // Teleport anchoring scroll
		slayerItem(29806);  // Aranea boots (Araneas / Araxxor area)
		slayerItem(29889);  // Glacial temotli (Amoxliatl — but also slayer)
		slayerItem(29895);  // Frozen tear
		slayerItem(30957);  // Earthbound tecpatl
		slayerItem(31081);  // Antler guard
		slayerItem(31084);  // Alchemist's signet
		slayerItem(31086);  // Broken antler
		slayerItem(31996);  // Dragon metal sheet
		slayerItem(31235);  // Gryphon feather
	}

	private static void slayerTask(String sourceName)
	{
		SOURCE_ATTRS.computeIfAbsent(sourceName, k -> new HashSet<>()).add(Attribute.SLAYER_TASK);
	}

	private static void slayerItem(int itemId)
	{
		DIRECT_SLAYER_ITEMS.add(itemId);
	}

	/**
	 * Get attributes for a source, or empty set if none.
	 */
	public static Set<Attribute> getAttributes(String sourceName)
	{
		return SOURCE_ATTRS.getOrDefault(sourceName, Collections.emptySet());
	}

	/**
	 * Check if a source requires a slayer task.
	 */
	public static boolean isSlayerTask(String sourceName)
	{
		return getAttributes(sourceName).contains(Attribute.SLAYER_TASK);
	}

	/**
	 * Check if an item id is directly registered as a slayer-themed drop,
	 * regardless of whether it has boss sources in {@link ItemSourceData}.
	 * Used by {@code buildItemTags} to attach the SKILLING Slayer tag to
	 * drops from slayer monsters that aren't currently registered as boss
	 * sources (Gargoyles, Wyverns, Cockatrice, etc.) and to drops with
	 * no specific source (Imbued heart, Eternal gem).
	 */
	public static boolean isSlayerItem(int itemId)
	{
		return DIRECT_SLAYER_ITEMS.contains(itemId);
	}
}

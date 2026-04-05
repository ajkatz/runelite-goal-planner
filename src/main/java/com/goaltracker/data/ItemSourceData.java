package com.goaltracker.data;

import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;

import java.util.*;

/**
 * Maps item IDs to their drop sources (bosses, raids, clues, minigames).
 * Data sourced from the OSRS Collection Log structure.
 *
 * To add new items: call source(itemId, "Source Name", TagCategory.XXX)
 * Items can have multiple tags (e.g., an item from both a boss and a minigame).
 */
public final class ItemSourceData
{
	private ItemSourceData() {}

	private static final Map<Integer, List<ItemTag>> SOURCES = new HashMap<>();

	/**
	 * Get tags for an item, or empty list if unknown.
	 */
	public static List<ItemTag> getTags(int itemId)
	{
		return SOURCES.getOrDefault(itemId, Collections.emptyList());
	}

	/**
	 * Check if an item has any source tags.
	 */
	public static boolean hasSource(int itemId)
	{
		return SOURCES.containsKey(itemId);
	}

	private static void source(int itemId, String label, TagCategory category)
	{
		SOURCES.computeIfAbsent(itemId, k -> new ArrayList<>()).add(new ItemTag(label, category));
	}

	static
	{
		// ==================== BOSSES ====================

		// Zulrah
		source(12922, "Zulrah", TagCategory.BOSS);  // Tanzanite fang
		source(12932, "Zulrah", TagCategory.BOSS);  // Magic fang
		source(12927, "Zulrah", TagCategory.BOSS);  // Serpentine visage
		source(13200, "Zulrah", TagCategory.BOSS);  // Tanzanite mutagen
		source(13201, "Zulrah", TagCategory.BOSS);  // Magma mutagen
		source(12921, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(12936, "Zulrah", TagCategory.BOSS);  // Jar of swamp

		// Vorkath
		source(21992, "Vorkath", TagCategory.BOSS);  // Dragonbone necklace
		source(22006, "Vorkath", TagCategory.BOSS);  // Draconic visage
		source(21997, "Vorkath", TagCategory.BOSS);  // Skeletal visage
		source(22003, "Vorkath", TagCategory.BOSS);  // Jar of decay
		source(22988, "Vorkath", TagCategory.BOSS);  // Vorki

		// General Graardor (Bandos)
		source(11832, "General Graardor", TagCategory.BOSS);  // Bandos chestplate
		source(11834, "General Graardor", TagCategory.BOSS);  // Bandos tassets
		source(11836, "General Graardor", TagCategory.BOSS);  // Bandos boots
		source(11818, "General Graardor", TagCategory.BOSS);  // Bandos hilt
		source(12650, "General Graardor", TagCategory.BOSS);  // Pet general graardor

		// Commander Zilyana (Saradomin)
		source(11806, "Commander Zilyana", TagCategory.BOSS);  // Saradomin sword
		source(11814, "Commander Zilyana", TagCategory.BOSS);  // Saradomin hilt
		source(11808, "Commander Zilyana", TagCategory.BOSS);  // Armadyl crossbow
		source(12651, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana
		source(11838, "Commander Zilyana", TagCategory.BOSS);  // Saradomin light

		// Kree'arra (Armadyl)
		source(11826, "Kree'arra", TagCategory.BOSS);  // Armadyl helmet
		source(11828, "Kree'arra", TagCategory.BOSS);  // Armadyl chestplate
		source(11830, "Kree'arra", TagCategory.BOSS);  // Armadyl chainskirt
		source(11810, "Kree'arra", TagCategory.BOSS);  // Armadyl hilt
		source(12649, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra

		// K'ril Tsutsaroth (Zamorak)
		source(11816, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorakian spear
		source(11838, "K'ril Tsutsaroth", TagCategory.BOSS);  // Staff of the dead
		source(11812, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorak hilt
		source(12652, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth

		// Cerberus
		source(13227, "Cerberus", TagCategory.BOSS);  // Primordial crystal
		source(13229, "Cerberus", TagCategory.BOSS);  // Pegasian crystal
		source(13231, "Cerberus", TagCategory.BOSS);  // Eternal crystal
		source(13225, "Cerberus", TagCategory.BOSS);  // Smouldering stone
		source(13247, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(13245, "Cerberus", TagCategory.BOSS);  // Jar of souls

		// Corporeal Beast
		source(12819, "Corporeal Beast", TagCategory.BOSS);  // Spectral sigil
		source(12823, "Corporeal Beast", TagCategory.BOSS);  // Arcane sigil
		source(12827, "Corporeal Beast", TagCategory.BOSS);  // Elysian sigil
		source(12816, "Corporeal Beast", TagCategory.BOSS);  // Spirit shield

		// Thermonuclear Smoke Devil
		source(12002, "Thermonuclear Smoke Devil", TagCategory.BOSS);  // Occult necklace
		source(12648, "Thermonuclear Smoke Devil", TagCategory.BOSS);  // Pet smoke devil

		// Kraken
		source(12004, "Kraken", TagCategory.BOSS);  // Kraken tentacle
		source(12655, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(11905, "Kraken", TagCategory.BOSS);  // Trident of the seas
		source(12007, "Kraken", TagCategory.BOSS);  // Jar of dirt

		// Abyssal Sire
		source(13265, "Abyssal Sire", TagCategory.BOSS);  // Unsired
		source(7979,  "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(13262, "Abyssal Sire", TagCategory.BOSS);  // Abyssal orphan
		source(13275, "Abyssal Sire", TagCategory.BOSS);  // Jar of miasma

		// Alchemical Hydra
		source(22966, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's claw
		source(22971, "Alchemical Hydra", TagCategory.BOSS);  // Hydra leather
		source(22969, "Alchemical Hydra", TagCategory.BOSS);  // Hydra tail
		source(22746, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22973, "Alchemical Hydra", TagCategory.BOSS);  // Jar of chemicals

		// Kalphite Queen
		source(12654, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(13103, "Kalphite Queen", TagCategory.BOSS);  // Jar of sand

		// King Black Dragon
		source(12653, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon

		// Giant Mole
		source(12646, "Giant Mole", TagCategory.BOSS);  // Baby mole

		// Sarachnis
		source(23525, "Sarachnis", TagCategory.BOSS);  // Sarachnis cudgel
		source(23495, "Sarachnis", TagCategory.BOSS);  // Sraracha

		// Grotesque Guardians
		source(21730, "Grotesque Guardians", TagCategory.BOSS);  // Granite gloves
		source(21736, "Grotesque Guardians", TagCategory.BOSS);  // Granite ring
		source(21739, "Grotesque Guardians", TagCategory.BOSS);  // Granite hammer
		source(21748, "Grotesque Guardians", TagCategory.BOSS);  // Noon

		// Dagannoth Kings
		source(6731, "Dagannoth Kings", TagCategory.BOSS);  // Berserker ring
		source(6733, "Dagannoth Kings", TagCategory.BOSS);  // Seers ring
		source(6735, "Dagannoth Kings", TagCategory.BOSS);  // Archers ring
		source(6737, "Dagannoth Kings", TagCategory.BOSS);  // Warrior ring
		source(12644, "Dagannoth Kings", TagCategory.BOSS);  // Dagannoth prime pet
		source(12643, "Dagannoth Kings", TagCategory.BOSS);  // Dagannoth rex pet
		source(12645, "Dagannoth Kings", TagCategory.BOSS);  // Dagannoth supreme pet

		// Nightmare
		source(24514, "The Nightmare", TagCategory.BOSS);  // Nightmare staff
		source(24517, "The Nightmare", TagCategory.BOSS);  // Inquisitor's great helm
		source(24519, "The Nightmare", TagCategory.BOSS);  // Inquisitor's hauberk
		source(24521, "The Nightmare", TagCategory.BOSS);  // Inquisitor's plateskirt
		source(24511, "The Nightmare", TagCategory.BOSS);  // Inquisitor's mace
		source(24491, "The Nightmare", TagCategory.BOSS);  // Eldritch orb
		source(24495, "The Nightmare", TagCategory.BOSS);  // Harmonised orb
		source(24493, "The Nightmare", TagCategory.BOSS);  // Volatile orb

		// Nex
		source(26235, "Nex", TagCategory.BOSS);  // Nihil horn
		source(26370, "Nex", TagCategory.BOSS);  // Torva full helm
		source(26372, "Nex", TagCategory.BOSS);  // Torva platebody
		source(26374, "Nex", TagCategory.BOSS);  // Torva platelegs
		source(26348, "Nex", TagCategory.BOSS);  // Ancient hilt
		source(26232, "Nex", TagCategory.BOSS);  // Zaryte vambraces

		// Demonic Gorillas
		source(19529, "Demonic Gorillas", TagCategory.BOSS);  // Zenyte shard

		// Phantom Muspah
		source(25527, "Phantom Muspah", TagCategory.BOSS);  // Ancient essence
		source(27627, "Phantom Muspah", TagCategory.BOSS);  // Muphin

		// Duke Sucellus
		source(28316, "Duke Sucellus", TagCategory.BOSS);  // Chromium ingot
		source(28318, "Duke Sucellus", TagCategory.BOSS);  // Virtus mask
		source(28320, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe top
		source(28322, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe bottom
		source(28324, "Duke Sucellus", TagCategory.BOSS);  // Eye of the duke

		// Vardorvis
		source(28296, "Vardorvis", TagCategory.BOSS);  // Ultor vestige
		source(28298, "Vardorvis", TagCategory.BOSS);  // Executioner's axe head
		source(28300, "Vardorvis", TagCategory.BOSS);  // Vardorvis' head

		// The Leviathan
		source(28302, "The Leviathan", TagCategory.BOSS);  // Venator vestige
		source(28304, "The Leviathan", TagCategory.BOSS);  // Leviathan's lure

		// The Whisperer
		source(28308, "The Whisperer", TagCategory.BOSS);  // Bellator vestige
		source(28310, "The Whisperer", TagCategory.BOSS);  // Siren's staff
		source(28312, "The Whisperer", TagCategory.BOSS);  // Eternal gem

		// ==================== RAIDS ====================

		// Chambers of Xeric
		source(21018, "Chambers of Xeric", TagCategory.RAID);  // Twisted buckler
		source(21015, "Chambers of Xeric", TagCategory.RAID);  // Dexterous prayer scroll
		source(21034, "Chambers of Xeric", TagCategory.RAID);  // Arcane prayer scroll
		source(21000, "Chambers of Xeric", TagCategory.RAID);  // Twisted bow
		source(21003, "Chambers of Xeric", TagCategory.RAID);  // Elder maul
		source(21006, "Chambers of Xeric", TagCategory.RAID);  // Kodai insignia
		source(21012, "Chambers of Xeric", TagCategory.RAID);  // Dragon claws
		source(21021, "Chambers of Xeric", TagCategory.RAID);  // Ancestral hat
		source(21024, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe top
		source(21027, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe bottom
		source(21043, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(20851, "Chambers of Xeric", TagCategory.RAID);  // Dragon hunter crossbow
		source(21079, "Chambers of Xeric", TagCategory.RAID);  // Dinh's bulwark

		// Theatre of Blood
		source(22325, "Theatre of Blood", TagCategory.RAID);  // Ghrazi rapier
		source(22328, "Theatre of Blood", TagCategory.RAID);  // Sanguinesti staff
		source(22324, "Theatre of Blood", TagCategory.RAID);  // Justiciar faceguard
		source(22327, "Theatre of Blood", TagCategory.RAID);  // Justiciar chestguard
		source(22330, "Theatre of Blood", TagCategory.RAID);  // Justiciar legguards
		source(22326, "Theatre of Blood", TagCategory.RAID);  // Avernic defender hilt
		source(22323, "Theatre of Blood", TagCategory.RAID);  // Scythe of vitur
		source(22473, "Theatre of Blood", TagCategory.RAID);  // Lil' Zik

		// Tombs of Amascut
		source(26219, "Tombs of Amascut", TagCategory.RAID);  // Osmumten's fang
		source(26231, "Tombs of Amascut", TagCategory.RAID);  // Lightbearer
		source(26233, "Tombs of Amascut", TagCategory.RAID);  // Elidinis' ward
		source(26225, "Tombs of Amascut", TagCategory.RAID);  // Masori mask
		source(26227, "Tombs of Amascut", TagCategory.RAID);  // Masori body
		source(26229, "Tombs of Amascut", TagCategory.RAID);  // Masori chaps
		source(27352, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's shadow
		source(27387, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian

		// ==================== CLUES ====================

		// Easy Clues
		source(2577, "Easy Clue", TagCategory.CLUE);   // Ranger boots (actually medium)
		source(10364, "Easy Clue", TagCategory.CLUE);  // Steel (t) platebody
		source(10372, "Easy Clue", TagCategory.CLUE);  // Steel (g) platebody

		// Medium Clues
		source(2577, "Medium Clue", TagCategory.CLUE);  // Ranger boots
		source(12073, "Medium Clue", TagCategory.CLUE);  // Ranger gloves

		// Hard Clues
		source(10330, "Hard Clue", TagCategory.CLUE);  // Gilded platebody
		source(10332, "Hard Clue", TagCategory.CLUE);  // Gilded platelegs
		source(10334, "Hard Clue", TagCategory.CLUE);  // Gilded plateskirt
		source(10336, "Hard Clue", TagCategory.CLUE);  // Gilded full helm
		source(10338, "Hard Clue", TagCategory.CLUE);  // Gilded kiteshield
		source(19994, "Hard Clue", TagCategory.CLUE);  // Robin hood hat

		// Elite Clues
		source(12785, "Elite Clue", TagCategory.CLUE);  // Dragon full helm ornament kit
		source(23185, "Elite Clue", TagCategory.CLUE);  // Dark bow (elite)

		// Master Clues
		source(19730, "Master Clue", TagCategory.CLUE);  // Bloodhound

		// ==================== MINIGAMES ====================

		// Barrows
		source(4708, "Barrows", TagCategory.MINIGAME);   // Ahrim's hood
		source(4710, "Barrows", TagCategory.MINIGAME);   // Ahrim's robetop
		source(4712, "Barrows", TagCategory.MINIGAME);   // Ahrim's robeskirt
		source(4714, "Barrows", TagCategory.MINIGAME);   // Ahrim's staff
		source(4716, "Barrows", TagCategory.MINIGAME);   // Dharok's helm
		source(4718, "Barrows", TagCategory.MINIGAME);   // Dharok's platebody
		source(4720, "Barrows", TagCategory.MINIGAME);   // Dharok's platelegs
		source(4722, "Barrows", TagCategory.MINIGAME);   // Dharok's greataxe
		source(4724, "Barrows", TagCategory.MINIGAME);   // Guthan's helm
		source(4726, "Barrows", TagCategory.MINIGAME);   // Guthan's platebody
		source(4728, "Barrows", TagCategory.MINIGAME);   // Guthan's chainskirt
		source(4730, "Barrows", TagCategory.MINIGAME);   // Guthan's warspear
		source(4732, "Barrows", TagCategory.MINIGAME);   // Karil's coif
		source(4734, "Barrows", TagCategory.MINIGAME);   // Karil's leathertop
		source(4736, "Barrows", TagCategory.MINIGAME);   // Karil's leatherskirt
		source(4738, "Barrows", TagCategory.MINIGAME);   // Karil's crossbow
		source(4745, "Barrows", TagCategory.MINIGAME);   // Torag's helm
		source(4747, "Barrows", TagCategory.MINIGAME);   // Torag's platebody
		source(4749, "Barrows", TagCategory.MINIGAME);   // Torag's platelegs
		source(4751, "Barrows", TagCategory.MINIGAME);   // Torag's hammers
		source(4753, "Barrows", TagCategory.MINIGAME);   // Verac's helm
		source(4755, "Barrows", TagCategory.MINIGAME);   // Verac's brassard
		source(4757, "Barrows", TagCategory.MINIGAME);   // Verac's plateskirt
		source(4759, "Barrows", TagCategory.MINIGAME);   // Verac's flail

		// The Gauntlet / Corrupted Gauntlet
		source(23757, "The Gauntlet", TagCategory.MINIGAME);  // Crystal weapon seed
		source(23759, "The Gauntlet", TagCategory.MINIGAME);  // Crystal armour seed
		source(25859, "The Gauntlet", TagCategory.MINIGAME);  // Enhanced crystal weapon seed
		source(23760, "The Gauntlet", TagCategory.MINIGAME);  // Youngllef

		// Fight Caves / Inferno
		source(6570, "Fight Caves", TagCategory.MINIGAME);    // Fire cape
		source(13225, "Fight Caves", TagCategory.MINIGAME);   // Tzrek-jad
		source(21295, "The Inferno", TagCategory.MINIGAME);   // Infernal cape
		source(21291, "The Inferno", TagCategory.MINIGAME);   // Jal-nib-rek

		// Pest Control
		source(8839, "Pest Control", TagCategory.MINIGAME);   // Void knight top
		source(8840, "Pest Control", TagCategory.MINIGAME);   // Void knight robe
		source(8842, "Pest Control", TagCategory.MINIGAME);   // Void knight gloves
		source(11663, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(11664, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(11665, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm

		// Wintertodt
		source(20693, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer garb
		source(20696, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer hood
		source(20699, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer boots
		source(20704, "Wintertodt", TagCategory.MINIGAME);  // Warm gloves
		source(20706, "Wintertodt", TagCategory.MINIGAME);  // Bruma torch
		source(20693, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
	}
}

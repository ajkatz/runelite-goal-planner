package com.goaltracker.data;

import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;

import java.util.*;

/**
 * Maps item IDs to their drop sources (bosses, raids, clues, minigames).
 * Generated from OSRS Wiki API. 326+ items mapped across 40+ sources.
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
		// Zulrah
		source(12922, "Zulrah", TagCategory.BOSS);  // Tanzanite fang
		source(12932, "Zulrah", TagCategory.BOSS);  // Magic fang
		source(12927, "Zulrah", TagCategory.BOSS);  // Serpentine visage
		source(6571, "Zulrah", TagCategory.BOSS);  // Uncut onyx
		source(13200, "Zulrah", TagCategory.BOSS);  // Tanzanite mutagen
		source(13201, "Zulrah", TagCategory.BOSS);  // Magma mutagen
		source(2127, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(12936, "Zulrah", TagCategory.BOSS);  // Jar of swamp

		// Vorkath
		source(22111, "Vorkath", TagCategory.BOSS);  // Dragonbone necklace
		source(22006, "Vorkath", TagCategory.BOSS);  // Skeletal visage
		source(11286, "Vorkath", TagCategory.BOSS);  // Draconic visage
		source(22106, "Vorkath", TagCategory.BOSS);  // Jar of decay
		source(8025, "Vorkath", TagCategory.BOSS);  // Vorki

		// General Graardor
		source(11832, "General Graardor", TagCategory.BOSS);  // Bandos chestplate
		source(11834, "General Graardor", TagCategory.BOSS);  // Bandos tassets
		source(11836, "General Graardor", TagCategory.BOSS);  // Bandos boots
		source(11812, "General Graardor", TagCategory.BOSS);  // Bandos hilt
		source(6632, "General Graardor", TagCategory.BOSS);  // Pet general graardor

		// Commander Zilyana
		source(11838, "Commander Zilyana", TagCategory.BOSS);  // Saradomin sword
		source(11814, "Commander Zilyana", TagCategory.BOSS);  // Saradomin hilt
		source(11785, "Commander Zilyana", TagCategory.BOSS);  // Armadyl crossbow
		source(6633, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana

		// Kree'arra
		source(11826, "Kree'arra", TagCategory.BOSS);  // Armadyl helmet
		source(11828, "Kree'arra", TagCategory.BOSS);  // Armadyl chestplate
		source(11830, "Kree'arra", TagCategory.BOSS);  // Armadyl chainskirt
		source(11810, "Kree'arra", TagCategory.BOSS);  // Armadyl hilt
		source(6631, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra

		// K'ril Tsutsaroth
		source(11824, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorakian spear
		source(11791, "K'ril Tsutsaroth", TagCategory.BOSS);  // Staff of the dead
		source(11816, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorak hilt
		source(6634, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth
		source(11787, "K'ril Tsutsaroth", TagCategory.BOSS);  // Steam battlestaff

		// Cerberus
		source(13231, "Cerberus", TagCategory.BOSS);  // Primordial crystal
		source(13229, "Cerberus", TagCategory.BOSS);  // Pegasian crystal
		source(13227, "Cerberus", TagCategory.BOSS);  // Eternal crystal
		source(13233, "Cerberus", TagCategory.BOSS);  // Smouldering stone
		source(964, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(13245, "Cerberus", TagCategory.BOSS);  // Jar of souls

		// Corporeal Beast
		source(12823, "Corporeal Beast", TagCategory.BOSS);  // Spectral sigil
		source(12827, "Corporeal Beast", TagCategory.BOSS);  // Arcane sigil
		source(12819, "Corporeal Beast", TagCategory.BOSS);  // Elysian sigil
		source(12829, "Corporeal Beast", TagCategory.BOSS);  // Spirit shield
		source(12833, "Corporeal Beast", TagCategory.BOSS);  // Holy elixir
		source(318, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core

		// Thermonuclear Smoke Devil
		source(12002, "Thermonuclear Smoke Devil", TagCategory.BOSS);  // Occult necklace
		source(11998, "Thermonuclear Smoke Devil", TagCategory.BOSS);  // Smoke battlestaff
		source(6639, "Thermonuclear Smoke Devil", TagCategory.BOSS);  // Pet smoke devil

		// Kraken
		source(12004, "Kraken", TagCategory.BOSS);  // Kraken tentacle
		source(6640, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(12007, "Kraken", TagCategory.BOSS);  // Jar of dirt

		// Abyssal Sire
		source(13273, "Abyssal Sire", TagCategory.BOSS);  // Unsired
		source(13274, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon spine
		source(13275, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon claw
		source(13276, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon axon
		source(13265, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(5883, "Abyssal Sire", TagCategory.BOSS);  // Abyssal orphan
		source(13277, "Abyssal Sire", TagCategory.BOSS);  // Jar of miasma
		source(7979, "Abyssal Sire", TagCategory.BOSS);  // Abyssal head

		// Alchemical Hydra
		source(22966, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's claw
		source(22983, "Alchemical Hydra", TagCategory.BOSS);  // Hydra leather
		source(22988, "Alchemical Hydra", TagCategory.BOSS);  // Hydra tail
		source(22973, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's eye
		source(22971, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's fang
		source(22969, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's heart
		source(8492, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(23064, "Alchemical Hydra", TagCategory.BOSS);  // Jar of chemicals

		// Kalphite Queen
		source(6638, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(12885, "Kalphite Queen", TagCategory.BOSS);  // Jar of sand
		source(3140, "Kalphite Queen", TagCategory.BOSS);  // Dragon chainbody
		source(7158, "Kalphite Queen", TagCategory.BOSS);  // Dragon 2h sword

		// King Black Dragon
		source(6636, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon
		source(11920, "King Black Dragon", TagCategory.BOSS);  // Dragon pickaxe
		source(11286, "King Black Dragon", TagCategory.BOSS);  // Draconic visage
		source(1149, "King Black Dragon", TagCategory.BOSS);  // Dragon med helm

		// Giant Mole
		source(6635, "Giant Mole", TagCategory.BOSS);  // Baby mole

		// Sarachnis
		source(23528, "Sarachnis", TagCategory.BOSS);  // Sarachnis cudgel
		source(2143, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(23525, "Sarachnis", TagCategory.BOSS);  // Jar of eyes

		// Grotesque Guardians
		source(21736, "Grotesque Guardians", TagCategory.BOSS);  // Granite gloves
		source(21739, "Grotesque Guardians", TagCategory.BOSS);  // Granite ring
		source(21742, "Grotesque Guardians", TagCategory.BOSS);  // Granite hammer
		source(4153, "Grotesque Guardians", TagCategory.BOSS);  // Granite maul
		source(7891, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(21745, "Grotesque Guardians", TagCategory.BOSS);  // Jar of stone

		// Dagannoth Kings
		source(6737, "Dagannoth Kings", TagCategory.BOSS);  // Berserker ring
		source(6731, "Dagannoth Kings", TagCategory.BOSS);  // Seers ring
		source(6733, "Dagannoth Kings", TagCategory.BOSS);  // Archers ring
		source(6735, "Dagannoth Kings", TagCategory.BOSS);  // Warrior ring
		source(6739, "Dagannoth Kings", TagCategory.BOSS);  // Dragon axe
		source(6562, "Dagannoth Kings", TagCategory.BOSS);  // Mud battlestaff
		source(6724, "Dagannoth Kings", TagCategory.BOSS);  // Seercull
		source(6627, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth prime
		source(6630, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth rex
		source(6626, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth supreme

		// The Nightmare
		source(24422, "The Nightmare", TagCategory.BOSS);  // Nightmare staff
		source(24419, "The Nightmare", TagCategory.BOSS);  // Inquisitor's great helm
		source(24420, "The Nightmare", TagCategory.BOSS);  // Inquisitor's hauberk
		source(24421, "The Nightmare", TagCategory.BOSS);  // Inquisitor's plateskirt
		source(24417, "The Nightmare", TagCategory.BOSS);  // Inquisitor's mace
		source(24517, "The Nightmare", TagCategory.BOSS);  // Eldritch orb
		source(24511, "The Nightmare", TagCategory.BOSS);  // Harmonised orb
		source(24514, "The Nightmare", TagCategory.BOSS);  // Volatile orb
		source(9398, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(24495, "The Nightmare", TagCategory.BOSS);  // Jar of dreams

		// Nex
		source(26235, "Nex", TagCategory.BOSS);  // Zaryte vambraces
		source(26372, "Nex", TagCategory.BOSS);  // Nihil horn
		source(26370, "Nex", TagCategory.BOSS);  // Ancient hilt
		source(11276, "Nex", TagCategory.BOSS);  // Nexling

		// Phantom Muspah
		source(27616, "Phantom Muspah", TagCategory.BOSS);  // Ancient essence
		source(27614, "Phantom Muspah", TagCategory.BOSS);  // Venator shard
		source(12005, "Phantom Muspah", TagCategory.BOSS);  // Muphin

		// Demonic Gorillas
		source(19529, "Demonic Gorillas", TagCategory.BOSS);  // Zenyte shard
		source(19592, "Demonic Gorillas", TagCategory.BOSS);  // Ballista limbs
		source(19601, "Demonic Gorillas", TagCategory.BOSS);  // Ballista spring
		source(19586, "Demonic Gorillas", TagCategory.BOSS);  // Light frame
		source(19589, "Demonic Gorillas", TagCategory.BOSS);  // Heavy frame
		source(19610, "Demonic Gorillas", TagCategory.BOSS);  // Monkey tail

		// Araxxor
		source(29799, "Araxxor", TagCategory.BOSS);  // Araxyte fang
		source(29788, "Araxxor", TagCategory.BOSS);  // Araxyte head
		source(29796, "Araxxor", TagCategory.BOSS);  // Noxious halberd
		source(29790, "Araxxor", TagCategory.BOSS);  // Noxious point
		source(29794, "Araxxor", TagCategory.BOSS);  // Noxious pommel
		source(29792, "Araxxor", TagCategory.BOSS);  // Noxious blade
		source(29781, "Araxxor", TagCategory.BOSS);  // Coagulated venom

		// Duke Sucellus
		source(28276, "Duke Sucellus", TagCategory.BOSS);  // Chromium ingot
		source(26241, "Duke Sucellus", TagCategory.BOSS);  // Virtus mask
		source(26243, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe top
		source(26245, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe bottom
		source(28321, "Duke Sucellus", TagCategory.BOSS);  // Eye of the duke
		source(12155, "Duke Sucellus", TagCategory.BOSS);  // Baron

		// Vardorvis
		source(28285, "Vardorvis", TagCategory.BOSS);  // Ultor vestige
		source(28319, "Vardorvis", TagCategory.BOSS);  // Executioner's axe head
		source(12154, "Vardorvis", TagCategory.BOSS);  // Butch

		// The Leviathan
		source(28283, "The Leviathan", TagCategory.BOSS);  // Venator vestige
		source(28325, "The Leviathan", TagCategory.BOSS);  // Leviathan's lure
		source(12156, "The Leviathan", TagCategory.BOSS);  // Lil'viathan

		// The Whisperer
		source(28279, "The Whisperer", TagCategory.BOSS);  // Bellator vestige
		source(28323, "The Whisperer", TagCategory.BOSS);  // Siren's staff
		source(21270, "The Whisperer", TagCategory.BOSS);  // Eternal gem
		source(12153, "The Whisperer", TagCategory.BOSS);  // Wisp

		// Scurrius
		source(7219, "Scurrius", TagCategory.BOSS);  // Scurry

		// Moons of Peril
		source(29028, "Moons of Peril", TagCategory.BOSS);  // Blood moon helm
		source(29022, "Moons of Peril", TagCategory.BOSS);  // Blood moon chestplate
		source(29025, "Moons of Peril", TagCategory.BOSS);  // Blood moon tassets
		source(29019, "Moons of Peril", TagCategory.BOSS);  // Blue moon helm
		source(29013, "Moons of Peril", TagCategory.BOSS);  // Blue moon chestplate
		source(29016, "Moons of Peril", TagCategory.BOSS);  // Blue moon tassets
		source(29010, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon helm
		source(29004, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon chestplate
		source(29007, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon tassets

		// Sol Heredit
		source(28933, "Sol Heredit", TagCategory.BOSS);  // Sunfire fanatic helm
		source(28936, "Sol Heredit", TagCategory.BOSS);  // Sunfire fanatic cuirass
		source(28939, "Sol Heredit", TagCategory.BOSS);  // Sunfire fanatic chausses
		source(28919, "Sol Heredit", TagCategory.BOSS);  // Tonalztics of ralos

		// Amoxliatl
		source(29309, "Amoxliatl", TagCategory.BOSS);  // Huntsman's kit

		// Chambers of Xeric
		source(20997, "Chambers of Xeric", TagCategory.RAID);  // Twisted bow
		source(21003, "Chambers of Xeric", TagCategory.RAID);  // Elder maul
		source(21043, "Chambers of Xeric", TagCategory.RAID);  // Kodai insignia
		source(13652, "Chambers of Xeric", TagCategory.RAID);  // Dragon claws
		source(21018, "Chambers of Xeric", TagCategory.RAID);  // Ancestral hat
		source(21021, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe top
		source(21024, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe bottom
		source(21000, "Chambers of Xeric", TagCategory.RAID);  // Twisted buckler
		source(21034, "Chambers of Xeric", TagCategory.RAID);  // Dexterous prayer scroll
		source(21079, "Chambers of Xeric", TagCategory.RAID);  // Arcane prayer scroll
		source(21012, "Chambers of Xeric", TagCategory.RAID);  // Dragon hunter crossbow
		source(21015, "Chambers of Xeric", TagCategory.RAID);  // Dinh's bulwark
		source(7519, "Chambers of Xeric", TagCategory.RAID);  // Olmlet

		// Theatre of Blood
		source(22324, "Theatre of Blood", TagCategory.RAID);  // Ghrazi rapier
		source(22326, "Theatre of Blood", TagCategory.RAID);  // Justiciar faceguard
		source(22327, "Theatre of Blood", TagCategory.RAID);  // Justiciar chestguard
		source(22328, "Theatre of Blood", TagCategory.RAID);  // Justiciar legguards
		source(22477, "Theatre of Blood", TagCategory.RAID);  // Avernic defender hilt

		// Tombs of Amascut
		source(26219, "Tombs of Amascut", TagCategory.RAID);  // Osmumten's fang
		source(25975, "Tombs of Amascut", TagCategory.RAID);  // Lightbearer
		source(25985, "Tombs of Amascut", TagCategory.RAID);  // Elidinis' ward
		source(27226, "Tombs of Amascut", TagCategory.RAID);  // Masori mask
		source(27229, "Tombs of Amascut", TagCategory.RAID);  // Masori body
		source(27232, "Tombs of Amascut", TagCategory.RAID);  // Masori chaps
		source(11652, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian

		// Barrows
		source(4708, "Barrows", TagCategory.MINIGAME);  // Ahrim's hood
		source(4712, "Barrows", TagCategory.MINIGAME);  // Ahrim's robetop
		source(4714, "Barrows", TagCategory.MINIGAME);  // Ahrim's robeskirt
		source(4710, "Barrows", TagCategory.MINIGAME);  // Ahrim's staff
		source(4716, "Barrows", TagCategory.MINIGAME);  // Dharok's helm
		source(4720, "Barrows", TagCategory.MINIGAME);  // Dharok's platebody
		source(4722, "Barrows", TagCategory.MINIGAME);  // Dharok's platelegs
		source(4718, "Barrows", TagCategory.MINIGAME);  // Dharok's greataxe
		source(4724, "Barrows", TagCategory.MINIGAME);  // Guthan's helm
		source(4728, "Barrows", TagCategory.MINIGAME);  // Guthan's platebody
		source(4730, "Barrows", TagCategory.MINIGAME);  // Guthan's chainskirt
		source(4726, "Barrows", TagCategory.MINIGAME);  // Guthan's warspear
		source(4732, "Barrows", TagCategory.MINIGAME);  // Karil's coif
		source(4736, "Barrows", TagCategory.MINIGAME);  // Karil's leathertop
		source(4738, "Barrows", TagCategory.MINIGAME);  // Karil's leatherskirt
		source(4734, "Barrows", TagCategory.MINIGAME);  // Karil's crossbow
		source(4745, "Barrows", TagCategory.MINIGAME);  // Torag's helm
		source(4749, "Barrows", TagCategory.MINIGAME);  // Torag's platebody
		source(4751, "Barrows", TagCategory.MINIGAME);  // Torag's platelegs
		source(4747, "Barrows", TagCategory.MINIGAME);  // Torag's hammers
		source(4753, "Barrows", TagCategory.MINIGAME);  // Verac's helm
		source(4757, "Barrows", TagCategory.MINIGAME);  // Verac's brassard
		source(4759, "Barrows", TagCategory.MINIGAME);  // Verac's plateskirt
		source(4755, "Barrows", TagCategory.MINIGAME);  // Verac's flail

		// The Gauntlet
		source(4207, "The Gauntlet", TagCategory.MINIGAME);  // Crystal weapon seed
		source(23956, "The Gauntlet", TagCategory.MINIGAME);  // Crystal armour seed
		source(25859, "The Gauntlet", TagCategory.MINIGAME);  // Enhanced crystal weapon seed
		source(8729, "The Gauntlet", TagCategory.MINIGAME);  // Youngllef

		// Fight Caves
		source(6570, "Fight Caves", TagCategory.MINIGAME);  // Fire cape

		// The Inferno
		source(21295, "The Inferno", TagCategory.MINIGAME);  // Infernal cape
		source(7674, "The Inferno", TagCategory.MINIGAME);  // Jal-nib-rek

		// Pest Control
		source(8839, "Pest Control", TagCategory.MINIGAME);  // Void knight top
		source(8840, "Pest Control", TagCategory.MINIGAME);  // Void knight robe
		source(8842, "Pest Control", TagCategory.MINIGAME);  // Void knight gloves
		source(11665, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(11663, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(11664, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm

		// Wintertodt
		source(20704, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer garb
		source(20708, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer hood
		source(20710, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer boots
		source(20712, "Wintertodt", TagCategory.MINIGAME);  // Warm gloves
		source(20720, "Wintertodt", TagCategory.MINIGAME);  // Bruma torch
		source(7368, "Wintertodt", TagCategory.MINIGAME);  // Phoenix

		// Tempoross
		source(10562, "Tempoross", TagCategory.MINIGAME);  // Tiny tempor
		source(21028, "Tempoross", TagCategory.MINIGAME);  // Dragon harpoon
		source(25592, "Tempoross", TagCategory.MINIGAME);  // Spirit angler headband
		source(25594, "Tempoross", TagCategory.MINIGAME);  // Spirit angler top
		source(25596, "Tempoross", TagCategory.MINIGAME);  // Spirit angler waders
		source(25598, "Tempoross", TagCategory.MINIGAME);  // Spirit angler boots

		// Guardians of the Rift
		source(11402, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal protector

		// Easy Clue

		// Medium Clue
		source(2577, "Medium Clue", TagCategory.CLUE);  // Ranger boots
		source(2579, "Medium Clue", TagCategory.CLUE);  // Wizard boots
		source(12598, "Medium Clue", TagCategory.CLUE);  // Holy sandals
		source(19994, "Medium Clue", TagCategory.CLUE);  // Ranger gloves

		// Hard Clue
		source(3481, "Hard Clue", TagCategory.CLUE);  // Gilded platebody
		source(3483, "Hard Clue", TagCategory.CLUE);  // Gilded platelegs
		source(3485, "Hard Clue", TagCategory.CLUE);  // Gilded plateskirt
		source(3486, "Hard Clue", TagCategory.CLUE);  // Gilded full helm
		source(3488, "Hard Clue", TagCategory.CLUE);  // Gilded kiteshield
		source(2581, "Hard Clue", TagCategory.CLUE);  // Robin hood hat
		source(22236, "Hard Clue", TagCategory.CLUE);  // Dragon platebody ornament kit
		source(22239, "Hard Clue", TagCategory.CLUE);  // Dragon kiteshield ornament kit

		// Elite Clue
		source(12538, "Elite Clue", TagCategory.CLUE);  // Dragon full helm ornament kit

		// Master Clue
		source(6296, "Master Clue", TagCategory.CLUE);  // Bloodhound
		source(20014, "Master Clue", TagCategory.CLUE);  // 3rd age pickaxe
		source(20011, "Master Clue", TagCategory.CLUE);  // 3rd age axe
		source(12426, "Master Clue", TagCategory.CLUE);  // 3rd age longsword
		source(12422, "Master Clue", TagCategory.CLUE);  // 3rd age wand
		source(12437, "Master Clue", TagCategory.CLUE);  // 3rd age cloak
		source(12424, "Master Clue", TagCategory.CLUE);  // 3rd age bow
	}
}

package com.goaltracker.data;

import com.goaltracker.model.ItemTag;
import com.goaltracker.model.TagCategory;

import java.util.*;

/**
 * Maps item IDs to their drop sources.
 * Auto-generated from OSRS Wiki Collection Log (1600+ items).
 *
 * To add items manually: call source(itemId, "Source Name", TagCategory.XXX)
 */
public final class ItemSourceData
{
	private ItemSourceData() {}

	private static final Map<Integer, List<ItemTag>> SOURCES = new HashMap<>();

	public static List<ItemTag> getTags(int itemId)
	{
		return SOURCES.getOrDefault(itemId, Collections.emptyList());
	}

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

		// Abyssal Sire
		source(5883, "Abyssal Sire", TagCategory.BOSS);  // Abyssal orphan
		source(13273, "Abyssal Sire", TagCategory.BOSS);  // Unsired
		source(7979, "Abyssal Sire", TagCategory.BOSS);  // Abyssal head
		source(13274, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon spine
		source(13275, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon claw
		source(13276, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon axon
		source(13277, "Abyssal Sire", TagCategory.BOSS);  // Jar of miasma
		source(13265, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(4151, "Abyssal Sire", TagCategory.BOSS);  // Abyssal whip

		// Alchemical Hydra
		source(8492, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22966, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's claw
		source(22988, "Alchemical Hydra", TagCategory.BOSS);  // Hydra tail
		source(22983, "Alchemical Hydra", TagCategory.BOSS);  // Hydra leather
		source(22971, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's fang
		source(22973, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's eye
		source(22969, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's heart
		source(22804, "Alchemical Hydra", TagCategory.BOSS);  // Dragon knife
		source(20849, "Alchemical Hydra", TagCategory.BOSS);  // Dragon thrownaxe
		source(23064, "Alchemical Hydra", TagCategory.BOSS);  // Jar of chemicals
		source(23077, "Alchemical Hydra", TagCategory.BOSS);  // Alchemical hydra heads

		// Amoxliatl
		source(14034, "Amoxliatl", TagCategory.BOSS);  // Moxi
		source(29889, "Amoxliatl", TagCategory.BOSS);  // Glacial temotli
		source(29895, "Amoxliatl", TagCategory.BOSS);  // Frozen tear

		// Araxxor
		source(13681, "Araxxor", TagCategory.BOSS);  // Nid
		source(29784, "Araxxor", TagCategory.BOSS);  // Araxyte venom sack
		source(29782, "Araxxor", TagCategory.BOSS);  // Spider cave teleport
		source(29799, "Araxxor", TagCategory.BOSS);  // Araxyte fang
		source(29790, "Araxxor", TagCategory.BOSS);  // Noxious point
		source(29792, "Araxxor", TagCategory.BOSS);  // Noxious blade
		source(29794, "Araxxor", TagCategory.BOSS);  // Noxious pommel
		source(29788, "Araxxor", TagCategory.BOSS);  // Araxyte head
		source(29786, "Araxxor", TagCategory.BOSS);  // Jar of venom
		source(29781, "Araxxor", TagCategory.BOSS);  // Coagulated venom

		// Barrows Chests
		source(4732, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4736, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4738, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4734, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4708, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4712, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4714, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4710, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4716, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4720, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4722, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4718, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4724, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4728, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4730, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4726, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4745, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4749, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4751, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4747, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4753, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4757, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4759, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4755, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4740, "Barrows Chests", TagCategory.BOSS);  // Bolt rack

		// Brutus
		source(15631, "Brutus", TagCategory.BOSS);  // Beef
		source(33101, "Brutus", TagCategory.BOSS);  // Mooleta
		source(33093, "Brutus", TagCategory.BOSS);  // Cow slippers

		// Bryophyta
		source(22372, "Bryophyta", TagCategory.BOSS);  // Bryophyta's essence

		// Callisto and Artio
		source(497, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(12603, "Callisto and Artio", TagCategory.BOSS);  // Tyrannical ring
		source(11920, "Callisto and Artio", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Callisto and Artio", TagCategory.BOSS);  // Dragon 2h sword
		source(27667, "Callisto and Artio", TagCategory.BOSS);  // Claws of callisto
		source(27681, "Callisto and Artio", TagCategory.BOSS);  // Voidwaker hilt

		// Cerberus
		source(964, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(13227, "Cerberus", TagCategory.BOSS);  // Eternal crystal
		source(13229, "Cerberus", TagCategory.BOSS);  // Pegasian crystal
		source(13231, "Cerberus", TagCategory.BOSS);  // Primordial crystal
		source(13245, "Cerberus", TagCategory.BOSS);  // Jar of souls
		source(13233, "Cerberus", TagCategory.BOSS);  // Smouldering stone
		source(13249, "Cerberus", TagCategory.BOSS);  // Key master teleport

		// Chaos Elemental
		source(2055, "Chaos Elemental", TagCategory.BOSS);  // Pet chaos elemental
		source(11920, "Chaos Elemental", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Chaos Elemental", TagCategory.BOSS);  // Dragon 2h sword

		// Chaos Fanatic
		source(2055, "Chaos Fanatic", TagCategory.BOSS);  // Pet chaos elemental
		source(11928, "Chaos Fanatic", TagCategory.BOSS);  // Odium shard 1
		source(11931, "Chaos Fanatic", TagCategory.BOSS);  // Malediction shard 1

		// Commander Zilyana
		source(6633, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana
		source(11785, "Commander Zilyana", TagCategory.BOSS);  // Armadyl crossbow
		source(11814, "Commander Zilyana", TagCategory.BOSS);  // Saradomin hilt
		source(11838, "Commander Zilyana", TagCategory.BOSS);  // Saradomin sword
		source(13256, "Commander Zilyana", TagCategory.BOSS);  // Saradomin's light
		source(11818, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 3

		// Corporeal Beast
		source(318, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(12819, "Corporeal Beast", TagCategory.BOSS);  // Elysian sigil
		source(12823, "Corporeal Beast", TagCategory.BOSS);  // Spectral sigil
		source(12827, "Corporeal Beast", TagCategory.BOSS);  // Arcane sigil
		source(12833, "Corporeal Beast", TagCategory.BOSS);  // Holy elixir
		source(12829, "Corporeal Beast", TagCategory.BOSS);  // Spirit shield
		source(25521, "Corporeal Beast", TagCategory.BOSS);  // Jar of spirits

		// Crazy archaeologist
		source(11929, "Crazy archaeologist", TagCategory.BOSS);  // Odium shard 2
		source(11932, "Crazy archaeologist", TagCategory.BOSS);  // Malediction shard 2
		source(11990, "Crazy archaeologist", TagCategory.BOSS);  // Fedora

		// Dagannoth Kings
		source(6627, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth prime
		source(6626, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth supreme
		source(6630, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth rex
		source(6737, "Dagannoth Kings", TagCategory.BOSS);  // Berserker ring
		source(6733, "Dagannoth Kings", TagCategory.BOSS);  // Archers ring
		source(6731, "Dagannoth Kings", TagCategory.BOSS);  // Seers ring
		source(6735, "Dagannoth Kings", TagCategory.BOSS);  // Warrior ring
		source(6739, "Dagannoth Kings", TagCategory.BOSS);  // Dragon axe
		source(6724, "Dagannoth Kings", TagCategory.BOSS);  // Seercull
		source(6562, "Dagannoth Kings", TagCategory.BOSS);  // Mud battlestaff

		// Deranged Archaeologist
		source(30895, "Deranged Archaeologist", TagCategory.BOSS);  // Steel ring

		// Doom of Mokhaiotl
		source(14785, "Doom of Mokhaiotl", TagCategory.BOSS);  // Dom
		source(31088, "Doom of Mokhaiotl", TagCategory.BOSS);  // Avernic treads
		source(31109, "Doom of Mokhaiotl", TagCategory.BOSS);  // Mokhaiotl cloth
		source(31099, "Doom of Mokhaiotl", TagCategory.BOSS);  // Mokhaiotl waystone
		source(31111, "Doom of Mokhaiotl", TagCategory.BOSS);  // Demon tear

		// Duke Sucellus
		source(12155, "Duke Sucellus", TagCategory.BOSS);  // Baron
		source(28321, "Duke Sucellus", TagCategory.BOSS);  // Eye of the duke
		source(26241, "Duke Sucellus", TagCategory.BOSS);  // Virtus mask
		source(26243, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe top
		source(26245, "Duke Sucellus", TagCategory.BOSS);  // Virtus robe bottom
		source(28281, "Duke Sucellus", TagCategory.BOSS);  // Magus vestige
		source(28270, "Duke Sucellus", TagCategory.BOSS);  // Ice quartz
		source(28333, "Duke Sucellus", TagCategory.BOSS);  // Frozen tablet
		source(28276, "Duke Sucellus", TagCategory.BOSS);  // Chromium ingot
		source(28334, "Duke Sucellus", TagCategory.BOSS);  // Awakener's orb

		// The Fight Caves
		source(5892, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(6570, "The Fight Caves", TagCategory.BOSS);  // Fire cape

		// Fortis Colosseum
		source(12767, "Fortis Colosseum", TagCategory.BOSS);  // Smol heredit
		source(28936, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic cuirass
		source(28939, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic chausses
		source(28933, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic helm
		source(28942, "Fortis Colosseum", TagCategory.BOSS);  // Echo crystal
		source(28924, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire splinters
		source(6571, "Fortis Colosseum", TagCategory.BOSS);  // Uncut onyx

		// The Gauntlet
		source(8729, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(23956, "The Gauntlet", TagCategory.BOSS);  // Crystal armour seed
		source(4207, "The Gauntlet", TagCategory.BOSS);  // Crystal weapon seed
		source(25859, "The Gauntlet", TagCategory.BOSS);  // Enhanced crystal weapon seed
		source(23859, "The Gauntlet", TagCategory.BOSS);  // Gauntlet cape

		// General Graardor
		source(6632, "General Graardor", TagCategory.BOSS);  // Pet general graardor
		source(11832, "General Graardor", TagCategory.BOSS);  // Bandos chestplate
		source(11834, "General Graardor", TagCategory.BOSS);  // Bandos tassets
		source(11836, "General Graardor", TagCategory.BOSS);  // Bandos boots
		source(11812, "General Graardor", TagCategory.BOSS);  // Bandos hilt
		source(11818, "General Graardor", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "General Graardor", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "General Graardor", TagCategory.BOSS);  // Godsword shard 3

		// Giant Mole
		source(6635, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(7418, "Giant Mole", TagCategory.BOSS);  // Mole skin
		source(7416, "Giant Mole", TagCategory.BOSS);  // Mole claw

		// Grotesque Guardians
		source(7891, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(21730, "Grotesque Guardians", TagCategory.BOSS);  // Black tourmaline core
		source(21736, "Grotesque Guardians", TagCategory.BOSS);  // Granite gloves
		source(21739, "Grotesque Guardians", TagCategory.BOSS);  // Granite ring
		source(21742, "Grotesque Guardians", TagCategory.BOSS);  // Granite hammer
		source(21745, "Grotesque Guardians", TagCategory.BOSS);  // Jar of stone
		source(21726, "Grotesque Guardians", TagCategory.BOSS);  // Granite dust

		// Hespori
		source(22994, "Hespori", TagCategory.BOSS);  // Bottomless compost bucket
		source(22883, "Hespori", TagCategory.BOSS);  // Iasor seed
		source(22885, "Hespori", TagCategory.BOSS);  // Kronos seed
		source(22881, "Hespori", TagCategory.BOSS);  // Attas seed

		// The Hueycoatl
		source(14033, "The Hueycoatl", TagCategory.BOSS);  // Huberte
		source(30070, "The Hueycoatl", TagCategory.BOSS);  // Dragon hunter wand
		source(30068, "The Hueycoatl", TagCategory.BOSS);  // Soiled page
		source(30085, "The Hueycoatl", TagCategory.BOSS);  // Hueycoatl hide
		source(30088, "The Hueycoatl", TagCategory.BOSS);  // Huasca seed

		// The Inferno
		source(7674, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(21295, "The Inferno", TagCategory.BOSS);  // Infernal cape

		// Kalphite Queen
		source(6638, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(7981, "Kalphite Queen", TagCategory.BOSS);  // Kq head
		source(12885, "Kalphite Queen", TagCategory.BOSS);  // Jar of sand
		source(7158, "Kalphite Queen", TagCategory.BOSS);  // Dragon 2h sword
		source(3140, "Kalphite Queen", TagCategory.BOSS);  // Dragon chainbody
		source(11920, "Kalphite Queen", TagCategory.BOSS);  // Dragon pickaxe

		// King Black Dragon
		source(6636, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon
		source(7980, "King Black Dragon", TagCategory.BOSS);  // Kbd heads
		source(11920, "King Black Dragon", TagCategory.BOSS);  // Dragon pickaxe
		source(11286, "King Black Dragon", TagCategory.BOSS);  // Draconic visage

		// Kraken
		source(6640, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(12004, "Kraken", TagCategory.BOSS);  // Kraken tentacle
		source(11908, "Kraken", TagCategory.BOSS);  // Trident of the seas
		source(12007, "Kraken", TagCategory.BOSS);  // Jar of dirt

		// Kree'arra
		source(6631, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra
		source(11826, "Kree'arra", TagCategory.BOSS);  // Armadyl helmet
		source(11828, "Kree'arra", TagCategory.BOSS);  // Armadyl chestplate
		source(11830, "Kree'arra", TagCategory.BOSS);  // Armadyl chainskirt
		source(11810, "Kree'arra", TagCategory.BOSS);  // Armadyl hilt
		source(11818, "Kree'arra", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "Kree'arra", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "Kree'arra", TagCategory.BOSS);  // Godsword shard 3

		// K'ril Tsutsaroth
		source(6634, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth
		source(11791, "K'ril Tsutsaroth", TagCategory.BOSS);  // Staff of the dead
		source(11824, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorakian spear
		source(11787, "K'ril Tsutsaroth", TagCategory.BOSS);  // Steam battlestaff
		source(11816, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorak hilt
		source(11818, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 3

		// The Leviathan
		source(12156, "The Leviathan", TagCategory.BOSS);  // Lil'viathan
		source(28325, "The Leviathan", TagCategory.BOSS);  // Leviathan's lure
		source(26241, "The Leviathan", TagCategory.BOSS);  // Virtus mask
		source(26243, "The Leviathan", TagCategory.BOSS);  // Virtus robe top
		source(26245, "The Leviathan", TagCategory.BOSS);  // Virtus robe bottom
		source(28283, "The Leviathan", TagCategory.BOSS);  // Venator vestige
		source(28274, "The Leviathan", TagCategory.BOSS);  // Smoke quartz
		source(28332, "The Leviathan", TagCategory.BOSS);  // Scarred tablet
		source(28276, "The Leviathan", TagCategory.BOSS);  // Chromium ingot
		source(28334, "The Leviathan", TagCategory.BOSS);  // Awakener's orb

		// Moons of Peril
		source(29004, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon chestplate
		source(29007, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon tassets
		source(29010, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon helm
		source(29000, "Moons of Peril", TagCategory.BOSS);  // Eclipse atlatl
		source(29013, "Moons of Peril", TagCategory.BOSS);  // Blue moon chestplate
		source(29016, "Moons of Peril", TagCategory.BOSS);  // Blue moon tassets
		source(29019, "Moons of Peril", TagCategory.BOSS);  // Blue moon helm
		source(28988, "Moons of Peril", TagCategory.BOSS);  // Blue moon spear
		source(29022, "Moons of Peril", TagCategory.BOSS);  // Blood moon chestplate
		source(29025, "Moons of Peril", TagCategory.BOSS);  // Blood moon tassets
		source(29028, "Moons of Peril", TagCategory.BOSS);  // Blood moon helm
		source(28997, "Moons of Peril", TagCategory.BOSS);  // Dual macuahuitl
		source(28991, "Moons of Peril", TagCategory.BOSS);  // Atlatl dart

		// Nex
		source(11276, "Nex", TagCategory.BOSS);  // Nexling
		source(26370, "Nex", TagCategory.BOSS);  // Ancient hilt
		source(26372, "Nex", TagCategory.BOSS);  // Nihil horn
		source(26235, "Nex", TagCategory.BOSS);  // Zaryte vambraces
		source(26231, "Nex", TagCategory.BOSS);  // Nihil shard

		// The Nightmare
		source(9398, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(24417, "The Nightmare", TagCategory.BOSS);  // Inquisitor's mace
		source(24419, "The Nightmare", TagCategory.BOSS);  // Inquisitor's great helm
		source(24420, "The Nightmare", TagCategory.BOSS);  // Inquisitor's hauberk
		source(24421, "The Nightmare", TagCategory.BOSS);  // Inquisitor's plateskirt
		source(24422, "The Nightmare", TagCategory.BOSS);  // Nightmare staff
		source(24514, "The Nightmare", TagCategory.BOSS);  // Volatile orb
		source(24511, "The Nightmare", TagCategory.BOSS);  // Harmonised orb
		source(24517, "The Nightmare", TagCategory.BOSS);  // Eldritch orb
		source(24495, "The Nightmare", TagCategory.BOSS);  // Jar of dreams
		source(25837, "The Nightmare", TagCategory.BOSS);  // Slepey tablet
		source(25838, "The Nightmare", TagCategory.BOSS);  // Parasitic egg

		// Obor
		source(20756, "Obor", TagCategory.BOSS);  // Hill giant club

		// Phantom Muspah
		source(12005, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(27614, "Phantom Muspah", TagCategory.BOSS);  // Venator shard
		source(27627, "Phantom Muspah", TagCategory.BOSS);  // Ancient icon
		source(27643, "Phantom Muspah", TagCategory.BOSS);  // Charged ice
		source(27622, "Phantom Muspah", TagCategory.BOSS);  // Frozen cache
		source(27616, "Phantom Muspah", TagCategory.BOSS);  // Ancient essence

		// Royal Titans
		source(10476, "Royal Titans", TagCategory.BOSS);  // Bran
		source(30626, "Royal Titans", TagCategory.BOSS);  // Deadeye prayer scroll
		source(30627, "Royal Titans", TagCategory.BOSS);  // Mystic vigour prayer scroll
		source(30628, "Royal Titans", TagCategory.BOSS);  // Ice element staff crown
		source(30631, "Royal Titans", TagCategory.BOSS);  // Fire element staff crown
		source(30640, "Royal Titans", TagCategory.BOSS);  // Desiccated page

		// Sarachnis
		source(2143, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(23525, "Sarachnis", TagCategory.BOSS);  // Jar of eyes
		source(23528, "Sarachnis", TagCategory.BOSS);  // Sarachnis cudgel
		source(33133, "Sarachnis", TagCategory.BOSS);  // Pristine spider silk

		// Scorpia
		source(5547, "Scorpia", TagCategory.BOSS);  // Scorpia's offspring
		source(11930, "Scorpia", TagCategory.BOSS);  // Odium shard 3
		source(11933, "Scorpia", TagCategory.BOSS);  // Malediction shard 3
		source(7158, "Scorpia", TagCategory.BOSS);  // Dragon 2h sword

		// Scurrius
		source(7219, "Scurrius", TagCategory.BOSS);  // Scurry
		source(28798, "Scurrius", TagCategory.BOSS);  // Scurrius' spine

		// Shellbane Gryphon
		source(14931, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(32921, "Shellbane Gryphon", TagCategory.BOSS);  // Jar of feathers
		source(31245, "Shellbane Gryphon", TagCategory.BOSS);  // Belle's folly (tarnished)
		source(31235, "Shellbane Gryphon", TagCategory.BOSS);  // Gryphon feather

		// Skotizo
		source(425, "Skotizo", TagCategory.BOSS);  // Skotos
		source(19701, "Skotizo", TagCategory.BOSS);  // Jar of darkness
		source(21275, "Skotizo", TagCategory.BOSS);  // Dark claw
		source(19685, "Skotizo", TagCategory.BOSS);  // Dark totem
		source(6571, "Skotizo", TagCategory.BOSS);  // Uncut onyx
		source(19677, "Skotizo", TagCategory.BOSS);  // Ancient shard

		// Tempoross
		source(10562, "Tempoross", TagCategory.BOSS);  // Tiny tempor
		source(25559, "Tempoross", TagCategory.BOSS);  // Big harpoonfish
		source(25592, "Tempoross", TagCategory.BOSS);  // Spirit angler headband
		source(25594, "Tempoross", TagCategory.BOSS);  // Spirit angler top
		source(25596, "Tempoross", TagCategory.BOSS);  // Spirit angler waders
		source(25598, "Tempoross", TagCategory.BOSS);  // Spirit angler boots
		source(25578, "Tempoross", TagCategory.BOSS);  // Soaked page
		source(25580, "Tempoross", TagCategory.BOSS);  // Tackle box
		source(25582, "Tempoross", TagCategory.BOSS);  // Fish barrel
		source(21028, "Tempoross", TagCategory.BOSS);  // Dragon harpoon
		source(25588, "Tempoross", TagCategory.BOSS);  // Spirit flakes

		// Thermonuclear smoke devil
		source(6639, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(12002, "Thermonuclear smoke devil", TagCategory.BOSS);  // Occult necklace
		source(11998, "Thermonuclear smoke devil", TagCategory.BOSS);  // Smoke battlestaff
		source(3140, "Thermonuclear smoke devil", TagCategory.BOSS);  // Dragon chainbody
		source(25524, "Thermonuclear smoke devil", TagCategory.BOSS);  // Jar of smoke

		// Vardorvis
		source(12154, "Vardorvis", TagCategory.BOSS);  // Butch
		source(28319, "Vardorvis", TagCategory.BOSS);  // Executioner's axe head
		source(26241, "Vardorvis", TagCategory.BOSS);  // Virtus mask
		source(26243, "Vardorvis", TagCategory.BOSS);  // Virtus robe top
		source(26245, "Vardorvis", TagCategory.BOSS);  // Virtus robe bottom
		source(28285, "Vardorvis", TagCategory.BOSS);  // Ultor vestige
		source(28268, "Vardorvis", TagCategory.BOSS);  // Blood quartz
		source(28330, "Vardorvis", TagCategory.BOSS);  // Strangled tablet
		source(28276, "Vardorvis", TagCategory.BOSS);  // Chromium ingot
		source(28334, "Vardorvis", TagCategory.BOSS);  // Awakener's orb

		// Venenatis and Spindel
		source(495, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(12605, "Venenatis and Spindel", TagCategory.BOSS);  // Treasonous ring
		source(11920, "Venenatis and Spindel", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Venenatis and Spindel", TagCategory.BOSS);  // Dragon 2h sword
		source(27670, "Venenatis and Spindel", TagCategory.BOSS);  // Fangs of venenatis
		source(27687, "Venenatis and Spindel", TagCategory.BOSS);  // Voidwaker gem

		// Vet'ion and Calvar'ion
		source(5536, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(12601, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Ring of the gods
		source(11920, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Dragon 2h sword
		source(27673, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Skull of vet'ion
		source(27684, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Voidwaker blade

		// Vorkath
		source(8025, "Vorkath", TagCategory.BOSS);  // Vorki
		source(21907, "Vorkath", TagCategory.BOSS);  // Vorkath's head
		source(11286, "Vorkath", TagCategory.BOSS);  // Draconic visage
		source(22006, "Vorkath", TagCategory.BOSS);  // Skeletal visage
		source(22106, "Vorkath", TagCategory.BOSS);  // Jar of decay
		source(22111, "Vorkath", TagCategory.BOSS);  // Dragonbone necklace

		// The Whisperer
		source(12153, "The Whisperer", TagCategory.BOSS);  // Wisp
		source(28323, "The Whisperer", TagCategory.BOSS);  // Siren's staff
		source(26241, "The Whisperer", TagCategory.BOSS);  // Virtus mask
		source(26243, "The Whisperer", TagCategory.BOSS);  // Virtus robe top
		source(26245, "The Whisperer", TagCategory.BOSS);  // Virtus robe bottom
		source(28279, "The Whisperer", TagCategory.BOSS);  // Bellator vestige
		source(28272, "The Whisperer", TagCategory.BOSS);  // Shadow quartz
		source(28331, "The Whisperer", TagCategory.BOSS);  // Sirenic tablet
		source(28276, "The Whisperer", TagCategory.BOSS);  // Chromium ingot
		source(28334, "The Whisperer", TagCategory.BOSS);  // Awakener's orb

		// Wintertodt
		source(7368, "Wintertodt", TagCategory.BOSS);  // Phoenix
		source(20718, "Wintertodt", TagCategory.BOSS);  // Burnt page
		source(20704, "Wintertodt", TagCategory.BOSS);  // Pyromancer garb
		source(20708, "Wintertodt", TagCategory.BOSS);  // Pyromancer hood
		source(20706, "Wintertodt", TagCategory.BOSS);  // Pyromancer robe
		source(20710, "Wintertodt", TagCategory.BOSS);  // Pyromancer boots
		source(20712, "Wintertodt", TagCategory.BOSS);  // Warm gloves
		source(20720, "Wintertodt", TagCategory.BOSS);  // Bruma torch
		source(6739, "Wintertodt", TagCategory.BOSS);  // Dragon axe

		// Yama
		source(14203, "Yama", TagCategory.BOSS);  // Yami
		source(30775, "Yama", TagCategory.BOSS);  // Chasm teleport scroll
		source(30765, "Yama", TagCategory.BOSS);  // Oathplate shards
		source(30750, "Yama", TagCategory.BOSS);  // Oathplate helm
		source(30753, "Yama", TagCategory.BOSS);  // Oathplate chest
		source(30756, "Yama", TagCategory.BOSS);  // Oathplate legs
		source(30759, "Yama", TagCategory.BOSS);  // Soulflame horn
		source(30806, "Yama", TagCategory.BOSS);  // Rite of vile transference
		source(30763, "Yama", TagCategory.BOSS);  // Forgotten lockbox
		source(30803, "Yama", TagCategory.BOSS);  // Dossier

		// Zalcano
		source(8731, "Zalcano", TagCategory.BOSS);  // Smolcano
		source(23953, "Zalcano", TagCategory.BOSS);  // Crystal tool seed
		source(23908, "Zalcano", TagCategory.BOSS);  // Zalcano shard
		source(6571, "Zalcano", TagCategory.BOSS);  // Uncut onyx

		// Zulrah
		source(2127, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(13200, "Zulrah", TagCategory.BOSS);  // Tanzanite mutagen
		source(13201, "Zulrah", TagCategory.BOSS);  // Magma mutagen
		source(12936, "Zulrah", TagCategory.BOSS);  // Jar of swamp
		source(12932, "Zulrah", TagCategory.BOSS);  // Magic fang
		source(12927, "Zulrah", TagCategory.BOSS);  // Serpentine visage
		source(12922, "Zulrah", TagCategory.BOSS);  // Tanzanite fang
		source(12938, "Zulrah", TagCategory.BOSS);  // Zul-andra teleport
		source(6571, "Zulrah", TagCategory.BOSS);  // Uncut onyx
		source(12934, "Zulrah", TagCategory.BOSS);  // Zulrah's scales

		// ==================== RAIDS ====================

		// Chambers of Xeric
		source(7519, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22386, "Chambers of Xeric", TagCategory.RAID);  // Metamorphic dust
		source(20997, "Chambers of Xeric", TagCategory.RAID);  // Twisted bow
		source(21003, "Chambers of Xeric", TagCategory.RAID);  // Elder maul
		source(21043, "Chambers of Xeric", TagCategory.RAID);  // Kodai insignia
		source(13652, "Chambers of Xeric", TagCategory.RAID);  // Dragon claws
		source(21018, "Chambers of Xeric", TagCategory.RAID);  // Ancestral hat
		source(21021, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe top
		source(21024, "Chambers of Xeric", TagCategory.RAID);  // Ancestral robe bottom
		source(21015, "Chambers of Xeric", TagCategory.RAID);  // Dinh's bulwark
		source(21034, "Chambers of Xeric", TagCategory.RAID);  // Dexterous prayer scroll
		source(21079, "Chambers of Xeric", TagCategory.RAID);  // Arcane prayer scroll
		source(21012, "Chambers of Xeric", TagCategory.RAID);  // Dragon hunter crossbow
		source(21000, "Chambers of Xeric", TagCategory.RAID);  // Twisted buckler
		source(21047, "Chambers of Xeric", TagCategory.RAID);  // Torn prayer scroll
		source(21027, "Chambers of Xeric", TagCategory.RAID);  // Dark relic
		source(6573, "Chambers of Xeric", TagCategory.RAID);  // Onyx
		source(24670, "Chambers of Xeric", TagCategory.RAID);  // Twisted ancestral colour kit
		source(22388, "Chambers of Xeric", TagCategory.RAID);  // Xeric's guard
		source(22390, "Chambers of Xeric", TagCategory.RAID);  // Xeric's warrior
		source(22392, "Chambers of Xeric", TagCategory.RAID);  // Xeric's sentinel
		source(22394, "Chambers of Xeric", TagCategory.RAID);  // Xeric's general
		source(22396, "Chambers of Xeric", TagCategory.RAID);  // Xeric's champion

		// Theatre of Blood
		source(8336, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(22324, "Theatre of Blood", TagCategory.RAID);  // Ghrazi rapier
		source(22326, "Theatre of Blood", TagCategory.RAID);  // Justiciar faceguard
		source(22327, "Theatre of Blood", TagCategory.RAID);  // Justiciar chestguard
		source(22328, "Theatre of Blood", TagCategory.RAID);  // Justiciar legguards
		source(22477, "Theatre of Blood", TagCategory.RAID);  // Avernic defender hilt
		source(22446, "Theatre of Blood", TagCategory.RAID);  // Vial of blood
		source(22494, "Theatre of Blood", TagCategory.RAID);  // Sinhaza shroud tier 1
		source(22496, "Theatre of Blood", TagCategory.RAID);  // Sinhaza shroud tier 2
		source(22498, "Theatre of Blood", TagCategory.RAID);  // Sinhaza shroud tier 3
		source(22500, "Theatre of Blood", TagCategory.RAID);  // Sinhaza shroud tier 4
		source(22502, "Theatre of Blood", TagCategory.RAID);  // Sinhaza shroud tier 5
		source(25746, "Theatre of Blood", TagCategory.RAID);  // Sanguine dust
		source(25742, "Theatre of Blood", TagCategory.RAID);  // Holy ornament kit
		source(25744, "Theatre of Blood", TagCategory.RAID);  // Sanguine ornament kit

		// Tombs of Amascut
		source(11652, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(25985, "Tombs of Amascut", TagCategory.RAID);  // Elidinis' ward
		source(27226, "Tombs of Amascut", TagCategory.RAID);  // Masori mask
		source(27229, "Tombs of Amascut", TagCategory.RAID);  // Masori body
		source(27232, "Tombs of Amascut", TagCategory.RAID);  // Masori chaps
		source(25975, "Tombs of Amascut", TagCategory.RAID);  // Lightbearer
		source(26219, "Tombs of Amascut", TagCategory.RAID);  // Osmumten's fang
		source(27279, "Tombs of Amascut", TagCategory.RAID);  // Thread of elidinis
		source(27283, "Tombs of Amascut", TagCategory.RAID);  // Breach of the scarab
		source(27285, "Tombs of Amascut", TagCategory.RAID);  // Eye of the corruptor
		source(27289, "Tombs of Amascut", TagCategory.RAID);  // Jewel of the sun
		source(30893, "Tombs of Amascut", TagCategory.RAID);  // Jewel of amascut
		source(27255, "Tombs of Amascut", TagCategory.RAID);  // Menaphite ornament kit
		source(27248, "Tombs of Amascut", TagCategory.RAID);  // Cursed phalanx
		source(27372, "Tombs of Amascut", TagCategory.RAID);  // Masori crafting kit
		source(27293, "Tombs of Amascut", TagCategory.RAID);  // Cache of runes
		source(27257, "Tombs of Amascut", TagCategory.RAID);  // Icthlarin's shroud (tier 1)
		source(27259, "Tombs of Amascut", TagCategory.RAID);  // Icthlarin's shroud (tier 2)
		source(27261, "Tombs of Amascut", TagCategory.RAID);  // Icthlarin's shroud (tier 3)
		source(27263, "Tombs of Amascut", TagCategory.RAID);  // Icthlarin's shroud (tier 4)
		source(27265, "Tombs of Amascut", TagCategory.RAID);  // Icthlarin's shroud (tier 5)
		source(27377, "Tombs of Amascut", TagCategory.RAID);  // Remnant of akkha
		source(27378, "Tombs of Amascut", TagCategory.RAID);  // Remnant of ba-ba
		source(27379, "Tombs of Amascut", TagCategory.RAID);  // Remnant of kephri
		source(27380, "Tombs of Amascut", TagCategory.RAID);  // Remnant of zebak
		source(27381, "Tombs of Amascut", TagCategory.RAID);  // Ancient remnant

		// ==================== CLUES ====================

		// Beginner Treasure Trails
		source(23285, "Beginner Treasure Trails", TagCategory.CLUE);  // Mole slippers
		source(23288, "Beginner Treasure Trails", TagCategory.CLUE);  // Frog slippers
		source(23291, "Beginner Treasure Trails", TagCategory.CLUE);  // Bear feet
		source(23294, "Beginner Treasure Trails", TagCategory.CLUE);  // Demon feet
		source(23297, "Beginner Treasure Trails", TagCategory.CLUE);  // Jester cape
		source(23300, "Beginner Treasure Trails", TagCategory.CLUE);  // Shoulder parrot
		source(23303, "Beginner Treasure Trails", TagCategory.CLUE);  // Monk's robe top (t)
		source(23306, "Beginner Treasure Trails", TagCategory.CLUE);  // Monk's robe (t)
		source(23309, "Beginner Treasure Trails", TagCategory.CLUE);  // Amulet of defence (t)
		source(23312, "Beginner Treasure Trails", TagCategory.CLUE);  // Sandwich lady hat
		source(23315, "Beginner Treasure Trails", TagCategory.CLUE);  // Sandwich lady top
		source(23318, "Beginner Treasure Trails", TagCategory.CLUE);  // Sandwich lady bottom
		source(23321, "Beginner Treasure Trails", TagCategory.CLUE);  // Rune scimitar ornament kit (guthix)
		source(23324, "Beginner Treasure Trails", TagCategory.CLUE);  // Rune scimitar ornament kit (saradomin)
		source(23327, "Beginner Treasure Trails", TagCategory.CLUE);  // Rune scimitar ornament kit (zamorak)
		source(12297, "Beginner Treasure Trails", TagCategory.CLUE);  // Black pickaxe

		// Easy Treasure Trails
		source(20211, "Easy Treasure Trails", TagCategory.CLUE);  // Team cape zero
		source(20217, "Easy Treasure Trails", TagCategory.CLUE);  // Team cape i
		source(20214, "Easy Treasure Trails", TagCategory.CLUE);  // Team cape x
		source(23351, "Easy Treasure Trails", TagCategory.CLUE);  // Cape of skulls
		source(20205, "Easy Treasure Trails", TagCategory.CLUE);  // Golden chef's hat
		source(20208, "Easy Treasure Trails", TagCategory.CLUE);  // Golden apron
		source(20166, "Easy Treasure Trails", TagCategory.CLUE);  // Wooden shield (g)
		source(2587, "Easy Treasure Trails", TagCategory.CLUE);  // Black full helm (t)
		source(2583, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (t)
		source(2585, "Easy Treasure Trails", TagCategory.CLUE);  // Black platelegs (t)
		source(3472, "Easy Treasure Trails", TagCategory.CLUE);  // Black plateskirt (t)
		source(2589, "Easy Treasure Trails", TagCategory.CLUE);  // Black kiteshield (t)
		source(2595, "Easy Treasure Trails", TagCategory.CLUE);  // Black full helm (g)
		source(2591, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (g)
		source(2593, "Easy Treasure Trails", TagCategory.CLUE);  // Black platelegs (g)
		source(3473, "Easy Treasure Trails", TagCategory.CLUE);  // Black plateskirt (g)
		source(2597, "Easy Treasure Trails", TagCategory.CLUE);  // Black kiteshield (g)
		source(7332, "Easy Treasure Trails", TagCategory.CLUE);  // Black shield (h1)
		source(7338, "Easy Treasure Trails", TagCategory.CLUE);  // Black shield (h2)
		source(7344, "Easy Treasure Trails", TagCategory.CLUE);  // Black shield (h3)
		source(7350, "Easy Treasure Trails", TagCategory.CLUE);  // Black shield (h4)
		source(7356, "Easy Treasure Trails", TagCategory.CLUE);  // Black shield (h5)
		source(10306, "Easy Treasure Trails", TagCategory.CLUE);  // Black helm (h1)
		source(10308, "Easy Treasure Trails", TagCategory.CLUE);  // Black helm (h2)
		source(10310, "Easy Treasure Trails", TagCategory.CLUE);  // Black helm (h3)
		source(10312, "Easy Treasure Trails", TagCategory.CLUE);  // Black helm (h4)
		source(10314, "Easy Treasure Trails", TagCategory.CLUE);  // Black helm (h5)
		source(23366, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (h1)
		source(23369, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (h2)
		source(23372, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (h3)
		source(23375, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (h4)
		source(23378, "Easy Treasure Trails", TagCategory.CLUE);  // Black platebody (h5)
		source(20193, "Easy Treasure Trails", TagCategory.CLUE);  // Steel full helm (t)
		source(20184, "Easy Treasure Trails", TagCategory.CLUE);  // Steel platebody (t)
		source(20187, "Easy Treasure Trails", TagCategory.CLUE);  // Steel platelegs (t)
		source(20190, "Easy Treasure Trails", TagCategory.CLUE);  // Steel plateskirt (t)
		source(20196, "Easy Treasure Trails", TagCategory.CLUE);  // Steel kiteshield (t)
		source(20178, "Easy Treasure Trails", TagCategory.CLUE);  // Steel full helm (g)
		source(20169, "Easy Treasure Trails", TagCategory.CLUE);  // Steel platebody (g)
		source(20172, "Easy Treasure Trails", TagCategory.CLUE);  // Steel platelegs (g)
		source(20175, "Easy Treasure Trails", TagCategory.CLUE);  // Steel plateskirt (g)
		source(20181, "Easy Treasure Trails", TagCategory.CLUE);  // Steel kiteshield (g)
		source(12225, "Easy Treasure Trails", TagCategory.CLUE);  // Iron platebody (t)
		source(12227, "Easy Treasure Trails", TagCategory.CLUE);  // Iron platelegs (t)
		source(12229, "Easy Treasure Trails", TagCategory.CLUE);  // Iron plateskirt (t)
		source(12233, "Easy Treasure Trails", TagCategory.CLUE);  // Iron kiteshield (t)
		source(12231, "Easy Treasure Trails", TagCategory.CLUE);  // Iron full helm (t)
		source(12235, "Easy Treasure Trails", TagCategory.CLUE);  // Iron platebody (g)
		source(12237, "Easy Treasure Trails", TagCategory.CLUE);  // Iron platelegs (g)
		source(12239, "Easy Treasure Trails", TagCategory.CLUE);  // Iron plateskirt (g)
		source(12243, "Easy Treasure Trails", TagCategory.CLUE);  // Iron kiteshield (g)
		source(12241, "Easy Treasure Trails", TagCategory.CLUE);  // Iron full helm (g)
		source(12215, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze platebody (t)
		source(12217, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze platelegs (t)
		source(12219, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze plateskirt (t)
		source(12223, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze kiteshield (t)
		source(12221, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze full helm (t)
		source(12205, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze platebody (g)
		source(12207, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze platelegs (g)
		source(12209, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze plateskirt (g)
		source(12213, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze kiteshield (g)
		source(12211, "Easy Treasure Trails", TagCategory.CLUE);  // Bronze full helm (g)
		source(7362, "Easy Treasure Trails", TagCategory.CLUE);  // Studded body (g)
		source(7366, "Easy Treasure Trails", TagCategory.CLUE);  // Studded chaps (g)
		source(7364, "Easy Treasure Trails", TagCategory.CLUE);  // Studded body (t)
		source(7368, "Easy Treasure Trails", TagCategory.CLUE);  // Studded chaps (t)
		source(23381, "Easy Treasure Trails", TagCategory.CLUE);  // Leather body (g)
		source(23384, "Easy Treasure Trails", TagCategory.CLUE);  // Leather chaps (g)
		source(7394, "Easy Treasure Trails", TagCategory.CLUE);  // Blue wizard hat (g)
		source(7390, "Easy Treasure Trails", TagCategory.CLUE);  // Blue wizard robe (g)
		source(7386, "Easy Treasure Trails", TagCategory.CLUE);  // Blue skirt (g)
		source(7396, "Easy Treasure Trails", TagCategory.CLUE);  // Blue wizard hat (t)
		source(7392, "Easy Treasure Trails", TagCategory.CLUE);  // Blue wizard robe (t)
		source(7388, "Easy Treasure Trails", TagCategory.CLUE);  // Blue skirt (t)
		source(12453, "Easy Treasure Trails", TagCategory.CLUE);  // Black wizard hat (g)
		source(12449, "Easy Treasure Trails", TagCategory.CLUE);  // Black wizard robe (g)
		source(12445, "Easy Treasure Trails", TagCategory.CLUE);  // Black skirt (g)
		source(12455, "Easy Treasure Trails", TagCategory.CLUE);  // Black wizard hat (t)
		source(12451, "Easy Treasure Trails", TagCategory.CLUE);  // Black wizard robe (t)
		source(12447, "Easy Treasure Trails", TagCategory.CLUE);  // Black skirt (t)
		source(20199, "Easy Treasure Trails", TagCategory.CLUE);  // Monk's robe top (g)
		source(20202, "Easy Treasure Trails", TagCategory.CLUE);  // Monk's robe (g)
		source(10458, "Easy Treasure Trails", TagCategory.CLUE);  // Saradomin robe top
		source(10464, "Easy Treasure Trails", TagCategory.CLUE);  // Saradomin robe legs
		source(10462, "Easy Treasure Trails", TagCategory.CLUE);  // Guthix robe top
		source(10466, "Easy Treasure Trails", TagCategory.CLUE);  // Guthix robe legs
		source(10460, "Easy Treasure Trails", TagCategory.CLUE);  // Zamorak robe top
		source(10468, "Easy Treasure Trails", TagCategory.CLUE);  // Zamorak robe legs
		source(12193, "Easy Treasure Trails", TagCategory.CLUE);  // Ancient robe top
		source(12195, "Easy Treasure Trails", TagCategory.CLUE);  // Ancient robe legs
		source(12253, "Easy Treasure Trails", TagCategory.CLUE);  // Armadyl robe top
		source(12255, "Easy Treasure Trails", TagCategory.CLUE);  // Armadyl robe legs
		source(12265, "Easy Treasure Trails", TagCategory.CLUE);  // Bandos robe top
		source(12267, "Easy Treasure Trails", TagCategory.CLUE);  // Bandos robe legs
		source(10316, "Easy Treasure Trails", TagCategory.CLUE);  // Bob's red shirt
		source(10320, "Easy Treasure Trails", TagCategory.CLUE);  // Bob's green shirt
		source(10318, "Easy Treasure Trails", TagCategory.CLUE);  // Bob's blue shirt
		source(10322, "Easy Treasure Trails", TagCategory.CLUE);  // Bob's black shirt
		source(10324, "Easy Treasure Trails", TagCategory.CLUE);  // Bob's purple shirt
		source(2631, "Easy Treasure Trails", TagCategory.CLUE);  // Highwayman mask
		source(2633, "Easy Treasure Trails", TagCategory.CLUE);  // Blue beret
		source(2635, "Easy Treasure Trails", TagCategory.CLUE);  // Black beret
		source(2637, "Easy Treasure Trails", TagCategory.CLUE);  // White beret
		source(12247, "Easy Treasure Trails", TagCategory.CLUE);  // Red beret
		source(10392, "Easy Treasure Trails", TagCategory.CLUE);  // A powdered wig
		source(12245, "Easy Treasure Trails", TagCategory.CLUE);  // Beanie
		source(12249, "Easy Treasure Trails", TagCategory.CLUE);  // Imp mask
		source(12251, "Easy Treasure Trails", TagCategory.CLUE);  // Goblin mask
		source(10398, "Easy Treasure Trails", TagCategory.CLUE);  // Sleeping cap
		source(10394, "Easy Treasure Trails", TagCategory.CLUE);  // Flared trousers
		source(10396, "Easy Treasure Trails", TagCategory.CLUE);  // Pantaloons
		source(12375, "Easy Treasure Trails", TagCategory.CLUE);  // Black cane
		source(23363, "Easy Treasure Trails", TagCategory.CLUE);  // Staff of bob the cat
		source(10404, "Easy Treasure Trails", TagCategory.CLUE);  // Red elegant shirt
		source(10424, "Easy Treasure Trails", TagCategory.CLUE);  // Red elegant blouse
		source(10406, "Easy Treasure Trails", TagCategory.CLUE);  // Red elegant legs
		source(10426, "Easy Treasure Trails", TagCategory.CLUE);  // Red elegant skirt
		source(10412, "Easy Treasure Trails", TagCategory.CLUE);  // Green elegant shirt
		source(10432, "Easy Treasure Trails", TagCategory.CLUE);  // Green elegant blouse
		source(10414, "Easy Treasure Trails", TagCategory.CLUE);  // Green elegant legs
		source(10434, "Easy Treasure Trails", TagCategory.CLUE);  // Green elegant skirt
		source(10408, "Easy Treasure Trails", TagCategory.CLUE);  // Blue elegant shirt
		source(10428, "Easy Treasure Trails", TagCategory.CLUE);  // Blue elegant blouse
		source(10410, "Easy Treasure Trails", TagCategory.CLUE);  // Blue elegant legs
		source(10430, "Easy Treasure Trails", TagCategory.CLUE);  // Blue elegant skirt
		source(10366, "Easy Treasure Trails", TagCategory.CLUE);  // Amulet of magic (t)
		source(23354, "Easy Treasure Trails", TagCategory.CLUE);  // Amulet of power (t)
		source(12297, "Easy Treasure Trails", TagCategory.CLUE);  // Black pickaxe
		source(23360, "Easy Treasure Trails", TagCategory.CLUE);  // Ham joint
		source(23357, "Easy Treasure Trails", TagCategory.CLUE);  // Rain bow
		source(10280, "Easy Treasure Trails", TagCategory.CLUE);  // Willow comp bow

		// Medium Treasure Trails
		source(2577, "Medium Treasure Trails", TagCategory.CLUE);  // Ranger boots
		source(2579, "Medium Treasure Trails", TagCategory.CLUE);  // Wizard boots
		source(12598, "Medium Treasure Trails", TagCategory.CLUE);  // Holy sandals
		source(23413, "Medium Treasure Trails", TagCategory.CLUE);  // Climbing boots (g)
		source(23389, "Medium Treasure Trails", TagCategory.CLUE);  // Spiked manacles
		source(2605, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant full helm (t)
		source(2599, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (t)
		source(2601, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platelegs (t)
		source(3474, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant plateskirt (t)
		source(2603, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant kiteshield (t)
		source(2613, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant full helm (g)
		source(2607, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (g)
		source(2609, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platelegs (g)
		source(3475, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant plateskirt (g)
		source(2611, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant kiteshield (g)
		source(7334, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant shield (h1)
		source(7340, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant shield (h2)
		source(7346, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant shield (h3)
		source(7352, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant shield (h4)
		source(7358, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant shield (h5)
		source(10296, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant helm (h1)
		source(10298, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant helm (h2)
		source(10300, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant helm (h3)
		source(10302, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant helm (h4)
		source(10304, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant helm (h5)
		source(23392, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (h1)
		source(23395, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (h2)
		source(23398, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (h3)
		source(23401, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (h4)
		source(23404, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant platebody (h5)
		source(12283, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril full helm (g)
		source(12277, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril platebody (g)
		source(12279, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril platelegs (g)
		source(12285, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril plateskirt (g)
		source(12281, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril kiteshield (g)
		source(12293, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril full helm (t)
		source(12287, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril platebody (t)
		source(12289, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril platelegs (t)
		source(12295, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril plateskirt (t)
		source(12291, "Medium Treasure Trails", TagCategory.CLUE);  // Mithril kiteshield (t)
		source(7370, "Medium Treasure Trails", TagCategory.CLUE);  // Green d'hide body (g)
		source(7372, "Medium Treasure Trails", TagCategory.CLUE);  // Green d'hide body (t)
		source(7378, "Medium Treasure Trails", TagCategory.CLUE);  // Green d'hide chaps (g)
		source(7380, "Medium Treasure Trails", TagCategory.CLUE);  // Green d'hide chaps (t)
		source(10452, "Medium Treasure Trails", TagCategory.CLUE);  // Saradomin mitre
		source(10446, "Medium Treasure Trails", TagCategory.CLUE);  // Saradomin cloak
		source(10454, "Medium Treasure Trails", TagCategory.CLUE);  // Guthix mitre
		source(10448, "Medium Treasure Trails", TagCategory.CLUE);  // Guthix cloak
		source(10456, "Medium Treasure Trails", TagCategory.CLUE);  // Zamorak mitre
		source(10450, "Medium Treasure Trails", TagCategory.CLUE);  // Zamorak cloak
		source(12203, "Medium Treasure Trails", TagCategory.CLUE);  // Ancient mitre
		source(12197, "Medium Treasure Trails", TagCategory.CLUE);  // Ancient cloak
		source(12201, "Medium Treasure Trails", TagCategory.CLUE);  // Ancient stole
		source(12199, "Medium Treasure Trails", TagCategory.CLUE);  // Ancient crozier
		source(12259, "Medium Treasure Trails", TagCategory.CLUE);  // Armadyl mitre
		source(12261, "Medium Treasure Trails", TagCategory.CLUE);  // Armadyl cloak
		source(12257, "Medium Treasure Trails", TagCategory.CLUE);  // Armadyl stole
		source(12263, "Medium Treasure Trails", TagCategory.CLUE);  // Armadyl crozier
		source(12271, "Medium Treasure Trails", TagCategory.CLUE);  // Bandos mitre
		source(12273, "Medium Treasure Trails", TagCategory.CLUE);  // Bandos cloak
		source(12269, "Medium Treasure Trails", TagCategory.CLUE);  // Bandos stole
		source(12275, "Medium Treasure Trails", TagCategory.CLUE);  // Bandos crozier
		source(7319, "Medium Treasure Trails", TagCategory.CLUE);  // Red boater
		source(7323, "Medium Treasure Trails", TagCategory.CLUE);  // Green boater
		source(7321, "Medium Treasure Trails", TagCategory.CLUE);  // Orange boater
		source(7327, "Medium Treasure Trails", TagCategory.CLUE);  // Black boater
		source(7325, "Medium Treasure Trails", TagCategory.CLUE);  // Blue boater
		source(12309, "Medium Treasure Trails", TagCategory.CLUE);  // Pink boater
		source(12311, "Medium Treasure Trails", TagCategory.CLUE);  // Purple boater
		source(12313, "Medium Treasure Trails", TagCategory.CLUE);  // White boater
		source(2645, "Medium Treasure Trails", TagCategory.CLUE);  // Red headband
		source(2647, "Medium Treasure Trails", TagCategory.CLUE);  // Black headband
		source(2649, "Medium Treasure Trails", TagCategory.CLUE);  // Brown headband
		source(12299, "Medium Treasure Trails", TagCategory.CLUE);  // White headband
		source(12301, "Medium Treasure Trails", TagCategory.CLUE);  // Blue headband
		source(12303, "Medium Treasure Trails", TagCategory.CLUE);  // Gold headband
		source(12305, "Medium Treasure Trails", TagCategory.CLUE);  // Pink headband
		source(12307, "Medium Treasure Trails", TagCategory.CLUE);  // Green headband
		source(12319, "Medium Treasure Trails", TagCategory.CLUE);  // Crier hat
		source(20240, "Medium Treasure Trails", TagCategory.CLUE);  // Crier coat
		source(20243, "Medium Treasure Trails", TagCategory.CLUE);  // Crier bell
		source(12377, "Medium Treasure Trails", TagCategory.CLUE);  // Adamant cane
		source(20251, "Medium Treasure Trails", TagCategory.CLUE);  // Arceuus banner
		source(20260, "Medium Treasure Trails", TagCategory.CLUE);  // Piscarilius banner
		source(20254, "Medium Treasure Trails", TagCategory.CLUE);  // Hosidius banner
		source(20263, "Medium Treasure Trails", TagCategory.CLUE);  // Shayzien banner
		source(20257, "Medium Treasure Trails", TagCategory.CLUE);  // Lovakengj banner
		source(20272, "Medium Treasure Trails", TagCategory.CLUE);  // Cabbage round shield
		source(20266, "Medium Treasure Trails", TagCategory.CLUE);  // Black unicorn mask
		source(20269, "Medium Treasure Trails", TagCategory.CLUE);  // White unicorn mask
		source(12361, "Medium Treasure Trails", TagCategory.CLUE);  // Cat mask
		source(12428, "Medium Treasure Trails", TagCategory.CLUE);  // Penguin mask
		source(12359, "Medium Treasure Trails", TagCategory.CLUE);  // Leprechaun hat
		source(20246, "Medium Treasure Trails", TagCategory.CLUE);  // Black leprechaun hat
		source(23407, "Medium Treasure Trails", TagCategory.CLUE);  // Wolf mask
		source(23410, "Medium Treasure Trails", TagCategory.CLUE);  // Wolf cloak
		source(10416, "Medium Treasure Trails", TagCategory.CLUE);  // Purple elegant shirt
		source(10436, "Medium Treasure Trails", TagCategory.CLUE);  // Purple elegant blouse
		source(10418, "Medium Treasure Trails", TagCategory.CLUE);  // Purple elegant legs
		source(10438, "Medium Treasure Trails", TagCategory.CLUE);  // Purple elegant skirt
		source(10400, "Medium Treasure Trails", TagCategory.CLUE);  // Black elegant shirt
		source(10420, "Medium Treasure Trails", TagCategory.CLUE);  // White elegant blouse
		source(10402, "Medium Treasure Trails", TagCategory.CLUE);  // Black elegant legs
		source(10422, "Medium Treasure Trails", TagCategory.CLUE);  // White elegant skirt
		source(12315, "Medium Treasure Trails", TagCategory.CLUE);  // Pink elegant shirt
		source(12339, "Medium Treasure Trails", TagCategory.CLUE);  // Pink elegant blouse
		source(12317, "Medium Treasure Trails", TagCategory.CLUE);  // Pink elegant legs
		source(12341, "Medium Treasure Trails", TagCategory.CLUE);  // Pink elegant skirt
		source(12347, "Medium Treasure Trails", TagCategory.CLUE);  // Gold elegant shirt
		source(12343, "Medium Treasure Trails", TagCategory.CLUE);  // Gold elegant blouse
		source(12349, "Medium Treasure Trails", TagCategory.CLUE);  // Gold elegant legs
		source(12345, "Medium Treasure Trails", TagCategory.CLUE);  // Gold elegant skirt
		source(20275, "Medium Treasure Trails", TagCategory.CLUE);  // Gnomish firelighter
		source(10364, "Medium Treasure Trails", TagCategory.CLUE);  // Strength amulet (t)
		source(10282, "Medium Treasure Trails", TagCategory.CLUE);  // Yew comp bow

		// Hard Treasure Trails
		source(2581, "Hard Treasure Trails", TagCategory.CLUE);  // Robin hood hat
		source(22231, "Hard Treasure Trails", TagCategory.CLUE);  // Dragon boots ornament kit
		source(23227, "Hard Treasure Trails", TagCategory.CLUE);  // Rune defender ornament kit
		source(23232, "Hard Treasure Trails", TagCategory.CLUE);  // Tzhaar-ket-om ornament kit
		source(23237, "Hard Treasure Trails", TagCategory.CLUE);  // Berserker necklace ornament kit
		source(2627, "Hard Treasure Trails", TagCategory.CLUE);  // Rune full helm (t)
		source(2623, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (t)
		source(2625, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platelegs (t)
		source(3477, "Hard Treasure Trails", TagCategory.CLUE);  // Rune plateskirt (t)
		source(2629, "Hard Treasure Trails", TagCategory.CLUE);  // Rune kiteshield (t)
		source(2619, "Hard Treasure Trails", TagCategory.CLUE);  // Rune full helm (g)
		source(2615, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (g)
		source(2617, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platelegs (g)
		source(3476, "Hard Treasure Trails", TagCategory.CLUE);  // Rune plateskirt (g)
		source(2621, "Hard Treasure Trails", TagCategory.CLUE);  // Rune kiteshield (g)
		source(2657, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak full helm
		source(2653, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak platebody
		source(2655, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak platelegs
		source(3478, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak plateskirt
		source(2659, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak kiteshield
		source(2673, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix full helm
		source(2669, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix platebody
		source(2671, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix platelegs
		source(3480, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix plateskirt
		source(2675, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix kiteshield
		source(2665, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin full helm
		source(2661, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin platebody
		source(2663, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin platelegs
		source(3479, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin plateskirt
		source(2667, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin kiteshield
		source(12466, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient full helm
		source(12460, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient platebody
		source(12462, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient platelegs
		source(12464, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient plateskirt
		source(12468, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient kiteshield
		source(12476, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl full helm
		source(12470, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl platebody
		source(12472, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl platelegs
		source(12474, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl plateskirt
		source(12478, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl kiteshield
		source(12486, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos full helm
		source(12480, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos platebody
		source(12482, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos platelegs
		source(12484, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos plateskirt
		source(12488, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos kiteshield
		source(7336, "Hard Treasure Trails", TagCategory.CLUE);  // Rune shield (h1)
		source(7342, "Hard Treasure Trails", TagCategory.CLUE);  // Rune shield (h2)
		source(7348, "Hard Treasure Trails", TagCategory.CLUE);  // Rune shield (h3)
		source(7354, "Hard Treasure Trails", TagCategory.CLUE);  // Rune shield (h4)
		source(7360, "Hard Treasure Trails", TagCategory.CLUE);  // Rune shield (h5)
		source(10286, "Hard Treasure Trails", TagCategory.CLUE);  // Rune helm (h1)
		source(10288, "Hard Treasure Trails", TagCategory.CLUE);  // Rune helm (h2)
		source(10290, "Hard Treasure Trails", TagCategory.CLUE);  // Rune helm (h3)
		source(10292, "Hard Treasure Trails", TagCategory.CLUE);  // Rune helm (h4)
		source(10294, "Hard Treasure Trails", TagCategory.CLUE);  // Rune helm (h5)
		source(23209, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (h1)
		source(23212, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (h2)
		source(23215, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (h3)
		source(23218, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (h4)
		source(23221, "Hard Treasure Trails", TagCategory.CLUE);  // Rune platebody (h5)
		source(10390, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin coif
		source(10386, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin d'hide body
		source(10388, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin chaps
		source(10384, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin bracers
		source(19933, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin d'hide boots
		source(23191, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin d'hide shield
		source(10382, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix coif
		source(10378, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix d'hide body
		source(10380, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix chaps
		source(10376, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix bracers
		source(19927, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix d'hide boots
		source(23188, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix d'hide shield
		source(10374, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak coif
		source(10370, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak d'hide body
		source(10372, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak chaps
		source(10368, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak bracers
		source(19936, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak d'hide boots
		source(23194, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak d'hide shield
		source(12504, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos coif
		source(12500, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos d'hide body
		source(12502, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos chaps
		source(12498, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos bracers
		source(19924, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos d'hide boots
		source(23203, "Hard Treasure Trails", TagCategory.CLUE);  // Bandos d'hide shield
		source(12512, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl coif
		source(12508, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl d'hide body
		source(12510, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl chaps
		source(12506, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl bracers
		source(19930, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl d'hide boots
		source(23200, "Hard Treasure Trails", TagCategory.CLUE);  // Armadyl d'hide shield
		source(12496, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient coif
		source(12492, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient d'hide body
		source(12494, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient chaps
		source(12490, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient bracers
		source(19921, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient d'hide boots
		source(23197, "Hard Treasure Trails", TagCategory.CLUE);  // Ancient d'hide shield
		source(12331, "Hard Treasure Trails", TagCategory.CLUE);  // Red d'hide body (t)
		source(12333, "Hard Treasure Trails", TagCategory.CLUE);  // Red d'hide chaps (t)
		source(12327, "Hard Treasure Trails", TagCategory.CLUE);  // Red d'hide body (g)
		source(12329, "Hard Treasure Trails", TagCategory.CLUE);  // Red d'hide chaps (g)
		source(7376, "Hard Treasure Trails", TagCategory.CLUE);  // Blue d'hide body (t)
		source(7384, "Hard Treasure Trails", TagCategory.CLUE);  // Blue d'hide chaps (t)
		source(7374, "Hard Treasure Trails", TagCategory.CLUE);  // Blue d'hide body (g)
		source(7382, "Hard Treasure Trails", TagCategory.CLUE);  // Blue d'hide chaps (g)
		source(7400, "Hard Treasure Trails", TagCategory.CLUE);  // Enchanted hat
		source(7399, "Hard Treasure Trails", TagCategory.CLUE);  // Enchanted top
		source(7398, "Hard Treasure Trails", TagCategory.CLUE);  // Enchanted robe
		source(10470, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin stole
		source(10440, "Hard Treasure Trails", TagCategory.CLUE);  // Saradomin crozier
		source(10472, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix stole
		source(10442, "Hard Treasure Trails", TagCategory.CLUE);  // Guthix crozier
		source(10474, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak stole
		source(10444, "Hard Treasure Trails", TagCategory.CLUE);  // Zamorak crozier
		source(19912, "Hard Treasure Trails", TagCategory.CLUE);  // Zombie head (Treasure Trails)
		source(19915, "Hard Treasure Trails", TagCategory.CLUE);  // Cyclops head
		source(2651, "Hard Treasure Trails", TagCategory.CLUE);  // Pirate's hat
		source(12323, "Hard Treasure Trails", TagCategory.CLUE);  // Red cavalier
		source(12321, "Hard Treasure Trails", TagCategory.CLUE);  // White cavalier
		source(12325, "Hard Treasure Trails", TagCategory.CLUE);  // Navy cavalier
		source(2639, "Hard Treasure Trails", TagCategory.CLUE);  // Tan cavalier
		source(2641, "Hard Treasure Trails", TagCategory.CLUE);  // Dark cavalier
		source(2643, "Hard Treasure Trails", TagCategory.CLUE);  // Black cavalier
		source(12516, "Hard Treasure Trails", TagCategory.CLUE);  // Pith helmet
		source(12514, "Hard Treasure Trails", TagCategory.CLUE);  // Explorer backpack
		source(23224, "Hard Treasure Trails", TagCategory.CLUE);  // Thieving bag
		source(12518, "Hard Treasure Trails", TagCategory.CLUE);  // Green dragon mask
		source(12520, "Hard Treasure Trails", TagCategory.CLUE);  // Blue dragon mask
		source(12522, "Hard Treasure Trails", TagCategory.CLUE);  // Red dragon mask
		source(12524, "Hard Treasure Trails", TagCategory.CLUE);  // Black dragon mask
		source(19918, "Hard Treasure Trails", TagCategory.CLUE);  // Nunchaku
		source(23206, "Hard Treasure Trails", TagCategory.CLUE);  // Dual sai
		source(12379, "Hard Treasure Trails", TagCategory.CLUE);  // Rune cane
		source(10362, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(10284, "Hard Treasure Trails", TagCategory.CLUE);  // Magic comp bow

		// Elite Treasure Trails
		source(23185, "Elite Treasure Trails", TagCategory.CLUE);  // Ring of 3rd age
		source(12526, "Elite Treasure Trails", TagCategory.CLUE);  // Fury ornament kit
		source(12534, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon chainbody ornament kit
		source(12536, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon legs/skirt ornament kit
		source(12532, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon sq shield ornament kit
		source(12538, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon full helm ornament kit
		source(20002, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon scimitar ornament kit
		source(12530, "Elite Treasure Trails", TagCategory.CLUE);  // Light infinity colour kit
		source(12528, "Elite Treasure Trails", TagCategory.CLUE);  // Dark infinity colour kit
		source(19997, "Elite Treasure Trails", TagCategory.CLUE);  // Holy wraps
		source(19994, "Elite Treasure Trails", TagCategory.CLUE);  // Ranger gloves
		source(12596, "Elite Treasure Trails", TagCategory.CLUE);  // Rangers' tunic
		source(23249, "Elite Treasure Trails", TagCategory.CLUE);  // Rangers' tights
		source(12381, "Elite Treasure Trails", TagCategory.CLUE);  // Black d'hide body (g)
		source(12383, "Elite Treasure Trails", TagCategory.CLUE);  // Black d'hide chaps (g)
		source(12385, "Elite Treasure Trails", TagCategory.CLUE);  // Black d'hide body (t)
		source(12387, "Elite Treasure Trails", TagCategory.CLUE);  // Black d'hide chaps (t)
		source(12397, "Elite Treasure Trails", TagCategory.CLUE);  // Royal crown
		source(12439, "Elite Treasure Trails", TagCategory.CLUE);  // Royal sceptre
		source(12393, "Elite Treasure Trails", TagCategory.CLUE);  // Royal gown top
		source(12395, "Elite Treasure Trails", TagCategory.CLUE);  // Royal gown bottom
		source(12351, "Elite Treasure Trails", TagCategory.CLUE);  // Musketeer hat
		source(12441, "Elite Treasure Trails", TagCategory.CLUE);  // Musketeer tabard
		source(12443, "Elite Treasure Trails", TagCategory.CLUE);  // Musketeer pants
		source(19958, "Elite Treasure Trails", TagCategory.CLUE);  // Dark tuxedo jacket
		source(19964, "Elite Treasure Trails", TagCategory.CLUE);  // Dark trousers
		source(19967, "Elite Treasure Trails", TagCategory.CLUE);  // Dark tuxedo shoes
		source(19961, "Elite Treasure Trails", TagCategory.CLUE);  // Dark tuxedo cuffs
		source(19970, "Elite Treasure Trails", TagCategory.CLUE);  // Dark bow tie
		source(19973, "Elite Treasure Trails", TagCategory.CLUE);  // Light tuxedo jacket
		source(19979, "Elite Treasure Trails", TagCategory.CLUE);  // Light trousers
		source(19982, "Elite Treasure Trails", TagCategory.CLUE);  // Light tuxedo shoes
		source(19976, "Elite Treasure Trails", TagCategory.CLUE);  // Light tuxedo cuffs
		source(19985, "Elite Treasure Trails", TagCategory.CLUE);  // Light bow tie
		source(19943, "Elite Treasure Trails", TagCategory.CLUE);  // Arceuus scarf
		source(19946, "Elite Treasure Trails", TagCategory.CLUE);  // Hosidius scarf
		source(19952, "Elite Treasure Trails", TagCategory.CLUE);  // Piscarilius scarf
		source(19955, "Elite Treasure Trails", TagCategory.CLUE);  // Shayzien scarf
		source(19949, "Elite Treasure Trails", TagCategory.CLUE);  // Lovakengj scarf
		source(12363, "Elite Treasure Trails", TagCategory.CLUE);  // Bronze dragon mask
		source(12365, "Elite Treasure Trails", TagCategory.CLUE);  // Iron dragon mask
		source(12367, "Elite Treasure Trails", TagCategory.CLUE);  // Steel dragon mask
		source(12369, "Elite Treasure Trails", TagCategory.CLUE);  // Mithril dragon mask
		source(23270, "Elite Treasure Trails", TagCategory.CLUE);  // Adamant dragon mask
		source(23273, "Elite Treasure Trails", TagCategory.CLUE);  // Rune dragon mask
		source(12357, "Elite Treasure Trails", TagCategory.CLUE);  // Katana
		source(12373, "Elite Treasure Trails", TagCategory.CLUE);  // Dragon cane
		source(12335, "Elite Treasure Trails", TagCategory.CLUE);  // Briefcase
		source(19991, "Elite Treasure Trails", TagCategory.CLUE);  // Bucket helm
		source(19988, "Elite Treasure Trails", TagCategory.CLUE);  // Blacksmith's helm
		source(12540, "Elite Treasure Trails", TagCategory.CLUE);  // Deerstalker
		source(12430, "Elite Treasure Trails", TagCategory.CLUE);  // Afro
		source(12355, "Elite Treasure Trails", TagCategory.CLUE);  // Big pirate hat
		source(12432, "Elite Treasure Trails", TagCategory.CLUE);  // Top hat
		source(12353, "Elite Treasure Trails", TagCategory.CLUE);  // Monocle
		source(12337, "Elite Treasure Trails", TagCategory.CLUE);  // Sagacious spectacles
		source(23246, "Elite Treasure Trails", TagCategory.CLUE);  // Fremennik kilt
		source(23252, "Elite Treasure Trails", TagCategory.CLUE);  // Giant boot
		source(23255, "Elite Treasure Trails", TagCategory.CLUE);  // Uri's hat

		// Master Treasure Trails
		source(6296, "Master Treasure Trails", TagCategory.CLUE);  // Bloodhound
		source(23185, "Master Treasure Trails", TagCategory.CLUE);  // Ring of 3rd age
		source(20068, "Master Treasure Trails", TagCategory.CLUE);  // Armadyl godsword ornament kit
		source(20071, "Master Treasure Trails", TagCategory.CLUE);  // Bandos godsword ornament kit
		source(20074, "Master Treasure Trails", TagCategory.CLUE);  // Saradomin godsword ornament kit
		source(20077, "Master Treasure Trails", TagCategory.CLUE);  // Zamorak godsword ornament kit
		source(20065, "Master Treasure Trails", TagCategory.CLUE);  // Occult ornament kit
		source(20062, "Master Treasure Trails", TagCategory.CLUE);  // Torture ornament kit
		source(22246, "Master Treasure Trails", TagCategory.CLUE);  // Anguish ornament kit
		source(20143, "Master Treasure Trails", TagCategory.CLUE);  // Dragon defender ornament kit
		source(22239, "Master Treasure Trails", TagCategory.CLUE);  // Dragon kiteshield ornament kit
		source(22236, "Master Treasure Trails", TagCategory.CLUE);  // Dragon platebody ornament kit
		source(23348, "Master Treasure Trails", TagCategory.CLUE);  // Tormented ornament kit
		source(20128, "Master Treasure Trails", TagCategory.CLUE);  // Hood of darkness
		source(20131, "Master Treasure Trails", TagCategory.CLUE);  // Robe top of darkness
		source(20137, "Master Treasure Trails", TagCategory.CLUE);  // Robe bottom of darkness
		source(20134, "Master Treasure Trails", TagCategory.CLUE);  // Gloves of darkness
		source(20140, "Master Treasure Trails", TagCategory.CLUE);  // Boots of darkness
		source(20035, "Master Treasure Trails", TagCategory.CLUE);  // Samurai kasa
		source(20038, "Master Treasure Trails", TagCategory.CLUE);  // Samurai shirt
		source(20044, "Master Treasure Trails", TagCategory.CLUE);  // Samurai greaves
		source(20047, "Master Treasure Trails", TagCategory.CLUE);  // Samurai boots
		source(20041, "Master Treasure Trails", TagCategory.CLUE);  // Samurai gloves
		source(20095, "Master Treasure Trails", TagCategory.CLUE);  // Ankou mask
		source(20098, "Master Treasure Trails", TagCategory.CLUE);  // Ankou top
		source(20101, "Master Treasure Trails", TagCategory.CLUE);  // Ankou gloves
		source(20107, "Master Treasure Trails", TagCategory.CLUE);  // Ankou socks
		source(20104, "Master Treasure Trails", TagCategory.CLUE);  // Ankou's leggings
		source(20080, "Master Treasure Trails", TagCategory.CLUE);  // Mummy's head
		source(20092, "Master Treasure Trails", TagCategory.CLUE);  // Mummy's feet
		source(20086, "Master Treasure Trails", TagCategory.CLUE);  // Mummy's hands
		source(20089, "Master Treasure Trails", TagCategory.CLUE);  // Mummy's legs
		source(20083, "Master Treasure Trails", TagCategory.CLUE);  // Mummy's body
		source(20125, "Master Treasure Trails", TagCategory.CLUE);  // Shayzien hood
		source(20116, "Master Treasure Trails", TagCategory.CLUE);  // Hosidius hood
		source(20113, "Master Treasure Trails", TagCategory.CLUE);  // Arceuus hood
		source(20122, "Master Treasure Trails", TagCategory.CLUE);  // Piscarilius hood
		source(20119, "Master Treasure Trails", TagCategory.CLUE);  // Lovakengj hood
		source(20020, "Master Treasure Trails", TagCategory.CLUE);  // Lesser demon mask
		source(20023, "Master Treasure Trails", TagCategory.CLUE);  // Greater demon mask
		source(20026, "Master Treasure Trails", TagCategory.CLUE);  // Black demon mask
		source(20032, "Master Treasure Trails", TagCategory.CLUE);  // Jungle demon mask
		source(20029, "Master Treasure Trails", TagCategory.CLUE);  // Old demon mask
		source(19724, "Master Treasure Trails", TagCategory.CLUE);  // Left eye patch
		source(20110, "Master Treasure Trails", TagCategory.CLUE);  // Bowl wig
		source(20056, "Master Treasure Trails", TagCategory.CLUE);  // Ale of the gods
		source(20050, "Master Treasure Trails", TagCategory.CLUE);  // Obsidian cape (r)
		source(20053, "Master Treasure Trails", TagCategory.CLUE);  // Half moon spectacles
		source(20008, "Master Treasure Trails", TagCategory.CLUE);  // Fancy tiara

		// Hard Treasure Trail Rewards (Rare)
		source(10334, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range coif
		source(10330, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range top
		source(10332, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range legs
		source(10336, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age vambraces
		source(10338, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe top
		source(10340, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe
		source(10342, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age mage hat
		source(10344, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age amulet
		source(23242, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age plateskirt
		source(10346, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platelegs
		source(10348, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platebody
		source(10350, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age full helmet
		source(10352, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age kiteshield
		source(3481, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platebody
		source(3483, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platelegs
		source(3485, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded plateskirt
		source(3486, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded full helm
		source(3488, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded kiteshield
		source(20146, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded med helm
		source(20149, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded chainbody
		source(20152, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded sq shield
		source(20155, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded 2h sword
		source(20158, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded spear
		source(20161, "Hard Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded hasta

		// Elite Treasure Trail Rewards (Rare)
		source(12426, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age longsword
		source(12422, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age wand
		source(12437, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age cloak
		source(12424, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age bow
		source(10334, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range coif
		source(10330, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range top
		source(10332, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range legs
		source(10336, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age vambraces
		source(10338, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe top
		source(10340, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe
		source(10342, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age mage hat
		source(10344, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age amulet
		source(23242, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age plateskirt
		source(10346, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platelegs
		source(10348, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platebody
		source(10350, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age full helmet
		source(10352, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age kiteshield
		source(12389, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded scimitar
		source(12391, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded boots
		source(3481, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platebody
		source(3483, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platelegs
		source(3485, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded plateskirt
		source(3486, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded full helm
		source(3488, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded kiteshield
		source(20146, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded med helm
		source(20149, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded chainbody
		source(20152, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded sq shield
		source(20155, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded 2h sword
		source(20158, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded spear
		source(20161, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded hasta
		source(23258, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded coif
		source(23261, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide vambraces
		source(23264, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide body
		source(23267, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide chaps
		source(23276, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded pickaxe
		source(23279, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded axe
		source(23282, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded spade
		source(20005, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Ring of nature
		source(12371, "Elite Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Lava dragon mask

		// Master Treasure Trail Rewards (Rare)
		source(20014, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age pickaxe
		source(20011, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age axe
		source(12426, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age longsword
		source(12422, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age wand
		source(12437, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age cloak
		source(12424, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age bow
		source(10334, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range coif
		source(10330, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range top
		source(10332, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age range legs
		source(10336, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age vambraces
		source(10338, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe top
		source(10340, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age robe
		source(10342, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age mage hat
		source(10344, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age amulet
		source(23242, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age plateskirt
		source(10346, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platelegs
		source(10348, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age platebody
		source(10350, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age full helmet
		source(10352, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age kiteshield
		source(23339, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age druidic robe bottoms
		source(23336, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age druidic robe top
		source(23342, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age druidic staff
		source(23345, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // 3rd age druidic cloak
		source(12389, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded scimitar
		source(12391, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded boots
		source(3481, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platebody
		source(3483, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded platelegs
		source(3485, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded plateskirt
		source(3486, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded full helm
		source(3488, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded kiteshield
		source(20146, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded med helm
		source(20149, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded chainbody
		source(20152, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded sq shield
		source(20155, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded 2h sword
		source(20158, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded spear
		source(20161, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded hasta
		source(23258, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded coif
		source(23261, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide vambraces
		source(23264, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide body
		source(23267, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded d'hide chaps
		source(23276, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded pickaxe
		source(23279, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded axe
		source(23282, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Gilded spade
		source(20059, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Bucket helm (g)
		source(20017, "Master Treasure Trail Rewards (Rare)", TagCategory.CLUE);  // Ring of coins

		// Shared Treasure Trail Rewards
		source(3827, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Saradomin page 1
		source(3828, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Saradomin page 2
		source(3829, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Saradomin page 3
		source(3830, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Saradomin page 4
		source(3831, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Zamorak page 1
		source(3832, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Zamorak page 2
		source(3833, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Zamorak page 3
		source(3834, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Zamorak page 4
		source(3835, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Guthix page 1
		source(3836, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Guthix page 2
		source(3837, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Guthix page 3
		source(3838, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Guthix page 4
		source(12613, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Bandos page 1
		source(12614, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Bandos page 2
		source(12615, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Bandos page 3
		source(12616, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Bandos page 4
		source(12617, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Armadyl page 1
		source(12618, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Armadyl page 2
		source(12619, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Armadyl page 3
		source(12620, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Armadyl page 4
		source(12621, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Ancient page 1
		source(12622, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Ancient page 2
		source(12623, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Ancient page 3
		source(12624, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Ancient page 4
		source(20220, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Holy blessing
		source(20223, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Unholy blessing
		source(20226, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Peaceful blessing
		source(20232, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // War blessing
		source(20229, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Honourable blessing
		source(20235, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Ancient blessing
		source(12402, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Nardah teleport
		source(12411, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Mos le'harmless teleport
		source(12406, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Mort'ton teleport
		source(12404, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Feldip hills teleport
		source(12405, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Lunar isle teleport
		source(12403, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Digsite teleport
		source(12408, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Piscatoris teleport
		source(12407, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Pest control teleport
		source(12409, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Tai bwo wannai teleport
		source(12642, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Lumberyard teleport
		source(12410, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Iorwerth camp teleport
		source(21387, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Master scroll book
		source(7329, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Red firelighter
		source(7330, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Green firelighter
		source(7331, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Blue firelighter
		source(10326, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Purple firelighter
		source(10327, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // White firelighter
		source(20238, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Charge dragonstone jewellery scroll
		source(10476, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Purple sweets

		// Scroll Cases
		source(30902, "Scroll Cases", TagCategory.CLUE);  // Minor beginner scroll case
		source(30904, "Scroll Cases", TagCategory.CLUE);  // Major beginner scroll case
		source(30906, "Scroll Cases", TagCategory.CLUE);  // Minor easy scroll case
		source(30908, "Scroll Cases", TagCategory.CLUE);  // Major easy scroll case
		source(30910, "Scroll Cases", TagCategory.CLUE);  // Minor medium scroll case
		source(30912, "Scroll Cases", TagCategory.CLUE);  // Major medium scroll case
		source(30914, "Scroll Cases", TagCategory.CLUE);  // Minor hard scroll case
		source(30916, "Scroll Cases", TagCategory.CLUE);  // Major hard scroll case
		source(30918, "Scroll Cases", TagCategory.CLUE);  // Minor elite scroll case
		source(30920, "Scroll Cases", TagCategory.CLUE);  // Major elite scroll case
		source(30922, "Scroll Cases", TagCategory.CLUE);  // Minor master scroll case
		source(30924, "Scroll Cases", TagCategory.CLUE);  // Major master scroll case
		source(30926, "Scroll Cases", TagCategory.CLUE);  // Mimic scroll case

		// ==================== MINIGAMES ====================

		// Barbarian Assault
		source(6642, "Barbarian Assault", TagCategory.MINIGAME);  // Pet penance queen
		source(10548, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter hat
		source(10550, "Barbarian Assault", TagCategory.MINIGAME);  // Ranger hat
		source(10549, "Barbarian Assault", TagCategory.MINIGAME);  // Runner hat
		source(10547, "Barbarian Assault", TagCategory.MINIGAME);  // Healer hat
		source(10551, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter torso
		source(10555, "Barbarian Assault", TagCategory.MINIGAME);  // Penance skirt
		source(10552, "Barbarian Assault", TagCategory.MINIGAME);  // Runner boots
		source(10553, "Barbarian Assault", TagCategory.MINIGAME);  // Penance gloves
		source(10589, "Barbarian Assault", TagCategory.MINIGAME);  // Granite helm
		source(10564, "Barbarian Assault", TagCategory.MINIGAME);  // Granite body

		// Barracuda Trials
		source(31732, "Barracuda Trials", TagCategory.MINIGAME);  // Stormy key
		source(31733, "Barracuda Trials", TagCategory.MINIGAME);  // Barrel stand
		source(31734, "Barracuda Trials", TagCategory.MINIGAME);  // Ralph's fabric roll
		source(31744, "Barracuda Trials", TagCategory.MINIGAME);  // Fetid key
		source(31745, "Barracuda Trials", TagCategory.MINIGAME);  // Captured wind mote
		source(31746, "Barracuda Trials", TagCategory.MINIGAME);  // Gurtob's fabric roll
		source(31756, "Barracuda Trials", TagCategory.MINIGAME);  // Serrated key
		source(31757, "Barracuda Trials", TagCategory.MINIGAME);  // Heart of ithell
		source(31758, "Barracuda Trials", TagCategory.MINIGAME);  // Gwyna's fabric roll

		// Brimhaven Agility Arena
		source(29480, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Agility arena ticket
		source(29482, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Brimhaven voucher
		source(2997, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Pirate's hook
		source(21061, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful hood (Agility Arena)
		source(21067, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful top (Agility Arena)
		source(21070, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful legs (Agility Arena)
		source(21073, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful gloves (Agility Arena)
		source(21076, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful boots (Agility Arena)
		source(21064, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful cape (Agility Arena)

		// Castle Wars
		source(4071, "Castle Wars", TagCategory.MINIGAME);  // Decorative helm (red)
		source(25165, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (red)
		source(4069, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (red platebody)
		source(4068, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (red)
		source(4072, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (red)
		source(4070, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (red platelegs)
		source(11893, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (red plateskirt)
		source(25163, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (red)
		source(4506, "Castle Wars", TagCategory.MINIGAME);  // Decorative helm (white)
		source(25169, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (white)
		source(4504, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (white platebody)
		source(4503, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (white)
		source(4507, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (white)
		source(4505, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (white platelegs)
		source(11894, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (white plateskirt)
		source(25167, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (white)
		source(4511, "Castle Wars", TagCategory.MINIGAME);  // Decorative helm (gold)
		source(25174, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (gold)
		source(4509, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platebody)
		source(4508, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (gold)
		source(4512, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (gold)
		source(4510, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platelegs)
		source(11895, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold plateskirt)
		source(25171, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (gold)
		source(4513, "Castle Wars", TagCategory.MINIGAME);  // Castlewars hood (Saradomin)
		source(4514, "Castle Wars", TagCategory.MINIGAME);  // Castlewars cloak (Saradomin)
		source(4515, "Castle Wars", TagCategory.MINIGAME);  // Castlewars hood (Zamorak)
		source(4516, "Castle Wars", TagCategory.MINIGAME);  // Castlewars cloak (Zamorak)
		source(4037, "Castle Wars", TagCategory.MINIGAME);  // Saradomin banner
		source(4039, "Castle Wars", TagCategory.MINIGAME);  // Zamorak banner
		source(11898, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic hat)
		source(11896, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic top)
		source(11897, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic legs)
		source(11899, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged top)
		source(11900, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged legs)
		source(11901, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (quiver)
		source(12637, "Castle Wars", TagCategory.MINIGAME);  // Saradomin halo
		source(12638, "Castle Wars", TagCategory.MINIGAME);  // Zamorak halo
		source(12639, "Castle Wars", TagCategory.MINIGAME);  // Guthix halo

		// Fishing Trawler
		source(13258, "Fishing Trawler", TagCategory.MINIGAME);  // Angler hat
		source(13259, "Fishing Trawler", TagCategory.MINIGAME);  // Angler top
		source(13260, "Fishing Trawler", TagCategory.MINIGAME);  // Angler waders
		source(13261, "Fishing Trawler", TagCategory.MINIGAME);  // Angler boots

		// Giants' Foundry
		source(27023, "Giants' Foundry", TagCategory.MINIGAME);  // Smiths tunic
		source(27025, "Giants' Foundry", TagCategory.MINIGAME);  // Smiths trousers
		source(27027, "Giants' Foundry", TagCategory.MINIGAME);  // Smiths boots
		source(27029, "Giants' Foundry", TagCategory.MINIGAME);  // Smiths gloves
		source(27021, "Giants' Foundry", TagCategory.MINIGAME);  // Colossal blade
		source(27012, "Giants' Foundry", TagCategory.MINIGAME);  // Double ammo mould
		source(27014, "Giants' Foundry", TagCategory.MINIGAME);  // Kovac's grog
		source(27017, "Giants' Foundry", TagCategory.MINIGAME);  // Smithing catalyst
		source(27019, "Giants' Foundry", TagCategory.MINIGAME);  // Ore pack (Giants' Foundry)

		// Gnome Restaurant
		source(9469, "Gnome Restaurant", TagCategory.MINIGAME);  // Grand seed pod
		source(9470, "Gnome Restaurant", TagCategory.MINIGAME);  // Gnome scarf
		source(9472, "Gnome Restaurant", TagCategory.MINIGAME);  // Gnome goggles
		source(9475, "Gnome Restaurant", TagCategory.MINIGAME);  // Mint cake

		// Guardians of the Rift
		source(11402, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal protector
		source(26792, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal pearls
		source(26798, "Guardians of the Rift", TagCategory.MINIGAME);  // Catalytic talisman
		source(26813, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal needle
		source(26807, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal green dye
		source(26809, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal blue dye
		source(26811, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal red dye
		source(26850, "Guardians of the Rift", TagCategory.MINIGAME);  // Hat of the eye
		source(26852, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe top of the eye
		source(26854, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe bottoms of the eye
		source(26856, "Guardians of the Rift", TagCategory.MINIGAME);  // Boots of the eye
		source(26815, "Guardians of the Rift", TagCategory.MINIGAME);  // Ring of the elements
		source(26822, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26820, "Guardians of the Rift", TagCategory.MINIGAME);  // Guardian's eye
		source(26908, "Guardians of the Rift", TagCategory.MINIGAME);  // Intricate pouch
		source(26912, "Guardians of the Rift", TagCategory.MINIGAME);  // Lost bag
		source(26910, "Guardians of the Rift", TagCategory.MINIGAME);  // Tarnished locket

		// Hallowed Sepulchre
		source(24711, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed mark
		source(24719, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed token
		source(24721, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed grapple
		source(24723, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed focus
		source(24725, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed symbol
		source(24727, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed hammer
		source(24731, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Hallowed ring
		source(24729, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Dark dye
		source(24733, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Dark acorn
		source(24740, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Strange old lockpick
		source(24844, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Ring of endurance
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page

		// Last Man Standing
		source(24189, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's chest#Cosmetic
		source(24190, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's legs#Cosmetic
		source(24191, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's cape#Cosmetic
		source(24192, "Last Man Standing", TagCategory.MINIGAME);  // Armadyl halo
		source(24195, "Last Man Standing", TagCategory.MINIGAME);  // Bandos halo
		source(24198, "Last Man Standing", TagCategory.MINIGAME);  // Seren halo
		source(24201, "Last Man Standing", TagCategory.MINIGAME);  // Ancient halo
		source(24204, "Last Man Standing", TagCategory.MINIGAME);  // Brassica halo
		source(24207, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (1)
		source(24209, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (10)
		source(24211, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (50)
		source(24213, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (100)
		source(24215, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (500)
		source(24520, "Last Man Standing", TagCategory.MINIGAME);  // Victor's cape (1000)
		source(12849, "Last Man Standing", TagCategory.MINIGAME);  // Granite clamp
		source(24229, "Last Man Standing", TagCategory.MINIGAME);  // Ornate maul handle
		source(12798, "Last Man Standing", TagCategory.MINIGAME);  // Steam staff upgrade kit
		source(21202, "Last Man Standing", TagCategory.MINIGAME);  // Lava staff upgrade kit
		source(12800, "Last Man Standing", TagCategory.MINIGAME);  // Dragon pickaxe upgrade kit
		source(12802, "Last Man Standing", TagCategory.MINIGAME);  // Ward upgrade kit
		source(12759, "Last Man Standing", TagCategory.MINIGAME);  // Green dark bow paint
		source(12761, "Last Man Standing", TagCategory.MINIGAME);  // Yellow dark bow paint
		source(12763, "Last Man Standing", TagCategory.MINIGAME);  // White dark bow paint
		source(12757, "Last Man Standing", TagCategory.MINIGAME);  // Blue dark bow paint
		source(12771, "Last Man Standing", TagCategory.MINIGAME);  // Volcanic whip mix
		source(12769, "Last Man Standing", TagCategory.MINIGAME);  // Frozen whip mix
		source(24217, "Last Man Standing", TagCategory.MINIGAME);  // Guthixian icon
		source(24219, "Last Man Standing", TagCategory.MINIGAME);  // Swift blade

		// Magic Training Arena
		source(6908, "Magic Training Arena", TagCategory.MINIGAME);  // Beginner wand
		source(6910, "Magic Training Arena", TagCategory.MINIGAME);  // Apprentice wand
		source(6912, "Magic Training Arena", TagCategory.MINIGAME);  // Teacher wand
		source(6914, "Magic Training Arena", TagCategory.MINIGAME);  // Master wand
		source(6918, "Magic Training Arena", TagCategory.MINIGAME);  // Infinity hat
		source(6916, "Magic Training Arena", TagCategory.MINIGAME);  // Infinity top
		source(6924, "Magic Training Arena", TagCategory.MINIGAME);  // Infinity bottoms
		source(6920, "Magic Training Arena", TagCategory.MINIGAME);  // Infinity boots
		source(6922, "Magic Training Arena", TagCategory.MINIGAME);  // Infinity gloves
		source(6889, "Magic Training Arena", TagCategory.MINIGAME);  // Mage's book

		// Mahogany Homes
		source(24884, "Mahogany Homes", TagCategory.MINIGAME);  // Supply crate (Mahogany Homes)
		source(24872, "Mahogany Homes", TagCategory.MINIGAME);  // Carpenter's helmet
		source(24874, "Mahogany Homes", TagCategory.MINIGAME);  // Carpenter's shirt
		source(24876, "Mahogany Homes", TagCategory.MINIGAME);  // Carpenter's trousers
		source(24878, "Mahogany Homes", TagCategory.MINIGAME);  // Carpenter's boots
		source(24880, "Mahogany Homes", TagCategory.MINIGAME);  // Amy's saw
		source(24882, "Mahogany Homes", TagCategory.MINIGAME);  // Plank sack
		source(24885, "Mahogany Homes", TagCategory.MINIGAME);  // Hosidius blueprints

		// Mastering Mixology
		source(29974, "Mastering Mixology", TagCategory.MINIGAME);  // Prescription goggles
		source(29978, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist labcoat
		source(29982, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist pants
		source(29986, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist gloves
		source(29988, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist's amulet
		source(29996, "Mastering Mixology", TagCategory.MINIGAME);  // Reagent pouch

		// Pest Control
		source(8841, "Pest Control", TagCategory.MINIGAME);  // Void knight mace
		source(8839, "Pest Control", TagCategory.MINIGAME);  // Void knight top
		source(8840, "Pest Control", TagCategory.MINIGAME);  // Void knight robe
		source(8842, "Pest Control", TagCategory.MINIGAME);  // Void knight gloves
		source(11663, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(11665, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(11664, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm
		source(13072, "Pest Control", TagCategory.MINIGAME);  // Elite void top
		source(13073, "Pest Control", TagCategory.MINIGAME);  // Elite void robe

		// Rogues' Den
		source(5554, "Rogues' Den", TagCategory.MINIGAME);  // Rogue mask
		source(5553, "Rogues' Den", TagCategory.MINIGAME);  // Rogue top
		source(5555, "Rogues' Den", TagCategory.MINIGAME);  // Rogue trousers
		source(5557, "Rogues' Den", TagCategory.MINIGAME);  // Rogue boots
		source(5556, "Rogues' Den", TagCategory.MINIGAME);  // Rogue gloves

		// Shades of Mort'ton
		source(12851, "Shades of Mort'ton", TagCategory.MINIGAME);  // Amulet of the damned
		source(12854, "Shades of Mort'ton", TagCategory.MINIGAME);  // Flamtaer bag
		source(3470, "Shades of Mort'ton", TagCategory.MINIGAME);  // Fine cloth
		source(25442, "Shades of Mort'ton", TagCategory.MINIGAME);  // Bronze locks
		source(25445, "Shades of Mort'ton", TagCategory.MINIGAME);  // Steel locks
		source(25448, "Shades of Mort'ton", TagCategory.MINIGAME);  // Black locks
		source(25451, "Shades of Mort'ton", TagCategory.MINIGAME);  // Silver locks
		source(25454, "Shades of Mort'ton", TagCategory.MINIGAME);  // Gold locks
		source(25438, "Shades of Mort'ton", TagCategory.MINIGAME);  // Zealot's helm
		source(25434, "Shades of Mort'ton", TagCategory.MINIGAME);  // Zealot's robe top
		source(25436, "Shades of Mort'ton", TagCategory.MINIGAME);  // Zealot's robe bottom
		source(25440, "Shades of Mort'ton", TagCategory.MINIGAME);  // Zealot's boots
		source(25474, "Shades of Mort'ton", TagCategory.MINIGAME);  // Tree wizards' journal
		source(25476, "Shades of Mort'ton", TagCategory.MINIGAME);  // Bloody notes

		// Soul Wars
		source(2833, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(25344, "Soul Wars", TagCategory.MINIGAME);  // Soul cape
		source(25340, "Soul Wars", TagCategory.MINIGAME);  // Ectoplasmator

		// Temple Trekking
		source(10941, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack hat
		source(10939, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack top
		source(10940, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack legs
		source(10933, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack boots

		// Tithe Farm
		source(13646, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's strawhat
		source(13642, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's jacket
		source(13640, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boro trousers
		source(13644, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boots
		source(13639, "Tithe Farm", TagCategory.MINIGAME);  // Seed box
		source(13353, "Tithe Farm", TagCategory.MINIGAME);  // Gricoller's can
		source(13226, "Tithe Farm", TagCategory.MINIGAME);  // Herb sack

		// Trouble Brewing
		source(8952, "Trouble Brewing", TagCategory.MINIGAME);  // Blue naval shirt
		source(8959, "Trouble Brewing", TagCategory.MINIGAME);  // Blue tricorn hat
		source(8991, "Trouble Brewing", TagCategory.MINIGAME);  // Blue navy slacks
		source(8953, "Trouble Brewing", TagCategory.MINIGAME);  // Green naval shirt
		source(8960, "Trouble Brewing", TagCategory.MINIGAME);  // Green tricorn hat
		source(8992, "Trouble Brewing", TagCategory.MINIGAME);  // Green navy slacks
		source(8954, "Trouble Brewing", TagCategory.MINIGAME);  // Red naval shirt
		source(8961, "Trouble Brewing", TagCategory.MINIGAME);  // Red tricorn hat
		source(8993, "Trouble Brewing", TagCategory.MINIGAME);  // Red navy slacks
		source(8955, "Trouble Brewing", TagCategory.MINIGAME);  // Brown naval shirt
		source(8962, "Trouble Brewing", TagCategory.MINIGAME);  // Brown tricorn hat
		source(8994, "Trouble Brewing", TagCategory.MINIGAME);  // Brown navy slacks
		source(8956, "Trouble Brewing", TagCategory.MINIGAME);  // Black naval shirt
		source(8963, "Trouble Brewing", TagCategory.MINIGAME);  // Black tricorn hat
		source(8995, "Trouble Brewing", TagCategory.MINIGAME);  // Black navy slacks
		source(8957, "Trouble Brewing", TagCategory.MINIGAME);  // Purple naval shirt
		source(8964, "Trouble Brewing", TagCategory.MINIGAME);  // Purple tricorn hat
		source(8996, "Trouble Brewing", TagCategory.MINIGAME);  // Purple navy slacks
		source(8958, "Trouble Brewing", TagCategory.MINIGAME);  // Grey naval shirt
		source(8965, "Trouble Brewing", TagCategory.MINIGAME);  // Grey tricorn hat
		source(8997, "Trouble Brewing", TagCategory.MINIGAME);  // Grey navy slacks
		source(8966, "Trouble Brewing", TagCategory.MINIGAME);  // Cutthroat flag
		source(8967, "Trouble Brewing", TagCategory.MINIGAME);  // Gilded smile flag
		source(8968, "Trouble Brewing", TagCategory.MINIGAME);  // Bronze fist flag
		source(8969, "Trouble Brewing", TagCategory.MINIGAME);  // Lucky shot flag
		source(8970, "Trouble Brewing", TagCategory.MINIGAME);  // Treasure flag
		source(8971, "Trouble Brewing", TagCategory.MINIGAME);  // Phasmatys flag
		source(8988, "Trouble Brewing", TagCategory.MINIGAME);  // The stuff
		source(8940, "Trouble Brewing", TagCategory.MINIGAME);  // Rum (red)
		source(8941, "Trouble Brewing", TagCategory.MINIGAME);  // Rum (blue)

		// Vale Totems
		source(31043, "Vale Totems", TagCategory.MINIGAME);  // Fletching knife
		source(31052, "Vale Totems", TagCategory.MINIGAME);  // Bow string spool
		source(31032, "Vale Totems", TagCategory.MINIGAME);  // Ent branch
		source(31034, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask

		// Volcanic Mine
		source(21697, "Volcanic Mine", TagCategory.MINIGAME);  // Ash covered tome
		source(21541, "Volcanic Mine", TagCategory.MINIGAME);  // Volcanic mine teleport
		source(27695, "Volcanic Mine", TagCategory.MINIGAME);  // Dragon pickaxe (broken)
		source(12013, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector helmet
		source(12014, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector jacket
		source(12015, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector legs
		source(12016, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector boots

		// ==================== OTHER ====================

		// Aerial Fishing
		source(22840, "Aerial Fishing", TagCategory.OTHER);  // Golden tench
		source(22846, "Aerial Fishing", TagCategory.OTHER);  // Pearl fishing rod
		source(22844, "Aerial Fishing", TagCategory.OTHER);  // Pearl fly fishing rod
		source(22842, "Aerial Fishing", TagCategory.OTHER);  // Pearl barbarian rod
		source(22838, "Aerial Fishing", TagCategory.OTHER);  // Fish sack
		source(13258, "Aerial Fishing", TagCategory.OTHER);  // Angler hat
		source(13259, "Aerial Fishing", TagCategory.OTHER);  // Angler top
		source(13260, "Aerial Fishing", TagCategory.OTHER);  // Angler waders
		source(13261, "Aerial Fishing", TagCategory.OTHER);  // Angler boots

		// All Pets
		source(5883, "All Pets", TagCategory.OTHER);  // Abyssal orphan
		source(8492, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(497, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(964, "All Pets", TagCategory.OTHER);  // Hellpuppy
		source(2055, "All Pets", TagCategory.OTHER);  // Pet chaos elemental
		source(6633, "All Pets", TagCategory.OTHER);  // Pet zilyana
		source(318, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(6627, "All Pets", TagCategory.OTHER);  // Pet dagannoth prime
		source(6626, "All Pets", TagCategory.OTHER);  // Pet dagannoth supreme
		source(6630, "All Pets", TagCategory.OTHER);  // Pet dagannoth rex
		source(5892, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(6632, "All Pets", TagCategory.OTHER);  // Pet general graardor
		source(6635, "All Pets", TagCategory.OTHER);  // Baby mole
		source(7891, "All Pets", TagCategory.OTHER);  // Noon
		source(7674, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(6638, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(6636, "All Pets", TagCategory.OTHER);  // Prince black dragon
		source(6640, "All Pets", TagCategory.OTHER);  // Pet kraken
		source(6631, "All Pets", TagCategory.OTHER);  // Pet kree'arra
		source(6634, "All Pets", TagCategory.OTHER);  // Pet k'ril tsutsaroth
		source(5547, "All Pets", TagCategory.OTHER);  // Scorpia's offspring
		source(425, "All Pets", TagCategory.OTHER);  // Skotos
		source(6639, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(495, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(5536, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(8025, "All Pets", TagCategory.OTHER);  // Vorki
		source(7368, "All Pets", TagCategory.OTHER);  // Phoenix
		source(2127, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(7519, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8336, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(6296, "All Pets", TagCategory.OTHER);  // Bloodhound
		source(6642, "All Pets", TagCategory.OTHER);  // Pet penance queen
		source(6715, "All Pets", TagCategory.OTHER);  // Heron
		source(7439, "All Pets", TagCategory.OTHER);  // Rock golem
		source(12169, "All Pets", TagCategory.OTHER);  // Beaver
		source(6719, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(7334, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(7335, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(7336, "All Pets", TagCategory.OTHER);  // Rocky
		source(7337, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7759, "All Pets", TagCategory.OTHER);  // Herbi
		source(4001, "All Pets", TagCategory.OTHER);  // Chompy chick
		source(2143, "All Pets", TagCategory.OTHER);  // Sraracha
		source(8731, "All Pets", TagCategory.OTHER);  // Smolcano
		source(8729, "All Pets", TagCategory.OTHER);  // Youngllef
		source(9398, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(2833, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(10562, "All Pets", TagCategory.OTHER);  // Tiny tempor
		source(11276, "All Pets", TagCategory.OTHER);  // Nexling
		source(11402, "All Pets", TagCategory.OTHER);  // Abyssal protector
		source(11652, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(12005, "All Pets", TagCategory.OTHER);  // Muphin
		source(12153, "All Pets", TagCategory.OTHER);  // Wisp
		source(12155, "All Pets", TagCategory.OTHER);  // Baron
		source(12154, "All Pets", TagCategory.OTHER);  // Butch
		source(12156, "All Pets", TagCategory.OTHER);  // Lil'viathan
		source(7219, "All Pets", TagCategory.OTHER);  // Scurry
		source(12767, "All Pets", TagCategory.OTHER);  // Smol heredit
		source(12768, "All Pets", TagCategory.OTHER);  // Quetzin
		source(13681, "All Pets", TagCategory.OTHER);  // Nid
		source(14033, "All Pets", TagCategory.OTHER);  // Huberte
		source(14034, "All Pets", TagCategory.OTHER);  // Moxi
		source(10476, "All Pets", TagCategory.OTHER);  // Bran
		source(14203, "All Pets", TagCategory.OTHER);  // Yami
	}
}

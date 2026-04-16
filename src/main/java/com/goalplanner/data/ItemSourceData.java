package com.goalplanner.data;

import com.goalplanner.model.ItemTag;
import com.goalplanner.model.TagCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps item IDs to their drop sources.
 * Auto-generated from OSRS Wiki Collection Log with ALL item ID variants.
 * 3800+ entries covering inventory, noted, collection log, and follower forms.
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
		source(5884, "Abyssal Sire", TagCategory.BOSS);  // Abyssal orphan
		source(13262, "Abyssal Sire", TagCategory.BOSS);  // Abyssal orphan
		source(13273, "Abyssal Sire", TagCategory.BOSS);  // Unsired
		source(7979, "Abyssal Sire", TagCategory.BOSS);  // Abyssal head
		source(13274, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon spine
		source(13275, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon claw
		source(13276, "Abyssal Sire", TagCategory.BOSS);  // Bludgeon axon
		source(13277, "Abyssal Sire", TagCategory.BOSS);  // Jar of miasma
		source(13265, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(13267, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(13269, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(13271, "Abyssal Sire", TagCategory.BOSS);  // Abyssal dagger
		source(4151, "Abyssal Sire", TagCategory.BOSS);  // Abyssal whip

		// Alchemical Hydra
		source(8492, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8493, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8494, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8495, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8517, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8518, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8519, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(8520, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22746, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22748, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22750, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22752, "Alchemical Hydra", TagCategory.BOSS);  // Ikkle hydra
		source(22966, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's claw
		source(22988, "Alchemical Hydra", TagCategory.BOSS);  // Hydra tail
		source(22983, "Alchemical Hydra", TagCategory.BOSS);  // Hydra leather
		source(22971, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's fang
		source(22973, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's eye
		source(22969, "Alchemical Hydra", TagCategory.BOSS);  // Hydra's heart
		source(22804, "Alchemical Hydra", TagCategory.BOSS);  // Dragon knife
		source(22806, "Alchemical Hydra", TagCategory.BOSS);  // Dragon knife
		source(22808, "Alchemical Hydra", TagCategory.BOSS);  // Dragon knife
		source(22810, "Alchemical Hydra", TagCategory.BOSS);  // Dragon knife
		source(20849, "Alchemical Hydra", TagCategory.BOSS);  // Dragon thrownaxe
		source(23064, "Alchemical Hydra", TagCategory.BOSS);  // Jar of chemicals
		source(23077, "Alchemical Hydra", TagCategory.BOSS);  // Alchemical hydra heads

		// Amoxliatl
		source(14034, "Amoxliatl", TagCategory.BOSS);  // Moxi
		source(14046, "Amoxliatl", TagCategory.BOSS);  // Moxi
		source(30154, "Amoxliatl", TagCategory.BOSS);  // Moxi
		source(29889, "Amoxliatl", TagCategory.BOSS);  // Glacial temotli
		source(29895, "Amoxliatl", TagCategory.BOSS);  // Frozen tear

		// Araxxor
		source(13681, "Araxxor", TagCategory.BOSS);  // Nid
		source(13682, "Araxxor", TagCategory.BOSS);  // Nid
		source(13683, "Araxxor", TagCategory.BOSS);  // Nid
		source(13684, "Araxxor", TagCategory.BOSS);  // Nid
		source(29836, "Araxxor", TagCategory.BOSS);  // Nid
		source(29838, "Araxxor", TagCategory.BOSS);  // Nid
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
		source(4928, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4929, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4930, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4931, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4932, "Barrows Chests", TagCategory.BOSS);  // Karil's coif
		source(4736, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4940, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4941, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4942, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4943, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4944, "Barrows Chests", TagCategory.BOSS);  // Karil's leathertop
		source(4738, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4946, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4947, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4948, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4949, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4950, "Barrows Chests", TagCategory.BOSS);  // Karil's leatherskirt
		source(4734, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4934, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4935, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4936, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4937, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4938, "Barrows Chests", TagCategory.BOSS);  // Karil's crossbow
		source(4708, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4856, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4857, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4858, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4859, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4860, "Barrows Chests", TagCategory.BOSS);  // Ahrim's hood
		source(4712, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4868, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4869, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4870, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4871, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4872, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robetop
		source(4714, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4874, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4875, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4876, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4877, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4878, "Barrows Chests", TagCategory.BOSS);  // Ahrim's robeskirt
		source(4710, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4862, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4863, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4864, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4865, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4866, "Barrows Chests", TagCategory.BOSS);  // Ahrim's staff
		source(4716, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4880, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4881, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4882, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4883, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4884, "Barrows Chests", TagCategory.BOSS);  // Dharok's helm
		source(4720, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4892, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4893, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4894, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4895, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4896, "Barrows Chests", TagCategory.BOSS);  // Dharok's platebody
		source(4722, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4898, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4899, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4900, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4901, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4902, "Barrows Chests", TagCategory.BOSS);  // Dharok's platelegs
		source(4718, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4886, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4887, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4888, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4889, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4890, "Barrows Chests", TagCategory.BOSS);  // Dharok's greataxe
		source(4724, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4904, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4905, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4906, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4907, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4908, "Barrows Chests", TagCategory.BOSS);  // Guthan's helm
		source(4728, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4916, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4917, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4918, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4919, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4920, "Barrows Chests", TagCategory.BOSS);  // Guthan's platebody
		source(4730, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4922, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4923, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4924, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4925, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4926, "Barrows Chests", TagCategory.BOSS);  // Guthan's chainskirt
		source(4726, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4910, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4911, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4912, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4913, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4914, "Barrows Chests", TagCategory.BOSS);  // Guthan's warspear
		source(4745, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4952, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4953, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4954, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4955, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4956, "Barrows Chests", TagCategory.BOSS);  // Torag's helm
		source(4749, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4964, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4965, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4966, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4967, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4968, "Barrows Chests", TagCategory.BOSS);  // Torag's platebody
		source(4751, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4970, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4971, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4972, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4973, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4974, "Barrows Chests", TagCategory.BOSS);  // Torag's platelegs
		source(4747, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4958, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4959, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4960, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4961, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4962, "Barrows Chests", TagCategory.BOSS);  // Torag's hammers
		source(4753, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4976, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4977, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4978, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4979, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4980, "Barrows Chests", TagCategory.BOSS);  // Verac's helm
		source(4757, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4988, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4989, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4990, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4991, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4992, "Barrows Chests", TagCategory.BOSS);  // Verac's brassard
		source(4759, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4994, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4995, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4996, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4997, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4998, "Barrows Chests", TagCategory.BOSS);  // Verac's plateskirt
		source(4755, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4982, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4983, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4984, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4985, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4986, "Barrows Chests", TagCategory.BOSS);  // Verac's flail
		source(4740, "Barrows Chests", TagCategory.BOSS);  // Bolt rack

		// Brutus
		source(15631, "Brutus", TagCategory.BOSS);  // Beef
		source(15633, "Brutus", TagCategory.BOSS);  // Beef
		source(33124, "Brutus", TagCategory.BOSS);  // Beef
		source(33101, "Brutus", TagCategory.BOSS);  // Mooleta
		source(33093, "Brutus", TagCategory.BOSS);  // Cow slippers
		source(33096, "Brutus", TagCategory.BOSS);  // Cow slippers
		source(33097, "Brutus", TagCategory.BOSS);  // Cow slippers
		source(33098, "Brutus", TagCategory.BOSS);  // Cow slippers

		// Bryophyta
		source(22372, "Bryophyta", TagCategory.BOSS);  // Bryophyta's essence

		// Callisto and Artio
		source(497, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(5558, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(11982, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(11986, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(13178, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(27649, "Callisto and Artio", TagCategory.BOSS);  // Callisto cub
		source(12603, "Callisto and Artio", TagCategory.BOSS);  // Tyrannical ring
		source(11920, "Callisto and Artio", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Callisto and Artio", TagCategory.BOSS);  // Dragon 2h sword
		source(27667, "Callisto and Artio", TagCategory.BOSS);  // Claws of callisto
		source(27681, "Callisto and Artio", TagCategory.BOSS);  // Voidwaker hilt

		// Cerberus
		source(964, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(3099, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(13247, "Cerberus", TagCategory.BOSS);  // Hellpuppy
		source(13227, "Cerberus", TagCategory.BOSS);  // Eternal crystal
		source(13229, "Cerberus", TagCategory.BOSS);  // Pegasian crystal
		source(13231, "Cerberus", TagCategory.BOSS);  // Primordial crystal
		source(13245, "Cerberus", TagCategory.BOSS);  // Jar of souls
		source(13233, "Cerberus", TagCategory.BOSS);  // Smouldering stone
		source(13249, "Cerberus", TagCategory.BOSS);  // Key master teleport

		// Chaos Elemental
		source(2055, "Chaos Elemental", TagCategory.BOSS);  // Pet chaos elemental
		source(5907, "Chaos Elemental", TagCategory.BOSS);  // Pet chaos elemental
		source(11995, "Chaos Elemental", TagCategory.BOSS);  // Pet chaos elemental
		source(11920, "Chaos Elemental", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Chaos Elemental", TagCategory.BOSS);  // Dragon 2h sword

		// Chaos Fanatic
		source(2055, "Chaos Fanatic", TagCategory.BOSS);  // Pet chaos elemental
		source(5907, "Chaos Fanatic", TagCategory.BOSS);  // Pet chaos elemental
		source(11995, "Chaos Fanatic", TagCategory.BOSS);  // Pet chaos elemental
		source(11928, "Chaos Fanatic", TagCategory.BOSS);  // Odium shard 1
		source(11931, "Chaos Fanatic", TagCategory.BOSS);  // Malediction shard 1

		// Commander Zilyana
		source(6633, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana
		source(6646, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana
		source(12651, "Commander Zilyana", TagCategory.BOSS);  // Pet zilyana
		source(11785, "Commander Zilyana", TagCategory.BOSS);  // Armadyl crossbow
		source(11814, "Commander Zilyana", TagCategory.BOSS);  // Saradomin hilt
		source(11838, "Commander Zilyana", TagCategory.BOSS);  // Saradomin sword
		source(13256, "Commander Zilyana", TagCategory.BOSS);  // Saradomin's light
		source(11818, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "Commander Zilyana", TagCategory.BOSS);  // Godsword shard 3

		// Corporeal Beast
		source(318, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(388, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(8008, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(8010, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(12816, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
		source(22318, "Corporeal Beast", TagCategory.BOSS);  // Pet dark core
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
		source(6629, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth prime
		source(12644, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth prime
		source(6626, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth supreme
		source(6628, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth supreme
		source(12643, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth supreme
		source(6630, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth rex
		source(6641, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth rex
		source(12645, "Dagannoth Kings", TagCategory.BOSS);  // Pet dagannoth rex
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
		source(14519, "Doom of Mokhaiotl", TagCategory.BOSS);  // Dom
		source(14785, "Doom of Mokhaiotl", TagCategory.BOSS);  // Dom
		source(31130, "Doom of Mokhaiotl", TagCategory.BOSS);  // Dom
		source(31088, "Doom of Mokhaiotl", TagCategory.BOSS);  // Avernic treads
		source(31109, "Doom of Mokhaiotl", TagCategory.BOSS);  // Mokhaiotl cloth
		source(31099, "Doom of Mokhaiotl", TagCategory.BOSS);  // Mokhaiotl waystone
		source(31111, "Doom of Mokhaiotl", TagCategory.BOSS);  // Demon tear

		// Duke Sucellus
		source(12155, "Duke Sucellus", TagCategory.BOSS);  // Baron
		source(12159, "Duke Sucellus", TagCategory.BOSS);  // Baron
		source(28250, "Duke Sucellus", TagCategory.BOSS);  // Baron
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
		source(5893, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(10620, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(10625, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(13225, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(25519, "The Fight Caves", TagCategory.BOSS);  // Tzrek-jad
		source(6570, "The Fight Caves", TagCategory.BOSS);  // Fire cape
		source(20445, "The Fight Caves", TagCategory.BOSS);  // Fire cape
		source(24223, "The Fight Caves", TagCategory.BOSS);  // Fire cape

		// Fortis Colosseum
		source(12767, "Fortis Colosseum", TagCategory.BOSS);  // Smol heredit
		source(12857, "Fortis Colosseum", TagCategory.BOSS);  // Smol heredit
		source(28960, "Fortis Colosseum", TagCategory.BOSS);  // Smol heredit
		source(28936, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic cuirass
		source(28939, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic chausses
		source(28933, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire fanatic helm
		source(28942, "Fortis Colosseum", TagCategory.BOSS);  // Echo crystal
		source(28924, "Fortis Colosseum", TagCategory.BOSS);  // Sunfire splinters
		source(6571, "Fortis Colosseum", TagCategory.BOSS);  // Uncut onyx

		// The Gauntlet
		source(8729, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(8730, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(8737, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(8738, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(23757, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(23759, "The Gauntlet", TagCategory.BOSS);  // Youngllef
		source(23956, "The Gauntlet", TagCategory.BOSS);  // Crystal armour seed
		source(4207, "The Gauntlet", TagCategory.BOSS);  // Crystal weapon seed
		source(25859, "The Gauntlet", TagCategory.BOSS);  // Enhanced crystal weapon seed
		source(23859, "The Gauntlet", TagCategory.BOSS);  // Gauntlet cape

		// General Graardor
		source(6632, "General Graardor", TagCategory.BOSS);  // Pet general graardor
		source(6644, "General Graardor", TagCategory.BOSS);  // Pet general graardor
		source(12650, "General Graardor", TagCategory.BOSS);  // Pet general graardor
		source(11832, "General Graardor", TagCategory.BOSS);  // Bandos chestplate
		source(11834, "General Graardor", TagCategory.BOSS);  // Bandos tassets
		source(11836, "General Graardor", TagCategory.BOSS);  // Bandos boots
		source(11812, "General Graardor", TagCategory.BOSS);  // Bandos hilt
		source(11818, "General Graardor", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "General Graardor", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "General Graardor", TagCategory.BOSS);  // Godsword shard 3

		// Giant Mole
		source(6635, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(6651, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(10650, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(10651, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(12646, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(25613, "Giant Mole", TagCategory.BOSS);  // Baby mole
		source(7418, "Giant Mole", TagCategory.BOSS);  // Mole skin
		source(7416, "Giant Mole", TagCategory.BOSS);  // Mole claw

		// Grotesque Guardians
		source(7890, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(7891, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(7892, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(7893, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(21748, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(21750, "Grotesque Guardians", TagCategory.BOSS);  // Noon
		source(21730, "Grotesque Guardians", TagCategory.BOSS);  // Black tourmaline core
		source(21736, "Grotesque Guardians", TagCategory.BOSS);  // Granite gloves
		source(21739, "Grotesque Guardians", TagCategory.BOSS);  // Granite ring
		source(21742, "Grotesque Guardians", TagCategory.BOSS);  // Granite hammer
		source(21745, "Grotesque Guardians", TagCategory.BOSS);  // Jar of stone
		source(21726, "Grotesque Guardians", TagCategory.BOSS);  // Granite dust

		// Hespori
		source(22994, "Hespori", TagCategory.BOSS);  // Bottomless compost bucket
		source(22997, "Hespori", TagCategory.BOSS);  // Bottomless compost bucket
		source(22883, "Hespori", TagCategory.BOSS);  // Iasor seed
		source(22885, "Hespori", TagCategory.BOSS);  // Kronos seed
		source(22881, "Hespori", TagCategory.BOSS);  // Attas seed

		// The Hueycoatl
		source(14033, "The Hueycoatl", TagCategory.BOSS);  // Huberte
		source(14045, "The Hueycoatl", TagCategory.BOSS);  // Huberte
		source(30152, "The Hueycoatl", TagCategory.BOSS);  // Huberte
		source(30070, "The Hueycoatl", TagCategory.BOSS);  // Dragon hunter wand
		source(30068, "The Hueycoatl", TagCategory.BOSS);  // Soiled page
		source(30085, "The Hueycoatl", TagCategory.BOSS);  // Hueycoatl hide
		source(30088, "The Hueycoatl", TagCategory.BOSS);  // Huasca seed

		// The Inferno
		source(7674, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(7675, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(8009, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(8011, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(21291, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(22319, "The Inferno", TagCategory.BOSS);  // Jal-nib-rek
		source(21287, "The Inferno", TagCategory.BOSS);  // Infernal cape
		source(21295, "The Inferno", TagCategory.BOSS);  // Infernal cape
		source(24224, "The Inferno", TagCategory.BOSS);  // Infernal cape

		// Kalphite Queen
		source(6637, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(6638, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(6653, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(6654, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(12647, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(12654, "Kalphite Queen", TagCategory.BOSS);  // Kalphite princess
		source(7981, "Kalphite Queen", TagCategory.BOSS);  // Kq head
		source(12885, "Kalphite Queen", TagCategory.BOSS);  // Jar of sand
		source(7158, "Kalphite Queen", TagCategory.BOSS);  // Dragon 2h sword
		source(3140, "Kalphite Queen", TagCategory.BOSS);  // Dragon chainbody
		source(11920, "Kalphite Queen", TagCategory.BOSS);  // Dragon pickaxe

		// King Black Dragon
		source(6636, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon
		source(6652, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon
		source(12653, "King Black Dragon", TagCategory.BOSS);  // Prince black dragon
		source(7980, "King Black Dragon", TagCategory.BOSS);  // Kbd heads
		source(11920, "King Black Dragon", TagCategory.BOSS);  // Dragon pickaxe
		source(11286, "King Black Dragon", TagCategory.BOSS);  // Draconic visage

		// Kraken
		source(6640, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(6656, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(12655, "Kraken", TagCategory.BOSS);  // Pet kraken
		source(12004, "Kraken", TagCategory.BOSS);  // Kraken tentacle
		source(11905, "Kraken", TagCategory.BOSS);  // Trident of the seas
		source(11907, "Kraken", TagCategory.BOSS);  // Trident of the seas
		source(11908, "Kraken", TagCategory.BOSS);  // Trident of the seas
		source(12007, "Kraken", TagCategory.BOSS);  // Jar of dirt

		// Kree'arra
		source(6631, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra
		source(6643, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra
		source(12649, "Kree'arra", TagCategory.BOSS);  // Pet kree'arra
		source(11826, "Kree'arra", TagCategory.BOSS);  // Armadyl helmet
		source(11828, "Kree'arra", TagCategory.BOSS);  // Armadyl chestplate
		source(11830, "Kree'arra", TagCategory.BOSS);  // Armadyl chainskirt
		source(11810, "Kree'arra", TagCategory.BOSS);  // Armadyl hilt
		source(11818, "Kree'arra", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "Kree'arra", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "Kree'arra", TagCategory.BOSS);  // Godsword shard 3

		// K'ril Tsutsaroth
		source(6634, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth
		source(6647, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth
		source(12652, "K'ril Tsutsaroth", TagCategory.BOSS);  // Pet k'ril tsutsaroth
		source(11791, "K'ril Tsutsaroth", TagCategory.BOSS);  // Staff of the dead
		source(11824, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorakian spear
		source(11787, "K'ril Tsutsaroth", TagCategory.BOSS);  // Steam battlestaff
		source(11816, "K'ril Tsutsaroth", TagCategory.BOSS);  // Zamorak hilt
		source(11818, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 1
		source(11820, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 2
		source(11822, "K'ril Tsutsaroth", TagCategory.BOSS);  // Godsword shard 3

		// The Leviathan
		source(12156, "The Leviathan", TagCategory.BOSS);  // Lil'viathan
		source(12160, "The Leviathan", TagCategory.BOSS);  // Lil'viathan
		source(28252, "The Leviathan", TagCategory.BOSS);  // Lil'viathan
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
		source(29031, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon chestplate
		source(29049, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon chestplate
		source(29007, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon tassets
		source(29033, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon tassets
		source(29052, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon tassets
		source(29010, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon helm
		source(29035, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon helm
		source(29055, "Moons of Peril", TagCategory.BOSS);  // Eclipse moon helm
		source(29000, "Moons of Peril", TagCategory.BOSS);  // Eclipse atlatl
		source(29013, "Moons of Peril", TagCategory.BOSS);  // Blue moon chestplate
		source(29037, "Moons of Peril", TagCategory.BOSS);  // Blue moon chestplate
		source(29058, "Moons of Peril", TagCategory.BOSS);  // Blue moon chestplate
		source(29016, "Moons of Peril", TagCategory.BOSS);  // Blue moon tassets
		source(29039, "Moons of Peril", TagCategory.BOSS);  // Blue moon tassets
		source(29061, "Moons of Peril", TagCategory.BOSS);  // Blue moon tassets
		source(29019, "Moons of Peril", TagCategory.BOSS);  // Blue moon helm
		source(29041, "Moons of Peril", TagCategory.BOSS);  // Blue moon helm
		source(29064, "Moons of Peril", TagCategory.BOSS);  // Blue moon helm
		source(28988, "Moons of Peril", TagCategory.BOSS);  // Blue moon spear
		source(29022, "Moons of Peril", TagCategory.BOSS);  // Blood moon chestplate
		source(29043, "Moons of Peril", TagCategory.BOSS);  // Blood moon chestplate
		source(29067, "Moons of Peril", TagCategory.BOSS);  // Blood moon chestplate
		source(29025, "Moons of Peril", TagCategory.BOSS);  // Blood moon tassets
		source(29045, "Moons of Peril", TagCategory.BOSS);  // Blood moon tassets
		source(29070, "Moons of Peril", TagCategory.BOSS);  // Blood moon tassets
		source(29028, "Moons of Peril", TagCategory.BOSS);  // Blood moon helm
		source(29047, "Moons of Peril", TagCategory.BOSS);  // Blood moon helm
		source(29073, "Moons of Peril", TagCategory.BOSS);  // Blood moon helm
		source(28997, "Moons of Peril", TagCategory.BOSS);  // Dual macuahuitl
		source(28991, "Moons of Peril", TagCategory.BOSS);  // Atlatl dart

		// Nex
		source(11276, "Nex", TagCategory.BOSS);  // Nexling
		source(11277, "Nex", TagCategory.BOSS);  // Nexling
		source(26348, "Nex", TagCategory.BOSS);  // Nexling
		source(26370, "Nex", TagCategory.BOSS);  // Ancient hilt
		source(26372, "Nex", TagCategory.BOSS);  // Nihil horn
		source(26235, "Nex", TagCategory.BOSS);  // Zaryte vambraces
		source(26231, "Nex", TagCategory.BOSS);  // Nihil shard

		// The Nightmare
		source(8183, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(8541, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(9398, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(9399, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(24491, "The Nightmare", TagCategory.BOSS);  // Little nightmare
		source(25836, "The Nightmare", TagCategory.BOSS);  // Little nightmare
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
		source(12006, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(12007, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(12014, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(12015, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(12016, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(27590, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(27592, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(27593, "Phantom Muspah", TagCategory.BOSS);  // Muphin
		source(27614, "Phantom Muspah", TagCategory.BOSS);  // Venator shard
		source(27627, "Phantom Muspah", TagCategory.BOSS);  // Ancient icon
		source(27643, "Phantom Muspah", TagCategory.BOSS);  // Charged ice
		source(27622, "Phantom Muspah", TagCategory.BOSS);  // Frozen cache
		source(27616, "Phantom Muspah", TagCategory.BOSS);  // Ancient essence

		// Royal Titans
		source(10476, "Royal Titans", TagCategory.BOSS);  // Bran
		source(12592, "Royal Titans", TagCategory.BOSS);  // Bran
		source(12593, "Royal Titans", TagCategory.BOSS);  // Bran
		source(12595, "Royal Titans", TagCategory.BOSS);  // Bran
		source(30622, "Royal Titans", TagCategory.BOSS);  // Bran
		source(30624, "Royal Titans", TagCategory.BOSS);  // Bran
		source(30626, "Royal Titans", TagCategory.BOSS);  // Deadeye prayer scroll
		source(30627, "Royal Titans", TagCategory.BOSS);  // Mystic vigour prayer scroll
		source(30628, "Royal Titans", TagCategory.BOSS);  // Ice element staff crown
		source(30631, "Royal Titans", TagCategory.BOSS);  // Fire element staff crown
		source(30640, "Royal Titans", TagCategory.BOSS);  // Desiccated page

		// Sarachnis
		source(2143, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(2144, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(11157, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(11158, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(11159, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(11160, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(23495, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(25842, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(25843, "Sarachnis", TagCategory.BOSS);  // Sraracha
		source(23525, "Sarachnis", TagCategory.BOSS);  // Jar of eyes
		source(23528, "Sarachnis", TagCategory.BOSS);  // Sarachnis cudgel
		source(33133, "Sarachnis", TagCategory.BOSS);  // Pristine spider silk

		// Scorpia
		source(5547, "Scorpia", TagCategory.BOSS);  // Scorpia's offspring
		source(5561, "Scorpia", TagCategory.BOSS);  // Scorpia's offspring
		source(13181, "Scorpia", TagCategory.BOSS);  // Scorpia's offspring
		source(11930, "Scorpia", TagCategory.BOSS);  // Odium shard 3
		source(11933, "Scorpia", TagCategory.BOSS);  // Malediction shard 3
		source(7158, "Scorpia", TagCategory.BOSS);  // Dragon 2h sword

		// Scurrius
		source(7219, "Scurrius", TagCategory.BOSS);  // Scurry
		source(7616, "Scurrius", TagCategory.BOSS);  // Scurry
		source(28801, "Scurrius", TagCategory.BOSS);  // Scurry
		source(28798, "Scurrius", TagCategory.BOSS);  // Scurrius' spine

		// Shellbane Gryphon
		source(14931, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(14932, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(15059, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(15060, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(31285, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(31287, "Shellbane Gryphon", TagCategory.BOSS);  // Gull (pet)
		source(32921, "Shellbane Gryphon", TagCategory.BOSS);  // Jar of feathers
		source(31245, "Shellbane Gryphon", TagCategory.BOSS);  // Belle's folly (tarnished)
		source(31235, "Shellbane Gryphon", TagCategory.BOSS);  // Gryphon feather

		// Skotizo
		source(425, "Skotizo", TagCategory.BOSS);  // Skotos
		source(7671, "Skotizo", TagCategory.BOSS);  // Skotos
		source(21273, "Skotizo", TagCategory.BOSS);  // Skotos
		source(19701, "Skotizo", TagCategory.BOSS);  // Jar of darkness
		source(21275, "Skotizo", TagCategory.BOSS);  // Dark claw
		source(19685, "Skotizo", TagCategory.BOSS);  // Dark totem
		source(6571, "Skotizo", TagCategory.BOSS);  // Uncut onyx
		source(19677, "Skotizo", TagCategory.BOSS);  // Ancient shard

		// Tempoross — categorized as MINIGAME despite being in the boss tab
		// of the in-game collection log; community treats it as a skilling
		// minigame, not a boss.
		source(10562, "Tempoross", TagCategory.MINIGAME);  // Tiny tempor
		source(10637, "Tempoross", TagCategory.MINIGAME);  // Tiny tempor
		source(25602, "Tempoross", TagCategory.MINIGAME);  // Tiny tempor
		source(25559, "Tempoross", TagCategory.MINIGAME);  // Big harpoonfish
		source(25592, "Tempoross", TagCategory.MINIGAME);  // Spirit angler headband
		source(25594, "Tempoross", TagCategory.MINIGAME);  // Spirit angler top
		source(25596, "Tempoross", TagCategory.MINIGAME);  // Spirit angler waders
		source(25598, "Tempoross", TagCategory.MINIGAME);  // Spirit angler boots
		source(25578, "Tempoross", TagCategory.MINIGAME);  // Soaked page
		source(25580, "Tempoross", TagCategory.MINIGAME);  // Tackle box
		source(25582, "Tempoross", TagCategory.MINIGAME);  // Fish barrel
		source(25584, "Tempoross", TagCategory.MINIGAME);  // Fish barrel
		source(21028, "Tempoross", TagCategory.MINIGAME);  // Dragon harpoon
		source(25588, "Tempoross", TagCategory.MINIGAME);  // Spirit flakes

		// Thermonuclear smoke devil
		source(6639, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(6655, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(8482, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(8483, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(12648, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(22663, "Thermonuclear smoke devil", TagCategory.BOSS);  // Pet smoke devil
		source(12002, "Thermonuclear smoke devil", TagCategory.BOSS);  // Occult necklace
		source(11998, "Thermonuclear smoke devil", TagCategory.BOSS);  // Smoke battlestaff
		source(3140, "Thermonuclear smoke devil", TagCategory.BOSS);  // Dragon chainbody
		source(25524, "Thermonuclear smoke devil", TagCategory.BOSS);  // Jar of smoke

		// Vardorvis
		source(12154, "Vardorvis", TagCategory.BOSS);  // Butch
		source(12158, "Vardorvis", TagCategory.BOSS);  // Butch
		source(28248, "Vardorvis", TagCategory.BOSS);  // Butch
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
		source(5557, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(11981, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(11985, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(13177, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(27648, "Venenatis and Spindel", TagCategory.BOSS);  // Venenatis spiderling
		source(12605, "Venenatis and Spindel", TagCategory.BOSS);  // Treasonous ring
		source(11920, "Venenatis and Spindel", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Venenatis and Spindel", TagCategory.BOSS);  // Dragon 2h sword
		source(27670, "Venenatis and Spindel", TagCategory.BOSS);  // Fangs of venenatis
		source(27687, "Venenatis and Spindel", TagCategory.BOSS);  // Voidwaker gem

		// Vet'ion and Calvar'ion
		source(5536, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(5537, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(5559, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(5560, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(11983, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(11984, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(11987, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(11988, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(13179, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(13180, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(27650, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(27651, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Vet'ion jr.
		source(12601, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Ring of the gods
		source(11920, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Dragon pickaxe
		source(7158, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Dragon 2h sword
		source(27673, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Skull of vet'ion
		source(27684, "Vet'ion and Calvar'ion", TagCategory.BOSS);  // Voidwaker blade

		// Vorkath
		source(8025, "Vorkath", TagCategory.BOSS);  // Vorki
		source(8029, "Vorkath", TagCategory.BOSS);  // Vorki
		source(21992, "Vorkath", TagCategory.BOSS);  // Vorki
		source(21907, "Vorkath", TagCategory.BOSS);  // Vorkath's head
		source(11286, "Vorkath", TagCategory.BOSS);  // Draconic visage
		source(22006, "Vorkath", TagCategory.BOSS);  // Skeletal visage
		source(22106, "Vorkath", TagCategory.BOSS);  // Jar of decay
		source(22111, "Vorkath", TagCategory.BOSS);  // Dragonbone necklace

		// The Whisperer
		source(12153, "The Whisperer", TagCategory.BOSS);  // Wisp
		source(12157, "The Whisperer", TagCategory.BOSS);  // Wisp
		source(28246, "The Whisperer", TagCategory.BOSS);  // Wisp
		source(28323, "The Whisperer", TagCategory.BOSS);  // Siren's staff
		source(26241, "The Whisperer", TagCategory.BOSS);  // Virtus mask
		source(26243, "The Whisperer", TagCategory.BOSS);  // Virtus robe top
		source(26245, "The Whisperer", TagCategory.BOSS);  // Virtus robe bottom
		source(28279, "The Whisperer", TagCategory.BOSS);  // Bellator vestige
		source(28272, "The Whisperer", TagCategory.BOSS);  // Shadow quartz
		source(28331, "The Whisperer", TagCategory.BOSS);  // Sirenic tablet
		source(28276, "The Whisperer", TagCategory.BOSS);  // Chromium ingot
		source(28334, "The Whisperer", TagCategory.BOSS);  // Awakener's orb

		// Wintertodt — categorized as MINIGAME despite being in the boss tab
		// of the in-game collection log; community treats it as a skilling
		// minigame, not a boss.
		source(3077, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3078, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3079, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3080, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3081, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3082, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3083, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(3084, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(7368, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(7370, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(20693, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(24483, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(24484, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(24485, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(24486, "Wintertodt", TagCategory.MINIGAME);  // Phoenix
		source(20718, "Wintertodt", TagCategory.MINIGAME);  // Burnt page
		source(20714, "Wintertodt", TagCategory.MINIGAME);  // Tome of fire (charged)
		source(20716, "Wintertodt", TagCategory.MINIGAME);  // Tome of fire (empty)
		source(20704, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer garb
		source(20708, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer hood
		source(20706, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer robe
		source(20710, "Wintertodt", TagCategory.MINIGAME);  // Pyromancer boots
		source(20712, "Wintertodt", TagCategory.MINIGAME);  // Warm gloves
		source(20720, "Wintertodt", TagCategory.MINIGAME);  // Bruma torch
		source(6739, "Wintertodt", TagCategory.MINIGAME);  // Dragon axe

		// Yama
		source(14203, "Yama", TagCategory.BOSS);  // Yami
		source(14204, "Yama", TagCategory.BOSS);  // Yami
		source(30888, "Yama", TagCategory.BOSS);  // Yami
		source(30775, "Yama", TagCategory.BOSS);  // Chasm teleport scroll
		source(30765, "Yama", TagCategory.BOSS);  // Oathplate shards
		source(30750, "Yama", TagCategory.BOSS);  // Oathplate helm
		source(30753, "Yama", TagCategory.BOSS);  // Oathplate chest
		source(30756, "Yama", TagCategory.BOSS);  // Oathplate legs
		source(30759, "Yama", TagCategory.BOSS);  // Soulflame horn
		source(30806, "Yama", TagCategory.BOSS);  // Rite of vile transference
		source(30763, "Yama", TagCategory.BOSS);  // Forgotten lockbox
		source(30803, "Yama", TagCategory.BOSS);  // Dossier
		source(30805, "Yama", TagCategory.BOSS);  // Dossier

		// Zalcano
		source(8731, "Zalcano", TagCategory.BOSS);  // Smolcano
		source(8739, "Zalcano", TagCategory.BOSS);  // Smolcano
		source(23760, "Zalcano", TagCategory.BOSS);  // Smolcano
		source(23953, "Zalcano", TagCategory.BOSS);  // Crystal tool seed
		source(23908, "Zalcano", TagCategory.BOSS);  // Zalcano shard
		source(6571, "Zalcano", TagCategory.BOSS);  // Uncut onyx

		// Zulrah
		source(2127, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(2128, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(2129, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(2130, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(2131, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(2132, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(12921, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(12939, "Zulrah", TagCategory.BOSS);  // Pet snakeling
		source(12940, "Zulrah", TagCategory.BOSS);  // Pet snakeling
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
		source(7520, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8196, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8197, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8198, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8199, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8200, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8201, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8202, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8203, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8204, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(8205, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(9511, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(9512, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(9513, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(9514, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(20851, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22376, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22378, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22380, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22382, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(22384, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(24656, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
		source(24658, "Chambers of Xeric", TagCategory.RAID);  // Olmlet
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
		source(8337, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10761, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10762, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10763, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10764, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10765, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10870, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10871, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10872, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10873, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(10874, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(22473, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(25748, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(25749, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(25750, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(25751, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
		source(25752, "Theatre of Blood", TagCategory.RAID);  // Lil' zik
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
		source(11653, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11812, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11813, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11840, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11841, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11842, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11843, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11844, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11845, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11846, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11847, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11848, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11849, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11850, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(11851, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27352, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27354, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27382, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27383, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27384, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27385, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27386, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
		source(27387, "Tombs of Amascut", TagCategory.RAID);  // Tumeken's guardian
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
		source(20278, "Medium Treasure Trails", TagCategory.CLUE);  // Gnomish firelighter
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
		source(10354, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(10356, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(10358, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(10360, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(10362, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(11964, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
		source(11966, "Hard Treasure Trails", TagCategory.CLUE);  // Amulet of glory (t)
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
		source(7232, "Master Treasure Trails", TagCategory.CLUE);  // Bloodhound
		source(19730, "Master Treasure Trails", TagCategory.CLUE);  // Bloodhound
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
		source(21389, "Shared Treasure Trail Rewards", TagCategory.CLUE);  // Master scroll book
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
		source(6674, "Barbarian Assault", TagCategory.MINIGAME);  // Pet penance queen
		source(12703, "Barbarian Assault", TagCategory.MINIGAME);  // Pet penance queen
		source(10548, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter hat
		source(20507, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter hat
		source(24173, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter hat
		source(10550, "Barbarian Assault", TagCategory.MINIGAME);  // Ranger hat
		source(20509, "Barbarian Assault", TagCategory.MINIGAME);  // Ranger hat
		source(24174, "Barbarian Assault", TagCategory.MINIGAME);  // Ranger hat
		source(10549, "Barbarian Assault", TagCategory.MINIGAME);  // Runner hat
		source(24531, "Barbarian Assault", TagCategory.MINIGAME);  // Runner hat
		source(24533, "Barbarian Assault", TagCategory.MINIGAME);  // Runner hat
		source(10547, "Barbarian Assault", TagCategory.MINIGAME);  // Healer hat
		source(20511, "Barbarian Assault", TagCategory.MINIGAME);  // Healer hat
		source(24172, "Barbarian Assault", TagCategory.MINIGAME);  // Healer hat
		source(10551, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter torso
		source(20513, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter torso
		source(24175, "Barbarian Assault", TagCategory.MINIGAME);  // Fighter torso
		source(10555, "Barbarian Assault", TagCategory.MINIGAME);  // Penance skirt
		source(20515, "Barbarian Assault", TagCategory.MINIGAME);  // Penance skirt
		source(24176, "Barbarian Assault", TagCategory.MINIGAME);  // Penance skirt
		source(10552, "Barbarian Assault", TagCategory.MINIGAME);  // Runner boots
		source(10553, "Barbarian Assault", TagCategory.MINIGAME);  // Penance gloves
		source(10554, "Barbarian Assault", TagCategory.MINIGAME);  // Penance gloves
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
		source(21063, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful hood (Agility Arena)
		source(21067, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful top (Agility Arena)
		source(21069, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful top (Agility Arena)
		source(21070, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful legs (Agility Arena)
		source(21072, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful legs (Agility Arena)
		source(21073, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful gloves (Agility Arena)
		source(21075, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful gloves (Agility Arena)
		source(21076, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful boots (Agility Arena)
		source(21078, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful boots (Agility Arena)
		source(21064, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful cape (Agility Arena)
		source(21066, "Brimhaven Agility Arena", TagCategory.MINIGAME);  // Graceful cape (Agility Arena)

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
		source(20489, "Castle Wars", TagCategory.MINIGAME);  // Decorative helm (gold)
		source(24160, "Castle Wars", TagCategory.MINIGAME);  // Decorative helm (gold)
		source(25157, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (gold)
		source(25174, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (gold)
		source(25176, "Castle Wars", TagCategory.MINIGAME);  // Decorative full helm (gold)
		source(4509, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platebody)
		source(20485, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platebody)
		source(24158, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platebody)
		source(4508, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (gold)
		source(20483, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (gold)
		source(24157, "Castle Wars", TagCategory.MINIGAME);  // Decorative sword (gold)
		source(4512, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (gold)
		source(20491, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (gold)
		source(24161, "Castle Wars", TagCategory.MINIGAME);  // Decorative shield (gold)
		source(4510, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platelegs)
		source(20487, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platelegs)
		source(24159, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold platelegs)
		source(11895, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold plateskirt)
		source(20493, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold plateskirt)
		source(24162, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (gold plateskirt)
		source(25155, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (gold)
		source(25171, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (gold)
		source(25173, "Castle Wars", TagCategory.MINIGAME);  // Decorative boots (gold)
		source(4513, "Castle Wars", TagCategory.MINIGAME);  // Castlewars hood (Saradomin)
		source(4514, "Castle Wars", TagCategory.MINIGAME);  // Castlewars cloak (Saradomin)
		source(4515, "Castle Wars", TagCategory.MINIGAME);  // Castlewars hood (Zamorak)
		source(4516, "Castle Wars", TagCategory.MINIGAME);  // Castlewars cloak (Zamorak)
		source(4037, "Castle Wars", TagCategory.MINIGAME);  // Saradomin banner
		source(11891, "Castle Wars", TagCategory.MINIGAME);  // Saradomin banner
		source(4039, "Castle Wars", TagCategory.MINIGAME);  // Zamorak banner
		source(11892, "Castle Wars", TagCategory.MINIGAME);  // Zamorak banner
		source(11898, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic hat)
		source(20499, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic hat)
		source(24165, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic hat)
		source(11896, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic top)
		source(20495, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic top)
		source(24163, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic top)
		source(11897, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic legs)
		source(20497, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic legs)
		source(24164, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (magic legs)
		source(11899, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged top)
		source(20501, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged top)
		source(24166, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged top)
		source(11900, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged legs)
		source(20503, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged legs)
		source(24167, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (ranged legs)
		source(11901, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (quiver)
		source(20505, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (quiver)
		source(24168, "Castle Wars", TagCategory.MINIGAME);  // Decorative armour (quiver)
		source(12637, "Castle Wars", TagCategory.MINIGAME);  // Saradomin halo
		source(20537, "Castle Wars", TagCategory.MINIGAME);  // Saradomin halo
		source(24169, "Castle Wars", TagCategory.MINIGAME);  // Saradomin halo
		source(12638, "Castle Wars", TagCategory.MINIGAME);  // Zamorak halo
		source(20539, "Castle Wars", TagCategory.MINIGAME);  // Zamorak halo
		source(24170, "Castle Wars", TagCategory.MINIGAME);  // Zamorak halo
		source(12639, "Castle Wars", TagCategory.MINIGAME);  // Guthix halo
		source(20541, "Castle Wars", TagCategory.MINIGAME);  // Guthix halo
		source(24171, "Castle Wars", TagCategory.MINIGAME);  // Guthix halo

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
		source(11429, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal protector
		source(26901, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal protector
		source(26792, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal pearls
		source(26798, "Guardians of the Rift", TagCategory.MINIGAME);  // Catalytic talisman
		source(26813, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal needle
		source(26807, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal green dye
		source(26809, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal blue dye
		source(26811, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal red dye
		source(26850, "Guardians of the Rift", TagCategory.MINIGAME);  // Hat of the eye
		source(26858, "Guardians of the Rift", TagCategory.MINIGAME);  // Hat of the eye
		source(26864, "Guardians of the Rift", TagCategory.MINIGAME);  // Hat of the eye
		source(26870, "Guardians of the Rift", TagCategory.MINIGAME);  // Hat of the eye
		source(26852, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe top of the eye
		source(26860, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe top of the eye
		source(26866, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe top of the eye
		source(26872, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe top of the eye
		source(26854, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe bottoms of the eye
		source(26862, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe bottoms of the eye
		source(26868, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe bottoms of the eye
		source(26874, "Guardians of the Rift", TagCategory.MINIGAME);  // Robe bottoms of the eye
		source(26856, "Guardians of the Rift", TagCategory.MINIGAME);  // Boots of the eye
		source(26815, "Guardians of the Rift", TagCategory.MINIGAME);  // Ring of the elements
		source(26818, "Guardians of the Rift", TagCategory.MINIGAME);  // Ring of the elements
		source(26822, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26824, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26826, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26828, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26830, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26832, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26834, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26836, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26838, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26840, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26842, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26844, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26846, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26848, "Guardians of the Rift", TagCategory.MINIGAME);  // Abyssal lantern
		source(26820, "Guardians of the Rift", TagCategory.MINIGAME);  // Guardian's eye
		source(26908, "Guardians of the Rift", TagCategory.MINIGAME);  // Intricate pouch
		source(26912, "Guardians of the Rift", TagCategory.MINIGAME);  // Lost bag
		source(26984, "Guardians of the Rift", TagCategory.MINIGAME);  // Lost bag
		source(26986, "Guardians of the Rift", TagCategory.MINIGAME);  // Lost bag
		source(26988, "Guardians of the Rift", TagCategory.MINIGAME);  // Lost bag
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
		source(24738, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Strange old lockpick
		source(24740, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Strange old lockpick
		source(24736, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Ring of endurance
		source(24844, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Ring of endurance
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24765, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24767, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24769, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24771, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24765, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24767, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24769, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24771, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24765, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24767, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24769, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24771, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24765, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24767, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24769, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24771, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24763, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24765, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24767, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24769, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page
		source(24771, "Hallowed Sepulchre", TagCategory.MINIGAME);  // Mysterious page

		// Last Man Standing
		source(13317, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's chest#Cosmetic
		source(24189, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's chest#Cosmetic
		source(13318, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's legs#Cosmetic
		source(24190, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's legs#Cosmetic
		source(13319, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's cape#Cosmetic
		source(24191, "Last Man Standing", TagCategory.MINIGAME);  // Deadman's cape#Cosmetic
		source(24147, "Last Man Standing", TagCategory.MINIGAME);  // Armadyl halo
		source(24192, "Last Man Standing", TagCategory.MINIGAME);  // Armadyl halo
		source(24194, "Last Man Standing", TagCategory.MINIGAME);  // Armadyl halo
		source(24149, "Last Man Standing", TagCategory.MINIGAME);  // Bandos halo
		source(24195, "Last Man Standing", TagCategory.MINIGAME);  // Bandos halo
		source(24197, "Last Man Standing", TagCategory.MINIGAME);  // Bandos halo
		source(24151, "Last Man Standing", TagCategory.MINIGAME);  // Seren halo
		source(24198, "Last Man Standing", TagCategory.MINIGAME);  // Seren halo
		source(24200, "Last Man Standing", TagCategory.MINIGAME);  // Seren halo
		source(24153, "Last Man Standing", TagCategory.MINIGAME);  // Ancient halo
		source(24201, "Last Man Standing", TagCategory.MINIGAME);  // Ancient halo
		source(24203, "Last Man Standing", TagCategory.MINIGAME);  // Ancient halo
		source(24155, "Last Man Standing", TagCategory.MINIGAME);  // Brassica halo
		source(24204, "Last Man Standing", TagCategory.MINIGAME);  // Brassica halo
		source(24206, "Last Man Standing", TagCategory.MINIGAME);  // Brassica halo
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
		source(29976, "Mastering Mixology", TagCategory.MINIGAME);  // Prescription goggles
		source(29978, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist labcoat
		source(29980, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist labcoat
		source(29982, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist pants
		source(29984, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist pants
		source(29986, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist gloves
		source(29988, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist's amulet
		source(29990, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist's amulet
		source(29992, "Mastering Mixology", TagCategory.MINIGAME);  // Alchemist's amulet
		source(29996, "Mastering Mixology", TagCategory.MINIGAME);  // Reagent pouch
		source(29998, "Mastering Mixology", TagCategory.MINIGAME);  // Reagent pouch

		// Pest Control
		source(8841, "Pest Control", TagCategory.MINIGAME);  // Void knight mace
		source(20473, "Pest Control", TagCategory.MINIGAME);  // Void knight mace
		source(24181, "Pest Control", TagCategory.MINIGAME);  // Void knight mace
		source(8839, "Pest Control", TagCategory.MINIGAME);  // Void knight top
		source(20465, "Pest Control", TagCategory.MINIGAME);  // Void knight top
		source(24177, "Pest Control", TagCategory.MINIGAME);  // Void knight top
		source(8840, "Pest Control", TagCategory.MINIGAME);  // Void knight robe
		source(20469, "Pest Control", TagCategory.MINIGAME);  // Void knight robe
		source(24179, "Pest Control", TagCategory.MINIGAME);  // Void knight robe
		source(8842, "Pest Control", TagCategory.MINIGAME);  // Void knight gloves
		source(20475, "Pest Control", TagCategory.MINIGAME);  // Void knight gloves
		source(24182, "Pest Control", TagCategory.MINIGAME);  // Void knight gloves
		source(11663, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(20477, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(24183, "Pest Control", TagCategory.MINIGAME);  // Void mage helm
		source(11665, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(20481, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(24185, "Pest Control", TagCategory.MINIGAME);  // Void melee helm
		source(11664, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm
		source(20479, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm
		source(24184, "Pest Control", TagCategory.MINIGAME);  // Void ranger helm
		source(13072, "Pest Control", TagCategory.MINIGAME);  // Elite void top
		source(20467, "Pest Control", TagCategory.MINIGAME);  // Elite void top
		source(24178, "Pest Control", TagCategory.MINIGAME);  // Elite void top
		source(13073, "Pest Control", TagCategory.MINIGAME);  // Elite void robe
		source(20471, "Pest Control", TagCategory.MINIGAME);  // Elite void robe
		source(24180, "Pest Control", TagCategory.MINIGAME);  // Elite void robe

		// Rogues' Den
		source(5554, "Rogues' Den", TagCategory.MINIGAME);  // Rogue mask
		source(5553, "Rogues' Den", TagCategory.MINIGAME);  // Rogue top
		source(5555, "Rogues' Den", TagCategory.MINIGAME);  // Rogue trousers
		source(5557, "Rogues' Den", TagCategory.MINIGAME);  // Rogue boots
		source(5556, "Rogues' Den", TagCategory.MINIGAME);  // Rogue gloves

		// Shades of Mort'ton
		source(12851, "Shades of Mort'ton", TagCategory.MINIGAME);  // Amulet of the damned
		source(12853, "Shades of Mort'ton", TagCategory.MINIGAME);  // Amulet of the damned
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
		source(3564, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(3566, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(5008, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(25348, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(25350, "Soul Wars", TagCategory.MINIGAME);  // Lil' creator
		source(25344, "Soul Wars", TagCategory.MINIGAME);  // Soul cape
		source(25346, "Soul Wars", TagCategory.MINIGAME);  // Soul cape
		source(25340, "Soul Wars", TagCategory.MINIGAME);  // Ectoplasmator

		// Temple Trekking
		source(10941, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack hat
		source(10939, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack top
		source(10940, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack legs
		source(10933, "Temple Trekking", TagCategory.MINIGAME);  // Lumberjack boots

		// Tithe Farm
		source(13646, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's strawhat
		source(13647, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's strawhat
		source(13642, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's jacket
		source(13640, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boro trousers
		source(13641, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boro trousers
		source(13644, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boots
		source(13645, "Tithe Farm", TagCategory.MINIGAME);  // Farmer's boots
		source(13639, "Tithe Farm", TagCategory.MINIGAME);  // Seed box
		source(24482, "Tithe Farm", TagCategory.MINIGAME);  // Seed box
		source(13353, "Tithe Farm", TagCategory.MINIGAME);  // Gricoller's can
		source(13226, "Tithe Farm", TagCategory.MINIGAME);  // Herb sack
		source(24478, "Tithe Farm", TagCategory.MINIGAME);  // Herb sack

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
		source(31037, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask
		source(31038, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask
		source(31039, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask
		source(31040, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask
		source(31041, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask
		source(31042, "Vale Totems", TagCategory.MINIGAME);  // Greenman mask

		// Volcanic Mine
		source(21697, "Volcanic Mine", TagCategory.MINIGAME);  // Ash covered tome
		source(21541, "Volcanic Mine", TagCategory.MINIGAME);  // Volcanic mine teleport
		source(27695, "Volcanic Mine", TagCategory.MINIGAME);  // Dragon pickaxe (broken)
		source(12013, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector helmet
		source(29472, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector helmet
		source(12014, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector jacket
		source(29474, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector jacket
		source(12015, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector legs
		source(29476, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector legs
		source(12016, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector boots
		source(29478, "Volcanic Mine", TagCategory.MINIGAME);  // Prospector boots

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
		source(5884, "All Pets", TagCategory.OTHER);  // Abyssal orphan
		source(13262, "All Pets", TagCategory.OTHER);  // Abyssal orphan
		source(8492, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8493, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8494, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8495, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8517, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8518, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8519, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(8520, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(22746, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(22748, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(22750, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(22752, "All Pets", TagCategory.OTHER);  // Ikkle hydra
		source(497, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(5558, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(11982, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(11986, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(13178, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(27649, "All Pets", TagCategory.OTHER);  // Callisto cub
		source(964, "All Pets", TagCategory.OTHER);  // Hellpuppy
		source(3099, "All Pets", TagCategory.OTHER);  // Hellpuppy
		source(13247, "All Pets", TagCategory.OTHER);  // Hellpuppy
		source(2055, "All Pets", TagCategory.OTHER);  // Pet chaos elemental
		source(5907, "All Pets", TagCategory.OTHER);  // Pet chaos elemental
		source(11995, "All Pets", TagCategory.OTHER);  // Pet chaos elemental
		source(6633, "All Pets", TagCategory.OTHER);  // Pet zilyana
		source(6646, "All Pets", TagCategory.OTHER);  // Pet zilyana
		source(12651, "All Pets", TagCategory.OTHER);  // Pet zilyana
		source(318, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(388, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(8008, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(8010, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(12816, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(22318, "All Pets", TagCategory.OTHER);  // Pet dark core
		source(6627, "All Pets", TagCategory.OTHER);  // Pet dagannoth prime
		source(6629, "All Pets", TagCategory.OTHER);  // Pet dagannoth prime
		source(12644, "All Pets", TagCategory.OTHER);  // Pet dagannoth prime
		source(6626, "All Pets", TagCategory.OTHER);  // Pet dagannoth supreme
		source(6628, "All Pets", TagCategory.OTHER);  // Pet dagannoth supreme
		source(12643, "All Pets", TagCategory.OTHER);  // Pet dagannoth supreme
		source(6630, "All Pets", TagCategory.OTHER);  // Pet dagannoth rex
		source(6641, "All Pets", TagCategory.OTHER);  // Pet dagannoth rex
		source(12645, "All Pets", TagCategory.OTHER);  // Pet dagannoth rex
		source(5892, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(5893, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(10620, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(10625, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(13225, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(25519, "All Pets", TagCategory.OTHER);  // Tzrek-jad
		source(6632, "All Pets", TagCategory.OTHER);  // Pet general graardor
		source(6644, "All Pets", TagCategory.OTHER);  // Pet general graardor
		source(12650, "All Pets", TagCategory.OTHER);  // Pet general graardor
		source(6635, "All Pets", TagCategory.OTHER);  // Baby mole
		source(6651, "All Pets", TagCategory.OTHER);  // Baby mole
		source(10650, "All Pets", TagCategory.OTHER);  // Baby mole
		source(10651, "All Pets", TagCategory.OTHER);  // Baby mole
		source(12646, "All Pets", TagCategory.OTHER);  // Baby mole
		source(25613, "All Pets", TagCategory.OTHER);  // Baby mole
		source(7890, "All Pets", TagCategory.OTHER);  // Noon
		source(7891, "All Pets", TagCategory.OTHER);  // Noon
		source(7892, "All Pets", TagCategory.OTHER);  // Noon
		source(7893, "All Pets", TagCategory.OTHER);  // Noon
		source(21748, "All Pets", TagCategory.OTHER);  // Noon
		source(21750, "All Pets", TagCategory.OTHER);  // Noon
		source(7674, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(7675, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(8009, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(8011, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(21291, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(22319, "All Pets", TagCategory.OTHER);  // Jal-nib-rek
		source(6637, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(6638, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(6653, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(6654, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(12647, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(12654, "All Pets", TagCategory.OTHER);  // Kalphite princess
		source(6636, "All Pets", TagCategory.OTHER);  // Prince black dragon
		source(6652, "All Pets", TagCategory.OTHER);  // Prince black dragon
		source(12653, "All Pets", TagCategory.OTHER);  // Prince black dragon
		source(6640, "All Pets", TagCategory.OTHER);  // Pet kraken
		source(6656, "All Pets", TagCategory.OTHER);  // Pet kraken
		source(12655, "All Pets", TagCategory.OTHER);  // Pet kraken
		source(6631, "All Pets", TagCategory.OTHER);  // Pet kree'arra
		source(6643, "All Pets", TagCategory.OTHER);  // Pet kree'arra
		source(12649, "All Pets", TagCategory.OTHER);  // Pet kree'arra
		source(6634, "All Pets", TagCategory.OTHER);  // Pet k'ril tsutsaroth
		source(6647, "All Pets", TagCategory.OTHER);  // Pet k'ril tsutsaroth
		source(12652, "All Pets", TagCategory.OTHER);  // Pet k'ril tsutsaroth
		source(5547, "All Pets", TagCategory.OTHER);  // Scorpia's offspring
		source(5561, "All Pets", TagCategory.OTHER);  // Scorpia's offspring
		source(13181, "All Pets", TagCategory.OTHER);  // Scorpia's offspring
		source(425, "All Pets", TagCategory.OTHER);  // Skotos
		source(7671, "All Pets", TagCategory.OTHER);  // Skotos
		source(21273, "All Pets", TagCategory.OTHER);  // Skotos
		source(6639, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(6655, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(8482, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(8483, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(12648, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(22663, "All Pets", TagCategory.OTHER);  // Pet smoke devil
		source(495, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(5557, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(11981, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(11985, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(13177, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(27648, "All Pets", TagCategory.OTHER);  // Venenatis spiderling
		source(5536, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(5537, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(5559, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(5560, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(11983, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(11984, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(11987, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(11988, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(13179, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(13180, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(27650, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(27651, "All Pets", TagCategory.OTHER);  // Vet'ion jr.
		source(8025, "All Pets", TagCategory.OTHER);  // Vorki
		source(8029, "All Pets", TagCategory.OTHER);  // Vorki
		source(21992, "All Pets", TagCategory.OTHER);  // Vorki
		source(3077, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3078, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3079, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3080, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3081, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3082, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3083, "All Pets", TagCategory.OTHER);  // Phoenix
		source(3084, "All Pets", TagCategory.OTHER);  // Phoenix
		source(7368, "All Pets", TagCategory.OTHER);  // Phoenix
		source(7370, "All Pets", TagCategory.OTHER);  // Phoenix
		source(20693, "All Pets", TagCategory.OTHER);  // Phoenix
		source(24483, "All Pets", TagCategory.OTHER);  // Phoenix
		source(24484, "All Pets", TagCategory.OTHER);  // Phoenix
		source(24485, "All Pets", TagCategory.OTHER);  // Phoenix
		source(24486, "All Pets", TagCategory.OTHER);  // Phoenix
		source(2127, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(2128, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(2129, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(2130, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(2131, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(2132, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(12921, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(12939, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(12940, "All Pets", TagCategory.OTHER);  // Pet snakeling
		source(7519, "All Pets", TagCategory.OTHER);  // Olmlet
		source(7520, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8196, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8197, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8198, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8199, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8200, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8201, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8202, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8203, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8204, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8205, "All Pets", TagCategory.OTHER);  // Olmlet
		source(9511, "All Pets", TagCategory.OTHER);  // Olmlet
		source(9512, "All Pets", TagCategory.OTHER);  // Olmlet
		source(9513, "All Pets", TagCategory.OTHER);  // Olmlet
		source(9514, "All Pets", TagCategory.OTHER);  // Olmlet
		source(20851, "All Pets", TagCategory.OTHER);  // Olmlet
		source(22376, "All Pets", TagCategory.OTHER);  // Olmlet
		source(22378, "All Pets", TagCategory.OTHER);  // Olmlet
		source(22380, "All Pets", TagCategory.OTHER);  // Olmlet
		source(22382, "All Pets", TagCategory.OTHER);  // Olmlet
		source(22384, "All Pets", TagCategory.OTHER);  // Olmlet
		source(24656, "All Pets", TagCategory.OTHER);  // Olmlet
		source(24658, "All Pets", TagCategory.OTHER);  // Olmlet
		source(8336, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(8337, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10761, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10762, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10763, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10764, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10765, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10870, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10871, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10872, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10873, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(10874, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(22473, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(25748, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(25749, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(25750, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(25751, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(25752, "All Pets", TagCategory.OTHER);  // Lil' zik
		source(6296, "All Pets", TagCategory.OTHER);  // Bloodhound
		source(7232, "All Pets", TagCategory.OTHER);  // Bloodhound
		source(19730, "All Pets", TagCategory.OTHER);  // Bloodhound
		source(6642, "All Pets", TagCategory.OTHER);  // Pet penance queen
		source(6674, "All Pets", TagCategory.OTHER);  // Pet penance queen
		source(12703, "All Pets", TagCategory.OTHER);  // Pet penance queen
		source(6715, "All Pets", TagCategory.OTHER);  // Heron
		source(6722, "All Pets", TagCategory.OTHER);  // Heron
		source(6817, "All Pets", TagCategory.OTHER);  // Heron
		source(10636, "All Pets", TagCategory.OTHER);  // Heron
		source(13320, "All Pets", TagCategory.OTHER);  // Heron
		source(25600, "All Pets", TagCategory.OTHER);  // Heron
		source(2182, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7439, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7440, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7441, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7442, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7443, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7444, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7445, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7446, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7447, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7448, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7449, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7450, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7451, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7452, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7453, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7454, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7455, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7642, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7643, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7644, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7645, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7646, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7647, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7648, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7711, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7736, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7737, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7738, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7739, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7740, "All Pets", TagCategory.OTHER);  // Rock golem
		source(7741, "All Pets", TagCategory.OTHER);  // Rock golem
		source(13321, "All Pets", TagCategory.OTHER);  // Rock golem
		source(14923, "All Pets", TagCategory.OTHER);  // Rock golem
		source(14924, "All Pets", TagCategory.OTHER);  // Rock golem
		source(14925, "All Pets", TagCategory.OTHER);  // Rock golem
		source(15051, "All Pets", TagCategory.OTHER);  // Rock golem
		source(15052, "All Pets", TagCategory.OTHER);  // Rock golem
		source(15053, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21187, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21188, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21189, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21190, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21191, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21192, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21193, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21194, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21195, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21196, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21197, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21340, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21358, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21359, "All Pets", TagCategory.OTHER);  // Rock golem
		source(21360, "All Pets", TagCategory.OTHER);  // Rock golem
		source(31276, "All Pets", TagCategory.OTHER);  // Rock golem
		source(31277, "All Pets", TagCategory.OTHER);  // Rock golem
		source(31278, "All Pets", TagCategory.OTHER);  // Rock golem
		source(12169, "All Pets", TagCategory.OTHER);  // Beaver
		source(12170, "All Pets", TagCategory.OTHER);  // Beaver
		source(12171, "All Pets", TagCategory.OTHER);  // Beaver
		source(12172, "All Pets", TagCategory.OTHER);  // Beaver
		source(12173, "All Pets", TagCategory.OTHER);  // Beaver
		source(12174, "All Pets", TagCategory.OTHER);  // Beaver
		source(12175, "All Pets", TagCategory.OTHER);  // Beaver
		source(12176, "All Pets", TagCategory.OTHER);  // Beaver
		source(12177, "All Pets", TagCategory.OTHER);  // Beaver
		source(12178, "All Pets", TagCategory.OTHER);  // Beaver
		source(12181, "All Pets", TagCategory.OTHER);  // Beaver
		source(12182, "All Pets", TagCategory.OTHER);  // Beaver
		source(12183, "All Pets", TagCategory.OTHER);  // Beaver
		source(12184, "All Pets", TagCategory.OTHER);  // Beaver
		source(12185, "All Pets", TagCategory.OTHER);  // Beaver
		source(12186, "All Pets", TagCategory.OTHER);  // Beaver
		source(12187, "All Pets", TagCategory.OTHER);  // Beaver
		source(12188, "All Pets", TagCategory.OTHER);  // Beaver
		source(12189, "All Pets", TagCategory.OTHER);  // Beaver
		source(12190, "All Pets", TagCategory.OTHER);  // Beaver
		source(13322, "All Pets", TagCategory.OTHER);  // Beaver
		source(14926, "All Pets", TagCategory.OTHER);  // Beaver
		source(14927, "All Pets", TagCategory.OTHER);  // Beaver
		source(14928, "All Pets", TagCategory.OTHER);  // Beaver
		source(14929, "All Pets", TagCategory.OTHER);  // Beaver
		source(15054, "All Pets", TagCategory.OTHER);  // Beaver
		source(15055, "All Pets", TagCategory.OTHER);  // Beaver
		source(15056, "All Pets", TagCategory.OTHER);  // Beaver
		source(15057, "All Pets", TagCategory.OTHER);  // Beaver
		source(28229, "All Pets", TagCategory.OTHER);  // Beaver
		source(28230, "All Pets", TagCategory.OTHER);  // Beaver
		source(28231, "All Pets", TagCategory.OTHER);  // Beaver
		source(28232, "All Pets", TagCategory.OTHER);  // Beaver
		source(28233, "All Pets", TagCategory.OTHER);  // Beaver
		source(28234, "All Pets", TagCategory.OTHER);  // Beaver
		source(28235, "All Pets", TagCategory.OTHER);  // Beaver
		source(28236, "All Pets", TagCategory.OTHER);  // Beaver
		source(28237, "All Pets", TagCategory.OTHER);  // Beaver
		source(31279, "All Pets", TagCategory.OTHER);  // Beaver
		source(31280, "All Pets", TagCategory.OTHER);  // Beaver
		source(31281, "All Pets", TagCategory.OTHER);  // Beaver
		source(31282, "All Pets", TagCategory.OTHER);  // Beaver
		source(6718, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6719, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6720, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6721, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6756, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6757, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6758, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6759, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13323, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13324, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13325, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13326, "All Pets", TagCategory.OTHER);  // Baby chinchompa
		source(7334, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(7351, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(9637, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(9638, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(14032, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(14044, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(20659, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(24701, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(30151, "All Pets", TagCategory.OTHER);  // Giant squirrel
		source(7335, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(7352, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9492, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9493, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9494, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9495, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9496, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9497, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9498, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9499, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9500, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(9501, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(20661, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(24555, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(24557, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(24559, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(24561, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(24563, "All Pets", TagCategory.OTHER);  // Tangleroot
		source(7336, "All Pets", TagCategory.OTHER);  // Rocky
		source(7353, "All Pets", TagCategory.OTHER);  // Rocky
		source(9850, "All Pets", TagCategory.OTHER);  // Rocky
		source(9851, "All Pets", TagCategory.OTHER);  // Rocky
		source(9852, "All Pets", TagCategory.OTHER);  // Rocky
		source(9853, "All Pets", TagCategory.OTHER);  // Rocky
		source(20663, "All Pets", TagCategory.OTHER);  // Rocky
		source(24847, "All Pets", TagCategory.OTHER);  // Rocky
		source(24849, "All Pets", TagCategory.OTHER);  // Rocky
		source(7337, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7338, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7339, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7340, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7341, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7342, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7343, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7344, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7345, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7346, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7347, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7348, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7349, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7350, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7354, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7355, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7356, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7357, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7358, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7359, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7360, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7361, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7362, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7363, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7364, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7365, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7366, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7367, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(8024, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(8028, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(11401, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(11428, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20665, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20667, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20669, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20671, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20673, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20675, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20677, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20679, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20681, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20683, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20685, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20687, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20689, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(20691, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(21990, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(26899, "All Pets", TagCategory.OTHER);  // Rift guardian
		source(7759, "All Pets", TagCategory.OTHER);  // Herbi
		source(7760, "All Pets", TagCategory.OTHER);  // Herbi
		source(21509, "All Pets", TagCategory.OTHER);  // Herbi
		source(4001, "All Pets", TagCategory.OTHER);  // Chompy chick
		source(4002, "All Pets", TagCategory.OTHER);  // Chompy chick
		source(13071, "All Pets", TagCategory.OTHER);  // Chompy chick
		source(2143, "All Pets", TagCategory.OTHER);  // Sraracha
		source(2144, "All Pets", TagCategory.OTHER);  // Sraracha
		source(11157, "All Pets", TagCategory.OTHER);  // Sraracha
		source(11158, "All Pets", TagCategory.OTHER);  // Sraracha
		source(11159, "All Pets", TagCategory.OTHER);  // Sraracha
		source(11160, "All Pets", TagCategory.OTHER);  // Sraracha
		source(23495, "All Pets", TagCategory.OTHER);  // Sraracha
		source(25842, "All Pets", TagCategory.OTHER);  // Sraracha
		source(25843, "All Pets", TagCategory.OTHER);  // Sraracha
		source(8731, "All Pets", TagCategory.OTHER);  // Smolcano
		source(8739, "All Pets", TagCategory.OTHER);  // Smolcano
		source(23760, "All Pets", TagCategory.OTHER);  // Smolcano
		source(8729, "All Pets", TagCategory.OTHER);  // Youngllef
		source(8730, "All Pets", TagCategory.OTHER);  // Youngllef
		source(8737, "All Pets", TagCategory.OTHER);  // Youngllef
		source(8738, "All Pets", TagCategory.OTHER);  // Youngllef
		source(23757, "All Pets", TagCategory.OTHER);  // Youngllef
		source(23759, "All Pets", TagCategory.OTHER);  // Youngllef
		source(8183, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(8541, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(9398, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(9399, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(24491, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(25836, "All Pets", TagCategory.OTHER);  // Little nightmare
		source(2833, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(3564, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(3566, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(5008, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(25348, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(25350, "All Pets", TagCategory.OTHER);  // Lil' creator
		source(10562, "All Pets", TagCategory.OTHER);  // Tiny tempor
		source(10637, "All Pets", TagCategory.OTHER);  // Tiny tempor
		source(25602, "All Pets", TagCategory.OTHER);  // Tiny tempor
		source(11276, "All Pets", TagCategory.OTHER);  // Nexling
		source(11277, "All Pets", TagCategory.OTHER);  // Nexling
		source(26348, "All Pets", TagCategory.OTHER);  // Nexling
		source(11402, "All Pets", TagCategory.OTHER);  // Abyssal protector
		source(11429, "All Pets", TagCategory.OTHER);  // Abyssal protector
		source(26901, "All Pets", TagCategory.OTHER);  // Abyssal protector
		source(11652, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11653, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11812, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11813, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11840, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11841, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11842, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11843, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11844, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11845, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11846, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11847, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11848, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11849, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11850, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(11851, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27352, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27354, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27382, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27383, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27384, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27385, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27386, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(27387, "All Pets", TagCategory.OTHER);  // Tumeken's guardian
		source(12005, "All Pets", TagCategory.OTHER);  // Muphin
		source(12006, "All Pets", TagCategory.OTHER);  // Muphin
		source(12007, "All Pets", TagCategory.OTHER);  // Muphin
		source(12014, "All Pets", TagCategory.OTHER);  // Muphin
		source(12015, "All Pets", TagCategory.OTHER);  // Muphin
		source(12016, "All Pets", TagCategory.OTHER);  // Muphin
		source(27590, "All Pets", TagCategory.OTHER);  // Muphin
		source(27592, "All Pets", TagCategory.OTHER);  // Muphin
		source(27593, "All Pets", TagCategory.OTHER);  // Muphin
		source(12153, "All Pets", TagCategory.OTHER);  // Wisp
		source(12157, "All Pets", TagCategory.OTHER);  // Wisp
		source(28246, "All Pets", TagCategory.OTHER);  // Wisp
		source(12155, "All Pets", TagCategory.OTHER);  // Baron
		source(12159, "All Pets", TagCategory.OTHER);  // Baron
		source(28250, "All Pets", TagCategory.OTHER);  // Baron
		source(12154, "All Pets", TagCategory.OTHER);  // Butch
		source(12158, "All Pets", TagCategory.OTHER);  // Butch
		source(28248, "All Pets", TagCategory.OTHER);  // Butch
		source(12156, "All Pets", TagCategory.OTHER);  // Lil'viathan
		source(12160, "All Pets", TagCategory.OTHER);  // Lil'viathan
		source(28252, "All Pets", TagCategory.OTHER);  // Lil'viathan
		source(7219, "All Pets", TagCategory.OTHER);  // Scurry
		source(7616, "All Pets", TagCategory.OTHER);  // Scurry
		source(28801, "All Pets", TagCategory.OTHER);  // Scurry
		source(12767, "All Pets", TagCategory.OTHER);  // Smol heredit
		source(12857, "All Pets", TagCategory.OTHER);  // Smol heredit
		source(28960, "All Pets", TagCategory.OTHER);  // Smol heredit
		source(12768, "All Pets", TagCategory.OTHER);  // Quetzin
		source(12858, "All Pets", TagCategory.OTHER);  // Quetzin
		source(28962, "All Pets", TagCategory.OTHER);  // Quetzin
		source(13681, "All Pets", TagCategory.OTHER);  // Nid
		source(13682, "All Pets", TagCategory.OTHER);  // Nid
		source(13683, "All Pets", TagCategory.OTHER);  // Nid
		source(13684, "All Pets", TagCategory.OTHER);  // Nid
		source(29836, "All Pets", TagCategory.OTHER);  // Nid
		source(29838, "All Pets", TagCategory.OTHER);  // Nid
		source(14033, "All Pets", TagCategory.OTHER);  // Huberte
		source(14045, "All Pets", TagCategory.OTHER);  // Huberte
		source(30152, "All Pets", TagCategory.OTHER);  // Huberte
		source(14034, "All Pets", TagCategory.OTHER);  // Moxi
		source(14046, "All Pets", TagCategory.OTHER);  // Moxi
		source(30154, "All Pets", TagCategory.OTHER);  // Moxi
		source(10476, "All Pets", TagCategory.OTHER);  // Bran
		source(12592, "All Pets", TagCategory.OTHER);  // Bran
		source(12593, "All Pets", TagCategory.OTHER);  // Bran
		source(12595, "All Pets", TagCategory.OTHER);  // Bran
		source(30622, "All Pets", TagCategory.OTHER);  // Bran
		source(30624, "All Pets", TagCategory.OTHER);  // Bran
		source(14203, "All Pets", TagCategory.OTHER);  // Yami
		source(14204, "All Pets", TagCategory.OTHER);  // Yami
		source(30888, "All Pets", TagCategory.OTHER);  // Yami
		source(14519, "All Pets", TagCategory.OTHER);  // Dom
		source(14785, "All Pets", TagCategory.OTHER);  // Dom
		source(31130, "All Pets", TagCategory.OTHER);  // Dom
		source(14930, "All Pets", TagCategory.OTHER);  // Soup
		source(15058, "All Pets", TagCategory.OTHER);  // Soup
		source(31283, "All Pets", TagCategory.OTHER);  // Soup
		source(14931, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(14932, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(15059, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(15060, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(31285, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(31287, "All Pets", TagCategory.OTHER);  // Gull (pet)
		source(15631, "All Pets", TagCategory.OTHER);  // Beef
		source(15633, "All Pets", TagCategory.OTHER);  // Beef
		source(33124, "All Pets", TagCategory.OTHER);  // Beef

		// Boat Paints
		source(32087, "Boat Paints", TagCategory.OTHER);  // Barracuda paint
		source(32090, "Boat Paints", TagCategory.OTHER);  // Shark paint
		source(32093, "Boat Paints", TagCategory.OTHER);  // Inky paint
		source(32096, "Boat Paints", TagCategory.OTHER);  // Angler's paint
		source(32099, "Boat Paints", TagCategory.OTHER);  // Salvor's paint
		source(32102, "Boat Paints", TagCategory.OTHER);  // Armadylean paint
		source(32104, "Boat Paints", TagCategory.OTHER);  // Zamorakian paint
		source(32106, "Boat Paints", TagCategory.OTHER);  // Guthixian paint
		source(32108, "Boat Paints", TagCategory.OTHER);  // Saradominist paint
		source(32110, "Boat Paints", TagCategory.OTHER);  // Merchant's paint
		source(32113, "Boat Paints", TagCategory.OTHER);  // Sandy paint

		// Camdozaal
		source(25625, "Camdozaal", TagCategory.OTHER);  // Barronite mace
		source(25641, "Camdozaal", TagCategory.OTHER);  // Barronite mace
		source(25643, "Camdozaal", TagCategory.OTHER);  // Barronite mace
		source(25635, "Camdozaal", TagCategory.OTHER);  // Barronite head
		source(25637, "Camdozaal", TagCategory.OTHER);  // Barronite handle
		source(25639, "Camdozaal", TagCategory.OTHER);  // Barronite guard
		source(25686, "Camdozaal", TagCategory.OTHER);  // Ancient globe
		source(25688, "Camdozaal", TagCategory.OTHER);  // Ancient ledger
		source(25690, "Camdozaal", TagCategory.OTHER);  // Ancient astroscope
		source(25692, "Camdozaal", TagCategory.OTHER);  // Ancient treatise
		source(25694, "Camdozaal", TagCategory.OTHER);  // Ancient carcanet
		source(25633, "Camdozaal", TagCategory.OTHER);  // Imcando hammer
		source(25644, "Camdozaal", TagCategory.OTHER);  // Imcando hammer

		// Champion's Challenge
		source(6798, "Champion's Challenge", TagCategory.OTHER);  // Earth warrior champion scroll
		source(6799, "Champion's Challenge", TagCategory.OTHER);  // Ghoul champion scroll
		source(6800, "Champion's Challenge", TagCategory.OTHER);  // Giant champion scroll
		source(6801, "Champion's Challenge", TagCategory.OTHER);  // Goblin champion scroll
		source(6802, "Champion's Challenge", TagCategory.OTHER);  // Hobgoblin champion scroll
		source(6803, "Champion's Challenge", TagCategory.OTHER);  // Imp champion scroll
		source(6804, "Champion's Challenge", TagCategory.OTHER);  // Jogre champion scroll
		source(6805, "Champion's Challenge", TagCategory.OTHER);  // Lesser demon champion scroll
		source(6806, "Champion's Challenge", TagCategory.OTHER);  // Skeleton champion scroll
		source(6807, "Champion's Challenge", TagCategory.OTHER);  // Zombie champion scroll
		source(21439, "Champion's Challenge", TagCategory.OTHER);  // Champion's cape

		// Chompy Bird Hunting
		source(4001, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy chick
		source(4002, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy chick
		source(13071, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy chick
		source(2978, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre bowman)
		source(2979, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (bowman)
		source(2980, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre yeoman)
		source(2981, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (yeoman)
		source(2982, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre marksman)
		source(2983, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (marksman)
		source(2984, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre woodsman)
		source(2985, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (woodsman)
		source(2986, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre forester)
		source(2987, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (forester)
		source(2988, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre bowmaster)
		source(2989, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (bowmaster)
		source(2990, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre expert)
		source(2991, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (expert)
		source(2992, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (ogre dragon archer)
		source(2993, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (dragon archer)
		source(2994, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (expert ogre dragon archer)
		source(2995, "Chompy Bird Hunting", TagCategory.OTHER);  // Chompy bird hat (expert dragon archer)

		// Colossal Wyrm Agility
		source(30040, "Colossal Wyrm Agility", TagCategory.OTHER);  // Colossal wyrm teleport scroll
		source(30042, "Colossal Wyrm Agility", TagCategory.OTHER);  // Calcified acorn
		source(30045, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful hood (Varlamore)
		source(30047, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful hood (Varlamore)
		source(30051, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful top (Varlamore)
		source(30053, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful top (Varlamore)
		source(30054, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful legs (Varlamore)
		source(30056, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful legs (Varlamore)
		source(30057, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful gloves (Varlamore)
		source(30059, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful gloves (Varlamore)
		source(30060, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful boots (Varlamore)
		source(30062, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful boots (Varlamore)
		source(30048, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful cape (Varlamore)
		source(30050, "Colossal Wyrm Agility", TagCategory.OTHER);  // Graceful cape (Varlamore)

		// Creature Creation
		source(10859, "Creature Creation", TagCategory.OTHER);  // Tea flask
		source(10877, "Creature Creation", TagCategory.OTHER);  // Plain satchel
		source(10878, "Creature Creation", TagCategory.OTHER);  // Green satchel
		source(10879, "Creature Creation", TagCategory.OTHER);  // Red satchel
		source(10880, "Creature Creation", TagCategory.OTHER);  // Black satchel
		source(10881, "Creature Creation", TagCategory.OTHER);  // Gold satchel
		source(10882, "Creature Creation", TagCategory.OTHER);  // Rune satchel

		// Cyclopes
		source(8844, "Cyclopes", TagCategory.OTHER);  // Bronze defender
		source(20449, "Cyclopes", TagCategory.OTHER);  // Bronze defender
		source(24136, "Cyclopes", TagCategory.OTHER);  // Bronze defender
		source(8845, "Cyclopes", TagCategory.OTHER);  // Iron defender
		source(20451, "Cyclopes", TagCategory.OTHER);  // Iron defender
		source(24137, "Cyclopes", TagCategory.OTHER);  // Iron defender
		source(8846, "Cyclopes", TagCategory.OTHER);  // Steel defender
		source(20453, "Cyclopes", TagCategory.OTHER);  // Steel defender
		source(24138, "Cyclopes", TagCategory.OTHER);  // Steel defender
		source(8847, "Cyclopes", TagCategory.OTHER);  // Black defender
		source(20455, "Cyclopes", TagCategory.OTHER);  // Black defender
		source(24139, "Cyclopes", TagCategory.OTHER);  // Black defender
		source(8848, "Cyclopes", TagCategory.OTHER);  // Mithril defender
		source(20457, "Cyclopes", TagCategory.OTHER);  // Mithril defender
		source(24140, "Cyclopes", TagCategory.OTHER);  // Mithril defender
		source(8849, "Cyclopes", TagCategory.OTHER);  // Adamant defender
		source(20459, "Cyclopes", TagCategory.OTHER);  // Adamant defender
		source(24141, "Cyclopes", TagCategory.OTHER);  // Adamant defender
		source(8850, "Cyclopes", TagCategory.OTHER);  // Rune defender
		source(20461, "Cyclopes", TagCategory.OTHER);  // Rune defender
		source(24142, "Cyclopes", TagCategory.OTHER);  // Rune defender
		source(12954, "Cyclopes", TagCategory.OTHER);  // Dragon defender
		source(20463, "Cyclopes", TagCategory.OTHER);  // Dragon defender
		source(24143, "Cyclopes", TagCategory.OTHER);  // Dragon defender

		// Elder Chaos Druids
		source(20517, "Elder Chaos Druids", TagCategory.OTHER);  // Elder chaos top
		source(20520, "Elder Chaos Druids", TagCategory.OTHER);  // Elder chaos robe
		source(20595, "Elder Chaos Druids", TagCategory.OTHER);  // Elder chaos hood

		// Forestry
		source(28626, "Forestry", TagCategory.OTHER);  // Fox whistle
		source(28663, "Forestry", TagCategory.OTHER);  // Golden pheasant egg
		source(10941, "Forestry", TagCategory.OTHER);  // Lumberjack hat
		source(10939, "Forestry", TagCategory.OTHER);  // Lumberjack top
		source(10940, "Forestry", TagCategory.OTHER);  // Lumberjack legs
		source(10933, "Forestry", TagCategory.OTHER);  // Lumberjack boots
		source(28173, "Forestry", TagCategory.OTHER);  // Forestry hat
		source(28169, "Forestry", TagCategory.OTHER);  // Forestry top
		source(28171, "Forestry", TagCategory.OTHER);  // Forestry legs
		source(28175, "Forestry", TagCategory.OTHER);  // Forestry boots
		source(28630, "Forestry", TagCategory.OTHER);  // Twitcher's gloves
		source(28138, "Forestry", TagCategory.OTHER);  // Funky shaped log
		source(28140, "Forestry", TagCategory.OTHER);  // Log basket
		source(28142, "Forestry", TagCategory.OTHER);  // Log basket
		source(28146, "Forestry", TagCategory.OTHER);  // Log brace
		source(28166, "Forestry", TagCategory.OTHER);  // Clothes pouch blueprint
		source(28613, "Forestry", TagCategory.OTHER);  // Cape pouch
		source(28177, "Forestry", TagCategory.OTHER);  // Felling axe handle
		source(28620, "Forestry", TagCategory.OTHER);  // Pheasant hat
		source(28622, "Forestry", TagCategory.OTHER);  // Pheasant legs
		source(28618, "Forestry", TagCategory.OTHER);  // Pheasant boots
		source(28616, "Forestry", TagCategory.OTHER);  // Pheasant cape
		source(28655, "Forestry", TagCategory.OTHER);  // Petal garland
		source(28674, "Forestry", TagCategory.OTHER);  // Sturdy beehive parts

		// Fossil Island Notes
		source(21664, "Fossil Island Notes", TagCategory.OTHER);  // Scribbled note
		source(21666, "Fossil Island Notes", TagCategory.OTHER);  // Partial note
		source(21668, "Fossil Island Notes", TagCategory.OTHER);  // Ancient note
		source(21670, "Fossil Island Notes", TagCategory.OTHER);  // Ancient writings
		source(21672, "Fossil Island Notes", TagCategory.OTHER);  // Experimental note
		source(21674, "Fossil Island Notes", TagCategory.OTHER);  // Paragraph of text
		source(21676, "Fossil Island Notes", TagCategory.OTHER);  // Musty smelling note
		source(21678, "Fossil Island Notes", TagCategory.OTHER);  // Hastily scrawled note
		source(21680, "Fossil Island Notes", TagCategory.OTHER);  // Old writing
		source(21682, "Fossil Island Notes", TagCategory.OTHER);  // Short note

		// Glough's Experiments
		source(19529, "Glough's Experiments", TagCategory.OTHER);  // Zenyte shard
		source(19586, "Glough's Experiments", TagCategory.OTHER);  // Light frame
		source(19589, "Glough's Experiments", TagCategory.OTHER);  // Heavy frame
		source(19592, "Glough's Experiments", TagCategory.OTHER);  // Ballista limbs
		source(19610, "Glough's Experiments", TagCategory.OTHER);  // Monkey tail
		source(19601, "Glough's Experiments", TagCategory.OTHER);  // Ballista spring

		// Hunter Guild
		source(12768, "Hunter Guild", TagCategory.OTHER);  // Quetzin
		source(12858, "Hunter Guild", TagCategory.OTHER);  // Quetzin
		source(28962, "Hunter Guild", TagCategory.OTHER);  // Quetzin
		source(29309, "Hunter Guild", TagCategory.OTHER);  // Huntsman's kit
		source(29263, "Hunter Guild", TagCategory.OTHER);  // Guild hunter headwear
		source(29265, "Hunter Guild", TagCategory.OTHER);  // Guild hunter top
		source(29267, "Hunter Guild", TagCategory.OTHER);  // Guild hunter legs
		source(29269, "Hunter Guild", TagCategory.OTHER);  // Guild hunter boots

		// Lost Schematics
		source(32401, "Lost Schematics", TagCategory.OTHER);  // Salvaging station schematic
		source(32402, "Lost Schematics", TagCategory.OTHER);  // Gale catcher schematic
		source(32403, "Lost Schematics", TagCategory.OTHER);  // Eternal brazier schematic
		source(32405, "Lost Schematics", TagCategory.OTHER);  // Rosewood cargo hold schematic
		source(32407, "Lost Schematics", TagCategory.OTHER);  // Rosewood hull schematic
		source(32408, "Lost Schematics", TagCategory.OTHER);  // Rosewood & cotton sails schematic
		source(32409, "Lost Schematics", TagCategory.OTHER);  // Dragon helm schematic
		source(32410, "Lost Schematics", TagCategory.OTHER);  // Dragon keel schematic
		source(32404, "Lost Schematics", TagCategory.OTHER);  // Dragon salvaging hook schematic
		source(32406, "Lost Schematics", TagCategory.OTHER);  // Dragon cannon schematic
		source(33143, "Lost Schematics", TagCategory.OTHER);  // Ballistic attractor schematic

		// Monkey Backpacks
		source(24862, "Monkey Backpacks", TagCategory.OTHER);  // Karamjan monkey (item)
		source(24866, "Monkey Backpacks", TagCategory.OTHER);  // Kruk jr
		source(24864, "Monkey Backpacks", TagCategory.OTHER);  // Maniacal monkey (item)
		source(24867, "Monkey Backpacks", TagCategory.OTHER);  // Princely monkey
		source(24865, "Monkey Backpacks", TagCategory.OTHER);  // Skeleton monkey (item)
		source(24863, "Monkey Backpacks", TagCategory.OTHER);  // Zombie monkey (item)

		// Motherlode Mine
		source(12019, "Motherlode Mine", TagCategory.OTHER);  // Coal bag
		source(24480, "Motherlode Mine", TagCategory.OTHER);  // Coal bag
		source(12020, "Motherlode Mine", TagCategory.OTHER);  // Gem bag
		source(24481, "Motherlode Mine", TagCategory.OTHER);  // Gem bag
		source(12013, "Motherlode Mine", TagCategory.OTHER);  // Prospector helmet
		source(29472, "Motherlode Mine", TagCategory.OTHER);  // Prospector helmet
		source(12014, "Motherlode Mine", TagCategory.OTHER);  // Prospector jacket
		source(29474, "Motherlode Mine", TagCategory.OTHER);  // Prospector jacket
		source(12015, "Motherlode Mine", TagCategory.OTHER);  // Prospector legs
		source(29476, "Motherlode Mine", TagCategory.OTHER);  // Prospector legs
		source(12016, "Motherlode Mine", TagCategory.OTHER);  // Prospector boots
		source(29478, "Motherlode Mine", TagCategory.OTHER);  // Prospector boots

		// My Notes
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11341, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11342, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11343, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11344, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11345, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11346, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11347, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11348, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11349, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11350, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11351, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11352, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11353, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11354, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11355, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11356, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11357, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11358, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11359, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11360, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11361, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11362, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11363, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11364, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11365, "My Notes", TagCategory.OTHER);  // Ancient page
		source(11366, "My Notes", TagCategory.OTHER);  // Ancient page

		// Ocean Encounters
		source(31770, "Ocean Encounters", TagCategory.OTHER);  // Tiny pearl
		source(31773, "Ocean Encounters", TagCategory.OTHER);  // Small pearl
		source(31776, "Ocean Encounters", TagCategory.OTHER);  // Shiny pearl
		source(31779, "Ocean Encounters", TagCategory.OTHER);  // Bright pearl
		source(31782, "Ocean Encounters", TagCategory.OTHER);  // Big pearl
		source(31785, "Ocean Encounters", TagCategory.OTHER);  // Huge pearl
		source(31788, "Ocean Encounters", TagCategory.OTHER);  // Enormous pearl
		source(31791, "Ocean Encounters", TagCategory.OTHER);  // Shimmering pearl
		source(31794, "Ocean Encounters", TagCategory.OTHER);  // Glistening pearl
		source(31797, "Ocean Encounters", TagCategory.OTHER);  // Brilliant pearl
		source(31800, "Ocean Encounters", TagCategory.OTHER);  // Radiant pearl

		// Random Events
		source(6654, "Random Events", TagCategory.OTHER);  // Camo top
		source(6655, "Random Events", TagCategory.OTHER);  // Camo bottoms
		source(6656, "Random Events", TagCategory.OTHER);  // Camo helmet
		source(6180, "Random Events", TagCategory.OTHER);  // Lederhosen top
		source(6181, "Random Events", TagCategory.OTHER);  // Lederhosen shorts
		source(6182, "Random Events", TagCategory.OTHER);  // Lederhosen hat
		source(7592, "Random Events", TagCategory.OTHER);  // Zombie shirt
		source(7593, "Random Events", TagCategory.OTHER);  // Zombie trousers
		source(7594, "Random Events", TagCategory.OTHER);  // Zombie mask
		source(7595, "Random Events", TagCategory.OTHER);  // Zombie gloves
		source(7596, "Random Events", TagCategory.OTHER);  // Zombie boots
		source(3057, "Random Events", TagCategory.OTHER);  // Mime mask
		source(3058, "Random Events", TagCategory.OTHER);  // Mime top
		source(3059, "Random Events", TagCategory.OTHER);  // Mime legs
		source(3060, "Random Events", TagCategory.OTHER);  // Mime gloves
		source(3061, "Random Events", TagCategory.OTHER);  // Mime boots
		source(6183, "Random Events", TagCategory.OTHER);  // Frog token
		source(20590, "Random Events", TagCategory.OTHER);  // Stale baguette
		source(25129, "Random Events", TagCategory.OTHER);  // Beekeeper's hat
		source(25131, "Random Events", TagCategory.OTHER);  // Beekeeper's top
		source(25133, "Random Events", TagCategory.OTHER);  // Beekeeper's legs
		source(25135, "Random Events", TagCategory.OTHER);  // Beekeeper's gloves
		source(25137, "Random Events", TagCategory.OTHER);  // Beekeeper's boots

		// Revenants
		source(22557, "Revenants", TagCategory.OTHER);  // Amulet of avarice
		source(21804, "Revenants", TagCategory.OTHER);  // Ancient crystal
		source(22305, "Revenants", TagCategory.OTHER);  // Ancient relic
		source(22302, "Revenants", TagCategory.OTHER);  // Ancient effigy
		source(22299, "Revenants", TagCategory.OTHER);  // Ancient medallion
		source(21813, "Revenants", TagCategory.OTHER);  // Ancient statuette
		source(21810, "Revenants", TagCategory.OTHER);  // Ancient totem
		source(21807, "Revenants", TagCategory.OTHER);  // Ancient emblem
		source(21802, "Revenants", TagCategory.OTHER);  // Revenant cave teleport
		source(21820, "Revenants", TagCategory.OTHER);  // Revenant ether

		// Rooftop Agility
		source(11849, "Rooftop Agility", TagCategory.OTHER);  // Mark of grace
		source(11850, "Rooftop Agility", TagCategory.OTHER);  // Graceful hood
		source(11851, "Rooftop Agility", TagCategory.OTHER);  // Graceful hood
		source(11852, "Rooftop Agility", TagCategory.OTHER);  // Graceful cape
		source(11853, "Rooftop Agility", TagCategory.OTHER);  // Graceful cape
		source(11854, "Rooftop Agility", TagCategory.OTHER);  // Graceful top
		source(11855, "Rooftop Agility", TagCategory.OTHER);  // Graceful top
		source(11856, "Rooftop Agility", TagCategory.OTHER);  // Graceful legs
		source(11857, "Rooftop Agility", TagCategory.OTHER);  // Graceful legs
		source(11858, "Rooftop Agility", TagCategory.OTHER);  // Graceful gloves
		source(11859, "Rooftop Agility", TagCategory.OTHER);  // Graceful gloves
		source(11860, "Rooftop Agility", TagCategory.OTHER);  // Graceful boots
		source(11861, "Rooftop Agility", TagCategory.OTHER);  // Graceful boots

		// Sailing Miscellaneous
		source(31996, "Sailing Miscellaneous", TagCategory.OTHER);  // Dragon metal sheet
		source(31406, "Sailing Miscellaneous", TagCategory.OTHER);  // Dragon nails
		source(31916, "Sailing Miscellaneous", TagCategory.OTHER);  // Dragon cannonball
		source(31946, "Sailing Miscellaneous", TagCategory.OTHER);  // Echo pearl
		source(31952, "Sailing Miscellaneous", TagCategory.OTHER);  // Swift albatross feather
		source(31954, "Sailing Miscellaneous", TagCategory.OTHER);  // Narwhal horn
		source(31959, "Sailing Miscellaneous", TagCategory.OTHER);  // Ray barbs
		source(31961, "Sailing Miscellaneous", TagCategory.OTHER);  // Broken dragon hook
		source(31949, "Sailing Miscellaneous", TagCategory.OTHER);  // Bottled storm
		source(32115, "Sailing Miscellaneous", TagCategory.OTHER);  // Dragon cannon barrel

		// Sea Treasures
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32388, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32389, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32390, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32391, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32392, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32393, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32394, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32395, "Sea Treasures", TagCategory.OTHER);  // Medallion fragment
		source(32398, "Sea Treasures", TagCategory.OTHER);  // Sailors' amulet
		source(32399, "Sea Treasures", TagCategory.OTHER);  // Sailors' amulet
		source(32863, "Sea Treasures", TagCategory.OTHER);  // Rusty locket
		source(32864, "Sea Treasures", TagCategory.OTHER);  // Mouldy block
		source(32865, "Sea Treasures", TagCategory.OTHER);  // Dull knife
		source(32866, "Sea Treasures", TagCategory.OTHER);  // Broken compass
		source(32867, "Sea Treasures", TagCategory.OTHER);  // Rusty coin
		source(32868, "Sea Treasures", TagCategory.OTHER);  // Broken sextant
		source(32869, "Sea Treasures", TagCategory.OTHER);  // Mouldy doll
		source(32870, "Sea Treasures", TagCategory.OTHER);  // Smashed mirror

		// Shayzien Armour
		source(13357, "Shayzien Armour", TagCategory.OTHER);  // Shayzien gloves (1)
		source(13358, "Shayzien Armour", TagCategory.OTHER);  // Shayzien boots (1)
		source(13359, "Shayzien Armour", TagCategory.OTHER);  // Shayzien helm (1)
		source(13360, "Shayzien Armour", TagCategory.OTHER);  // Shayzien greaves (1)
		source(13361, "Shayzien Armour", TagCategory.OTHER);  // Shayzien platebody (1)
		source(13362, "Shayzien Armour", TagCategory.OTHER);  // Shayzien gloves (2)
		source(13363, "Shayzien Armour", TagCategory.OTHER);  // Shayzien boots (2)
		source(13364, "Shayzien Armour", TagCategory.OTHER);  // Shayzien helm (2)
		source(13365, "Shayzien Armour", TagCategory.OTHER);  // Shayzien greaves (2)
		source(13366, "Shayzien Armour", TagCategory.OTHER);  // Shayzien platebody (2)
		source(13367, "Shayzien Armour", TagCategory.OTHER);  // Shayzien gloves (3)
		source(13368, "Shayzien Armour", TagCategory.OTHER);  // Shayzien boots (3)
		source(13369, "Shayzien Armour", TagCategory.OTHER);  // Shayzien helm (3)
		source(13370, "Shayzien Armour", TagCategory.OTHER);  // Shayzien greaves (3)
		source(13371, "Shayzien Armour", TagCategory.OTHER);  // Shayzien platebody (3)
		source(13372, "Shayzien Armour", TagCategory.OTHER);  // Shayzien gloves (4)
		source(13373, "Shayzien Armour", TagCategory.OTHER);  // Shayzien boots (4)
		source(13374, "Shayzien Armour", TagCategory.OTHER);  // Shayzien helm (4)
		source(13375, "Shayzien Armour", TagCategory.OTHER);  // Shayzien greaves (4)
		source(13376, "Shayzien Armour", TagCategory.OTHER);  // Shayzien platebody (4)
		source(13377, "Shayzien Armour", TagCategory.OTHER);  // Shayzien gloves (5)
		source(13378, "Shayzien Armour", TagCategory.OTHER);  // Shayzien boots (5)
		source(13379, "Shayzien Armour", TagCategory.OTHER);  // Shayzien helm (5)
		source(13380, "Shayzien Armour", TagCategory.OTHER);  // Shayzien greaves (5)
		source(13381, "Shayzien Armour", TagCategory.OTHER);  // Shayzien body (5)

		// Shooting Stars
		source(25547, "Shooting Stars", TagCategory.OTHER);  // Star fragment

		// Skilling Pets
		source(6715, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(6722, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(6817, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(10636, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(13320, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(25600, "Skilling Pets", TagCategory.OTHER);  // Heron
		source(2182, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7439, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7440, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7441, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7442, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7443, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7444, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7445, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7446, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7447, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7448, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7449, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7450, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7451, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7452, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7453, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7454, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7455, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7642, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7643, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7644, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7645, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7646, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7647, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7648, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7711, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7736, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7737, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7738, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7739, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7740, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(7741, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(13321, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(14923, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(14924, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(14925, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(15051, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(15052, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(15053, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21187, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21188, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21189, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21190, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21191, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21192, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21193, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21194, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21195, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21196, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21197, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21340, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21358, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21359, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(21360, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(31276, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(31277, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(31278, "Skilling Pets", TagCategory.OTHER);  // Rock golem
		source(12169, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12170, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12171, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12172, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12173, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12174, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12175, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12176, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12177, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12178, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12181, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12182, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12183, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12184, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12185, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12186, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12187, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12188, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12189, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(12190, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(13322, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(14926, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(14927, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(14928, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(14929, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(15054, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(15055, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(15056, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(15057, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28229, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28230, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28231, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28232, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28233, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28234, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28235, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28236, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(28237, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(31279, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(31280, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(31281, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(31282, "Skilling Pets", TagCategory.OTHER);  // Beaver
		source(6718, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6719, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6720, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6721, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6756, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6757, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6758, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(6759, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13323, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13324, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13325, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(13326, "Skilling Pets", TagCategory.OTHER);  // Baby chinchompa
		source(7334, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(7351, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(9637, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(9638, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(14032, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(14044, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(20659, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(24701, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(30151, "Skilling Pets", TagCategory.OTHER);  // Giant squirrel
		source(7335, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(7352, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9492, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9493, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9494, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9495, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9496, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9497, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9498, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9499, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9500, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(9501, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(20661, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(24555, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(24557, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(24559, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(24561, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(24563, "Skilling Pets", TagCategory.OTHER);  // Tangleroot
		source(7336, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(7353, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(9850, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(9851, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(9852, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(9853, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(20663, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(24847, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(24849, "Skilling Pets", TagCategory.OTHER);  // Rocky
		source(7337, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7338, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7339, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7340, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7341, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7342, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7343, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7344, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7345, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7346, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7347, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7348, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7349, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7350, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7354, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7355, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7356, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7357, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7358, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7359, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7360, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7361, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7362, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7363, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7364, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7365, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7366, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(7367, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(8024, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(8028, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(11401, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(11428, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20665, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20667, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20669, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20671, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20673, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20675, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20677, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20679, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20681, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20683, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20685, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20687, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20689, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(20691, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(21990, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(26899, "Skilling Pets", TagCategory.OTHER);  // Rift guardian
		source(14930, "Skilling Pets", TagCategory.OTHER);  // Soup
		source(15058, "Skilling Pets", TagCategory.OTHER);  // Soup
		source(31283, "Skilling Pets", TagCategory.OTHER);  // Soup

		// Slayer: the old OTHER "Slayer" category was retired 2026-04-08 in
		// favor of direct-inheritance via SourceAttributes.isSlayerItem().
		// Every item id that used to live here is now registered in
		// SourceAttributes.DIRECT_SLAYER_ITEMS, and buildItemTags appends
		// the SKILLING Slayer tag to any item in that set. This makes the
		// SKILLING Slayer tag the single source of truth for slayer items
		// and eliminates the gray-text duplicate that motivated the
		// earlier dedupe bandaid.

		// Tormented Demons
		source(29580, "Tormented Demons", TagCategory.OTHER);  // Tormented synapse
		source(29574, "Tormented Demons", TagCategory.OTHER);  // Burning claw
		source(29684, "Tormented Demons", TagCategory.OTHER);  // Guthixian temple teleport

		// TzHaar
		source(6568, "TzHaar", TagCategory.OTHER);  // Obsidian cape
		source(6524, "TzHaar", TagCategory.OTHER);  // Toktz-ket-xil
		source(6528, "TzHaar", TagCategory.OTHER);  // Tzhaar-ket-om
		source(6523, "TzHaar", TagCategory.OTHER);  // Toktz-xil-ak
		source(6525, "TzHaar", TagCategory.OTHER);  // Toktz-xil-ek
		source(6526, "TzHaar", TagCategory.OTHER);  // Toktz-mej-tal
		source(6522, "TzHaar", TagCategory.OTHER);  // Toktz-xil-ul
		source(21298, "TzHaar", TagCategory.OTHER);  // Obsidian helmet
		source(21301, "TzHaar", TagCategory.OTHER);  // Obsidian platebody
		source(21304, "TzHaar", TagCategory.OTHER);  // Obsidian platelegs

		// Miscellaneous
		source(7759, "Miscellaneous", TagCategory.OTHER);  // Herbi
		source(7760, "Miscellaneous", TagCategory.OTHER);  // Herbi
		source(21509, "Miscellaneous", TagCategory.OTHER);  // Herbi
		source(4001, "Miscellaneous", TagCategory.OTHER);  // Chompy chick
		source(4002, "Miscellaneous", TagCategory.OTHER);  // Chompy chick
		source(13071, "Miscellaneous", TagCategory.OTHER);  // Chompy chick
		source(13576, "Miscellaneous", TagCategory.OTHER);  // Dragon warhammer
		source(7991, "Miscellaneous", TagCategory.OTHER);  // Big swordfish
		source(7993, "Miscellaneous", TagCategory.OTHER);  // Big shark
		source(7989, "Miscellaneous", TagCategory.OTHER);  // Big bass
		source(31408, "Miscellaneous", TagCategory.OTHER);  // Giant blue krill
		source(31412, "Miscellaneous", TagCategory.OTHER);  // Golden haddock
		source(31416, "Miscellaneous", TagCategory.OTHER);  // Orangefin
		source(31420, "Miscellaneous", TagCategory.OTHER);  // Huge halibut
		source(31424, "Miscellaneous", TagCategory.OTHER);  // Purplefin
		source(31428, "Miscellaneous", TagCategory.OTHER);  // Swift marlin
		source(10976, "Miscellaneous", TagCategory.OTHER);  // Long bone
		source(10977, "Miscellaneous", TagCategory.OTHER);  // Curved bone
		source(11942, "Miscellaneous", TagCategory.OTHER);  // Ecumenical key
		source(19679, "Miscellaneous", TagCategory.OTHER);  // Dark totem base
		source(19681, "Miscellaneous", TagCategory.OTHER);  // Dark totem middle
		source(19683, "Miscellaneous", TagCategory.OTHER);  // Dark totem top
		source(11338, "Miscellaneous", TagCategory.OTHER);  // Chewed bones
		source(11335, "Miscellaneous", TagCategory.OTHER);  // Dragon full helm
		source(2366, "Miscellaneous", TagCategory.OTHER);  // Shield left half
		source(22100, "Miscellaneous", TagCategory.OTHER);  // Dragon metal slice
		source(22103, "Miscellaneous", TagCategory.OTHER);  // Dragon metal lump
		source(21918, "Miscellaneous", TagCategory.OTHER);  // Dragon limbs
		source(1249, "Miscellaneous", TagCategory.OTHER);  // Dragon spear
		source(1263, "Miscellaneous", TagCategory.OTHER);  // Dragon spear
		source(3176, "Miscellaneous", TagCategory.OTHER);  // Dragon spear
		source(5716, "Miscellaneous", TagCategory.OTHER);  // Dragon spear
		source(5730, "Miscellaneous", TagCategory.OTHER);  // Dragon spear
		source(19707, "Miscellaneous", TagCategory.OTHER);  // Amulet of eternal glory
		source(21838, "Miscellaneous", TagCategory.OTHER);  // Shaman mask
		source(20439, "Miscellaneous", TagCategory.OTHER);  // Evil chicken head
		source(20436, "Miscellaneous", TagCategory.OTHER);  // Evil chicken wings
		source(20442, "Miscellaneous", TagCategory.OTHER);  // Evil chicken legs
		source(20433, "Miscellaneous", TagCategory.OTHER);  // Evil chicken feet
		source(21343, "Miscellaneous", TagCategory.OTHER);  // Mining gloves
		source(21345, "Miscellaneous", TagCategory.OTHER);  // Superior mining gloves
		source(21392, "Miscellaneous", TagCategory.OTHER);  // Expert mining gloves
		source(9007, "Miscellaneous", TagCategory.OTHER);  // Right skull half
		source(9008, "Miscellaneous", TagCategory.OTHER);  // Left skull half
		source(9010, "Miscellaneous", TagCategory.OTHER);  // Top of sceptre
		source(9011, "Miscellaneous", TagCategory.OTHER);  // Bottom of sceptre
		source(22374, "Miscellaneous", TagCategory.OTHER);  // Mossy key
		source(20754, "Miscellaneous", TagCategory.OTHER);  // Giant key
		source(22875, "Miscellaneous", TagCategory.OTHER);  // Hespori seed
		source(7536, "Miscellaneous", TagCategory.OTHER);  // Fresh crab claw
		source(7538, "Miscellaneous", TagCategory.OTHER);  // Fresh crab shell
		source(23522, "Miscellaneous", TagCategory.OTHER);  // Mask of ranul
		source(23943, "Miscellaneous", TagCategory.OTHER);  // Elven signet
		source(24000, "Miscellaneous", TagCategory.OTHER);  // Crystal grail
		source(23959, "Miscellaneous", TagCategory.OTHER);  // Enhanced crystal teleport seed
		source(24034, "Miscellaneous", TagCategory.OTHER);  // Dragonstone full helm
		source(24037, "Miscellaneous", TagCategory.OTHER);  // Dragonstone platebody
		source(24040, "Miscellaneous", TagCategory.OTHER);  // Dragonstone platelegs
		source(24046, "Miscellaneous", TagCategory.OTHER);  // Dragonstone gauntlets
		source(24043, "Miscellaneous", TagCategory.OTHER);  // Dragonstone boots
		source(6571, "Miscellaneous", TagCategory.OTHER);  // Uncut onyx
		source(21649, "Miscellaneous", TagCategory.OTHER);  // Merfolk trident
		source(25844, "Miscellaneous", TagCategory.OTHER);  // Orange egg sac
		source(25846, "Miscellaneous", TagCategory.OTHER);  // Blue egg sac
		source(28813, "Miscellaneous", TagCategory.OTHER);  // Broken zombie axe
		source(30324, "Miscellaneous", TagCategory.OTHER);  // Broken zombie helmet
		source(30111, "Miscellaneous", TagCategory.OTHER);  // Helmet of the moon
		source(31572, "Miscellaneous", TagCategory.OTHER);  // Squid beak
	}
}

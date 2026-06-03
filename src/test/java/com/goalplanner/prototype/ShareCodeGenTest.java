package com.goalplanner.prototype;

import com.goalplanner.share.GoalShareDto;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.ShareCodec;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Generator (not a real assertion test) — emits a GPSHARE1 share code for the
 * dependency-rail example (diamond + OR) so it can be imported into the live
 * plugin. Writes the code to build/share-code.txt and prints it.
 */
class ShareCodeGenTest
{
	private static GoalShareDto custom(int ref, String name, List<Integer> requires, List<Integer> orRequires)
	{
		GoalShareDto d = new GoalShareDto();
		d.setRef(ref);
		d.setType("CUSTOM");
		d.setName(name);
		d.setTargetValue(1);
		if (requires != null) d.setRequires(requires);
		if (orRequires != null) d.setOrRequires(orRequires);
		return d;
	}

	@Test
	void generate() throws Exception
	{
		// Interconnected diamonds: 3 skills each feed 2 of 3 quests, all merge
		// into Quest Cape; Grandmaster CA needs that AND (Fire OR Infernal).
		List<GoalShareDto> goals = Arrays.asList(
			custom(0, "70 Agility", null, null),
			custom(1, "100 Combat", null, null),
			custom(2, "75 Ranged", null, null),
			custom(3, "Monkey Madness II", Arrays.asList(0, 1), null),     // Agility + Combat
			custom(4, "Desert Treasure II", Arrays.asList(1, 2), null),    // Combat + Ranged
			custom(5, "Song of the Elves", Arrays.asList(0, 2), null),     // Agility + Ranged
			custom(6, "Quest Point Cape", Arrays.asList(3, 4, 5), null),   // merge of all three
			custom(7, "Fire Cape", null, null),
			custom(8, "Infernal Cape", null, null),
			custom(9, "Grandmaster CA", Arrays.asList(6), Arrays.asList(7, 8)) // QPC AND (Fire OR Infernal)
		);

		ShareBundle bundle = new ShareBundle();
		bundle.setKind(ShareBundle.Kind.SECTION);
		bundle.setSectionName("Dependency Demo");
		bundle.setSharedBy("rail prototype");
		bundle.setGoals(goals);

		String code = new ShareCodec(new Gson()).encode(bundle);

		Path out = Paths.get("build", "share-code.txt");
		Files.createDirectories(out.getParent());
		Files.write(out, code.getBytes());
		System.out.println("SHARE_CODE=" + code);
	}
}

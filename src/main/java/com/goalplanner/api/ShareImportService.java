package com.goalplanner.api;

import com.goalplanner.model.Goal;
import com.goalplanner.model.GoalType;
import com.goalplanner.model.Section;
import com.goalplanner.model.Tag;
import com.goalplanner.model.TagCategory;
import com.goalplanner.persistence.GoalStore;
import com.goalplanner.share.GoalShareDto;
import com.goalplanner.share.ShareBundle;
import com.goalplanner.share.TagShareDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies a decoded {@link ShareBundle} (received over the Party transport, or
 * pasted as a share code) into the store: a new user section, fresh goals with
 * find-or-created tags and remapped relations, all in one undo-compound so a
 * single {@code undo()} reverses the whole import.
 *
 * <p>Imports land in their own user section: because user sections keep their
 * completed goals inline, the recipient sees the whole shared set as a checklist
 * against their own account — requirements they already meet show ticked off,
 * the rest show progress.
 *
 * <p>Inverse of {@code com.goalplanner.share.ShareMapper}. Because the payload
 * originates on another player's client it is treated as <b>untrusted</b>:
 * unknown {@link GoalType}/{@link TagCategory} values are skipped and string
 * fields are length-clamped, so a malformed or hostile bundle can't corrupt the
 * store.
 */
@Slf4j
class ShareImportService
{
	/** Defensive caps on attacker-controllable strings. */
	private static final int MAX_NAME = 120;
	private static final int MAX_TEXT = 600;

	private final GoalPlannerApiImpl api;

	ShareImportService(GoalPlannerApiImpl api)
	{
		this.api = api;
	}

	/**
	 * Import a bundle into a new user section.
	 *
	 * @return the id of the section the goals were imported into, or
	 *         {@code null} if there was nothing importable.
	 */
	String importBundle(ShareBundle bundle)
	{
		if (bundle == null || bundle.getGoals() == null || bundle.getGoals().isEmpty())
		{
			return null;
		}

		final String sectionName = importSectionName(bundle);
		final List<GoalShareDto> dtos = bundle.getGoals();

		// Wrap the whole import as ONE undoable command: apply creates the
		// section, goals and relations; revert removes them. A single undo
		// reverses the entire import, and redo replays it (creating fresh ids).
		// Created ids are tracked across apply/revert so redo works too.
		// Imported goals always start INCOMPLETE — buildGoal copies only
		// definition fields, never the sharer's progress/completion.
		final List<String> createdGoalIds = new ArrayList<>();
		final String[] createdSectionId = {null};   // set only when we create a section (for revert)
		final String[] landedSectionId = {null};     // section the goals ended up in (return value)

		api.executeCommand(new com.goalplanner.command.Command()
		{
			@Override
			public boolean apply()
			{
				// Every import lands in its own NEW user section, forced to
				// KEEP COMPLETED INLINE (override the global auto-archive default)
				// so the recipient sees the shared set as a checklist against
				// their account: requirements they already meet show ticked off,
				// the rest show progress. Loose selections get their own section
				// too rather than scattering into Incomplete.
				Section section = api.goalStore.createUserSection(sectionName);
				if (section == null)
				{
					log.warn("importBundle: createUserSection returned null for '{}'", sectionName);
					return false;
				}
				api.goalStore.setSectionAutoArchiveOverride(section.getId(), false);
				createdSectionId[0] = section.getId();
				String targetSectionId = section.getId();
				landedSectionId[0] = targetSectionId;
				createdGoalIds.clear();
				Map<Integer, String> refToId = new HashMap<>();

				// First pass: create goals, skipping any with an unknown type.
				for (GoalShareDto dto : dtos)
				{
					GoalType type = parseType(dto.getType());
					if (type == null)
					{
						log.warn("importBundle: skipping goal with unknown type '{}'", dto.getType());
						continue;
					}
					Goal goal = buildGoal(dto, type, targetSectionId);
					api.goalStore.addGoal(goal);
					createdGoalIds.add(goal.getId());
					refToId.put(dto.getRef(), goal.getId());
				}

				// Second pass: rewire relations now that every goal has a fresh id.
				for (GoalShareDto dto : dtos)
				{
					String fromId = refToId.get(dto.getRef());
					if (fromId == null)
					{
						continue;
					}
					for (Integer ref : dto.getRequires())
					{
						String toId = refToId.get(ref);
						if (toId != null)
						{
							api.goalStore.addRequirement(fromId, toId);
						}
					}
					for (Integer ref : dto.getOrRequires())
					{
						String toId = refToId.get(ref);
						if (toId != null)
						{
							api.goalStore.addOrRequirement(fromId, toId);
						}
					}
				}
				return true;
			}

			@Override
			public boolean revert()
			{
				for (String goalId : createdGoalIds)
				{
					api.goalStore.removeGoal(goalId);
				}
				if (createdSectionId[0] != null)
				{
					api.goalStore.deleteUserSection(createdSectionId[0]);
				}
				return true;
			}

			@Override
			public String getDescription()
			{
				return "Import shared goals: " + sectionName;
			}
		});

		return landedSectionId[0];
	}

	private Goal buildGoal(GoalShareDto dto, GoalType type, String sectionId)
	{
		Goal goal = Goal.builder()
			.type(type)
			.name(clamp(dto.getName(), MAX_NAME))
			.description(clamp(dto.getDescription(), MAX_TEXT))
			.targetValue(dto.getTargetValue())
			.skillName(dto.getSkillName())
			.questName(dto.getQuestName())
			.accountMetric(dto.getAccountMetric())
			.bossName(clamp(dto.getBossName(), MAX_NAME))
			.varbitId(dto.getVarbitId())
			.itemId(dto.getItemId())
			.spriteId(dto.getSpriteId())
			.tooltip(clamp(dto.getTooltip(), MAX_TEXT))
			.caTaskId(dto.getCaTaskId())
			.customColorRgb(dto.getCustomColorRgb())
			.optional(dto.isOptional())
			.autoSeeded(dto.isAutoSeeded())
			.wikiUrl(clamp(dto.getWikiUrl(), MAX_TEXT))
			.inventorySetup(clamp(dto.getInventorySetup(), MAX_NAME))
			.sectionId(sectionId)
			.build();
		goal.setTagIds(resolveTags(dto.getTags()));
		return goal;
	}

	private List<String> resolveTags(List<TagShareDto> tags)
	{
		List<String> ids = new ArrayList<>();
		if (tags == null)
		{
			return ids;
		}
		for (TagShareDto spec : tags)
		{
			String label = clamp(spec.getLabel(), MAX_NAME);
			TagCategory category = parseCategory(spec.getCategory());
			if (label == null || label.isEmpty() || category == null)
			{
				continue;   // unusable tag — skip
			}
			Tag tag = spec.isSystem()
				? api.goalStore.findOrCreateSystemTag(label, category)
				: findOrCreateUserTag(label, category);
			if (tag == null)
			{
				continue;
			}
			// Only recolour user tags — system-tag colours are shared across the
			// recipient's goals and shouldn't be mutated by an import.
			if (!spec.isSystem() && spec.getColorRgb() != -1)
			{
				api.goalStore.recolorTag(tag.getId(), spec.getColorRgb());
			}
			ids.add(tag.getId());
		}
		return ids;
	}

	private Tag findOrCreateUserTag(String label, TagCategory category)
	{
		Tag existing = api.goalStore.findTagByLabel(label, category);
		return existing != null ? existing : api.goalStore.createUserTag(label, category);
	}

	private String importSectionName(ShareBundle bundle)
	{
		String base = bundle.getKind() == ShareBundle.Kind.SECTION && notBlank(bundle.getSectionName())
			? bundle.getSectionName().trim()
			: "Shared goals";
		String sharedBy = bundle.getSharedBy() != null ? bundle.getSharedBy().trim() : null;
		String withFrom = notBlank(sharedBy) ? base + " (from " + sharedBy + ")" : base;
		// Section names are capped (GoalStore.createUserSection THROWS on longer
		// ones — which CommandHistory swallows, silently failing the whole import).
		// If the "(from X)" form doesn't fit, drop the suffix; clamp as a backstop.
		if (withFrom.length() <= GoalStore.MAX_SECTION_NAME_LENGTH)
		{
			return withFrom;
		}
		return clamp(base, GoalStore.MAX_SECTION_NAME_LENGTH);
	}

	private static GoalType parseType(String name)
	{
		if (name == null)
		{
			return null;
		}
		try
		{
			return GoalType.valueOf(name);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private static TagCategory parseCategory(String name)
	{
		if (name == null)
		{
			return null;
		}
		try
		{
			return TagCategory.valueOf(name);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private static boolean notBlank(String s)
	{
		return s != null && !s.trim().isEmpty();
	}

	private static String clamp(String s, int max)
	{
		if (s == null)
		{
			return null;
		}
		String t = s.trim();
		return t.length() <= max ? t : t.substring(0, max);
	}
}

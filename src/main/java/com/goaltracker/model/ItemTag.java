package com.goaltracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight (label, category) spec used by the data layer (item sources,
 * source attributes, etc) to describe tags BEFORE they become persisted
 * {@link Tag} entities. NOT serialized.
 *
 * <p>Goal creation flows iterate these specs and call
 * {@code goalStore.findOrCreateSystemTag(spec.label, spec.category)} to
 * get back the actual {@link Tag} entity (with an id), which the goal
 * then references via its {@code tagIds} list.
 *
 * <p>Mission 19 (tag refactor): the previous version of this class held
 * persisted color overrides and was embedded in {@code Goal.tags}. That
 * model is gone — tags are now first-class entities. This class exists
 * solely as a value type for the data layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTag
{
	private String label;
	private TagCategory category;
}

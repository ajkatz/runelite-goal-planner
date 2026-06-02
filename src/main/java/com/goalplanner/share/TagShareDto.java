package com.goalplanner.share;

import lombok.Data;

/**
 * A tag's shareable form inside a {@link ShareBundle}. Carries the
 * resolvable identity ({@link #label} + {@link #category}) plus its colour and
 * system flag, so the importer can find-or-create the matching tag in the
 * recipient's store rather than relying on a cross-client store id.
 */
@Data
public class TagShareDto
{
	private String label;
	private String category;   // TagCategory name
	private int colorRgb = -1;
	private boolean system;
}

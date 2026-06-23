package com.goalplanner.persistence;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * A bookmarked share code ("Saved Plan"): a user-supplied {@code name}, the raw
 * {@code GPSHARE...} {@code code}, and per-section display-name overrides applied
 * when the plan is imported. Stored globally (across profiles) by
 * {@link SavedPlanStore} - a share code carries goal definitions only, so the
 * library is account-agnostic.
 */
@Data
public class SavedPlan
{
	private String id;
	private String name;
	private String code;

	/**
	 * Desired section display names, parallel to the code's sections (as listed
	 * by {@code bundle.effectiveSections()}). A blank/absent entry keeps the
	 * code's original section name on import.
	 */
	private List<String> sectionNames = new ArrayList<>();

	private long savedAt;
}

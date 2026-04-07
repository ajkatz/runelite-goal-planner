package com.goaltracker.api;

/**
 * Public DTO for a section. No internal model classes referenced.
 */
public class SectionView
{
	public String id;
	public String name;
	public int order;
	public boolean collapsed;
	public boolean builtIn;
	/**
	 * Built-in section role: "INCOMPLETE", "COMPLETED", or {@code null} for user-defined sections.
	 */
	public String kind;
}

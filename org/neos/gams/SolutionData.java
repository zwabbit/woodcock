package org.neos.gams;

import java.util.ArrayList;
import java.util.List;

public class SolutionData {
	/**
	 * Equation type
	 */
	public static final String EQU = "EQU";
	
	/**
	 * Variable type
	 */
	public static final String VAR = "VAR";

	String description;
	String name;
	int dimension;
	String type;

	List<SolutionRow> rows = new ArrayList<SolutionRow>();
	
	public boolean addRow(SolutionRow row) {
		return this.rows.add(row);
	}
	
	public List<SolutionRow> getRows() {
		return this.rows;
	}
		
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}

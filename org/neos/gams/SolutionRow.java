package org.neos.gams;

import java.util.ArrayList;
import java.util.List;

/**
 * Data entity for each row of GAMS output table. 
 * @author Thawan Kooburat
 *
 */
public class SolutionRow {

	List<String> index = new ArrayList<String>();
	Double level;
	Double marginal;
	Double lower;
	Double upper;
	

	public boolean addIndex(String txt) {
		return index.add(txt);
	}

	/**
	 * Get key from a specified dimension. 
	 * First dimension's key can be accessed using index = 0 
	 * 
	 * @param index
	 * @return
	 */
	public String getIndex(int index) {
		return this.index.get(index);
	}

	public Double getLevel() {
		return level;
	}

	public void setLevel(Double level) {
		this.level = level;
	}

	public Double getMarginal() {
		return marginal;
	}

	public void setMarginal(Double marginal) {
		this.marginal = marginal;
	}

	public Double getLower() {
		return lower;
	}

	public void setLower(Double lower) {
		this.lower = lower;
	}

	public Double getUpper() {
		return upper;
	}

	public void setUpper(Double upper) {
		this.upper = upper;
	}



}

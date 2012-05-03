package org.neos.gams;

/**
 * Abstract class for all GAMS data type
 * @author Thawan Kooburat
 *
 */
public abstract class BaseData {
	String name;
	String description;

	public BaseData(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	/**
	 * Create string that represent this data to be used in the GAMS model
	 */
	public abstract String toString();
}

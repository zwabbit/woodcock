package org.neos.gams;

import java.util.ArrayList;
import java.util.List;

/**
 * Set data type. This structure does not check for duplicate insertion.
 * 
 * @author Thawan Kooburat
 * 
 */
public class Set extends BaseData {

	/*
	 * GAMS retain the order of element entered, so we have to retain this.
	 */
	List<String> values = new ArrayList<String>();

	public Set(String name, String description) {
		super(name, description);

	}

	public boolean addValue(String val) {
		return this.values.add(val);
	}

	public String getValue(int i) {
		return values.get(i);
	}

	public String toString() {
		if (values.size() == 0)
			return "";

		StringBuffer buff = new StringBuffer();
		buff.append(String.format("Set %s %s /", name, description));

		int i = 0;
		buff.append(values.get(0));

		for (i = 1; i < values.size(); i++) {
			buff.append(", " + values.get(i));
		}
		buff.append(" /;\n");
		return buff.toString();
	}
	public static void main(String[] args) {
		Set countryCode = new Set("cc", "Country Code");
		countryCode.addValue("UK");
		countryCode.addValue("US");
		countryCode.addValue("TH");
		System.out.println(countryCode);
		
		
	}

}

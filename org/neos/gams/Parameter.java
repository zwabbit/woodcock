package org.neos.gams;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameter data type. To use this for more than 1 dimension parameter, concatenate key using "."
 * Use method toString() to create string for including into the model.
 * @author Thawan Kooburat
 *
 */
public class Parameter extends BaseData {

	int dimension;
	List<Entry> values = new ArrayList<Entry>();

	public Parameter(String name, String description) {
		super(name, description);

	}

	public boolean add(String key, String value) {
		return values.add(new Entry(key, value));
	}
		
	public String toString() {
		if (values.size() == 0)
			return "";

		StringBuffer buff = new StringBuffer();
		buff.append(String.format("Parameter %s %s /\n", name, description));

		for (int i = 0; i < values.size(); i++) {
			buff.append(String.format("%s %s\n", values.get(i).key,
					values.get(i).value));
		}
		buff.append(" /;\n");
		return buff.toString();
	}

	class Entry {
		String key;
		String value;

		Entry(String key, String val) {
			this.key = key;
			this.value = val;
		}
	}
	
	
	public static void main(String[] args) {
		Parameter capital = new Parameter("cap(c)", "Capital of");
		capital.add("TH", "Bangkok");
		capital.add("US", "Washington D.C");
		capital.add("UK", "London");
		System.out.println(capital);
	}

}

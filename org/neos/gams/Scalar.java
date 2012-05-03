package org.neos.gams;

public class Scalar extends BaseData {
	String value;

	public Scalar(String name, String description, String value) {
		super(name, description);
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String val) {
		this.value = val;
	}

	public String toString() {

		return String.format("Scalar %s %s /%s/;", name, description, value);
	}
	
	public static void main(String[] args) {
		Scalar city = new Scalar("city", "City", "Madison");
		System.out.println(city);
	}
}

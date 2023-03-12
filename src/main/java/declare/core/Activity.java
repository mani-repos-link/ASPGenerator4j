package main.java.declare.core;

import java.util.Objects;

import main.java.declare.encoding.Encoder;

public class Activity {
	String name;
	String encodedName;
	
	public Activity(String name) {
		this.name = name;
		this.encodedName = Encoder.getActivityEncoding(name);
	}

	public String getName() {
		return this.name;
	}
	
	public String getEncodedName() {
		return this.encodedName;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Activity other = (Activity) obj;
		return Objects.equals(name, other.name);
	}
}

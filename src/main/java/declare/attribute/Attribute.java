package main.java.declare.attribute;

import java.util.Objects;

import main.java.declare.encoding.Encoder;

public abstract class Attribute {
	private String name;
	private String encodedName;
	
	public Attribute(String name) {
		this.name = name;
		this.encodedName = Encoder.getAttributeEncoding();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getEncodedName() {
		return this.encodedName;
	}
	
	@Override
	public abstract String toString();

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
		Attribute other = (Attribute) obj;
		return Objects.equals(name, other.name);
	}
}

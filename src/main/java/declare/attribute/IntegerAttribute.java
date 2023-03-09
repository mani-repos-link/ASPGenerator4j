package main.java.declare.attribute;

public class IntegerAttribute extends Attribute {
	private int lowerBound;
	private int upperBound;
	
	public IntegerAttribute(String name, int lowerBound, int upperBound) {
		super(name);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}
	
	@Override
	public String toString() {
		return this.getName() + ": integer between " + lowerBound + " and " + upperBound;
	}
}

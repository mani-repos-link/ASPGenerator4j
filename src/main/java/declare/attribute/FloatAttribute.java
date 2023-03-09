package main.java.declare.attribute;

public class FloatAttribute extends Attribute {
	private double lowerBound;
	private double upperBound;
	
	public FloatAttribute(String name, double lowerBound, double upperBound) {
		super(name);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}
	
	@Override
	public String toString() {
		return this.getName() + ": float between " + lowerBound + " and " + upperBound;
	}
}

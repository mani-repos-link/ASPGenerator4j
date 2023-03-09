package main.java.declare.constraint.condition;

public enum ConcreteOperator implements Operator {
	// Operators for numerical attributes
	GT(">"),
	GEQ(">="),
	LT("<"),
	LEQ("<="),
	EQ("="),
	NEQ("!="),
	
	// Operators for categorical attributes
	IS("is"),
	IS_NOT("is not"),
	IN("in"),				//TODO: (in, not in, same, different, exist) are never used!
	NOT_IN("not in"),
	
	// Operators for both types of attributes
	SAME("same"),
	DIFFERENT("different"),
	EXIST("exist");
	
	private String stringDisplay;
	
	private ConcreteOperator(String stringDisplay) {
		this.stringDisplay = stringDisplay;
	}
	
	@Override
	public String toString() {
		return this.stringDisplay;
	}
}
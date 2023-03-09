package main.java.declare.constraint.condition;

public enum LogicOperator implements Operator {
	AND("AND"),
	OR("OR");

	private String stringDisplay;
	
	private LogicOperator(String stringDisplay) {
		this.stringDisplay = stringDisplay;
	}
	
	@Override
	public String toString() {
		return this.stringDisplay;
	}
}

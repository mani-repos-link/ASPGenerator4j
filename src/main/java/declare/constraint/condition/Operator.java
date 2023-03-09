package main.java.declare.constraint.condition;

import java.util.AbstractMap;
import java.util.Map;

public interface Operator {
	
	public static Map<Operator, Operator> oppositeOperator = Map.ofEntries(
					new AbstractMap.SimpleEntry<Operator, Operator>(LogicOperator.AND, LogicOperator.OR),
					new AbstractMap.SimpleEntry<Operator, Operator>(LogicOperator.OR, LogicOperator.AND),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.GT, ConcreteOperator.LEQ),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.LEQ, ConcreteOperator.GT),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.LT, ConcreteOperator.GEQ),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.GEQ, ConcreteOperator.LT),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.EQ, ConcreteOperator.NEQ),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.NEQ, ConcreteOperator.EQ),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.IS, ConcreteOperator.IS_NOT),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.IS_NOT, ConcreteOperator.IS),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.IN, ConcreteOperator.NOT_IN),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.NOT_IN, ConcreteOperator.IN),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.SAME, ConcreteOperator.DIFFERENT),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.DIFFERENT, ConcreteOperator.SAME),
					new AbstractMap.SimpleEntry<Operator, Operator>(ConcreteOperator.EXIST, ConcreteOperator.EXIST) ); //TODO: Note that the ConcreteOperator EXIST hasn't an opposite operator!
	
	public String toString();
	
	public static Operator getOpposite(Operator op) {
		return oppositeOperator.get(op);
	}
	
	public static Operator getOperatorFromString(String opStr) {
		for (Operator op : oppositeOperator.keySet())
			if (opStr.equals(op.toString()))
				return op;
		
		return null;
	}
}

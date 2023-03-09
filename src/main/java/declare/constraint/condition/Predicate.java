package main.java.declare.constraint.condition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.declare.attribute.Attribute;
import main.java.declare.attribute.CategoricalAttribute;

public abstract class Predicate {
	public static final Predicate fulfillmentPredicate = new ConcretePredicate(null, new CategoricalAttribute("fulfillment", Collections.emptySet()), ConcreteOperator.IS, "true");
	public static final Predicate violationPredicate = new ConcretePredicate(null, new CategoricalAttribute("violation", Collections.emptySet()), ConcreteOperator.IS, "true");
	public final static Predicate dummyPredicate = new ConcretePredicate(null, new CategoricalAttribute("dummy_predicate", Collections.emptySet()), ConcreteOperator.IS, "true");
	
	private Predicate parent;
	private Operator operator;
	
	public Predicate(Predicate parent, Operator operator) {
		this.parent = parent;
		this.operator = operator;
	}
	
	public Predicate getParent() {
		return parent;
	}

	public Operator getOperator() {
		return operator;
	}
	
	public void setParent(Predicate newParent) {
		this.parent = newParent;
	}
	
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public static Predicate getPredicateFromString(String predicateStr, Set<Attribute> declaredAttribs) {
		if (predicateStr.isBlank())
			return new LogicPredicate(null, LogicOperator.OR);
		
		// STRING PARSER: Parse the predicate string to extract the children sub-predicates between parentheses
		Deque<Character> stack = new ArrayDeque<>();
		List<String> innerPredStrings = new ArrayList<>();
		int firstParenthesisIndex = predicateStr.indexOf("(");
		
		if (firstParenthesisIndex >= 0) {
			int lastParenthesisIndex;
			
			for (int i = firstParenthesisIndex; i < predicateStr.length(); i++) {
				char x = predicateStr.charAt(i);
				
				if (x == '(') {
					if (stack.isEmpty())
						firstParenthesisIndex = i;
					
					stack.push(x);
				}
				
				if (x == ')' && !stack.isEmpty()) {
					stack.pop();
					
					if (stack.isEmpty()) {
						lastParenthesisIndex = i;
						innerPredStrings.add(predicateStr.substring(firstParenthesisIndex+1, lastParenthesisIndex));
					}
				}
	        }
		}
		
		
		Predicate predicate;
		
		// BASE CASE: Empty sub-predicates list means empty predicate or the presence of only one AttributePredicate
		if (innerPredStrings.isEmpty() && !predicateStr.isBlank()) {
			String operatorsRegex = "(?i)\\s+(is\\s+not|is|not\\s+in|in|or|and|not|same|different|exist|<=|>=|<|>|=|!=)\\s+";
			Matcher operatorMatcher = Pattern.compile(operatorsRegex).matcher(predicateStr);
			
			String operator = "";
			if (operatorMatcher.find())
				operator = operatorMatcher.group().trim();
			
			String[] params = predicateStr.split("\\s+"+operator+"\\s+");
			Attribute attribute = declaredAttribs.stream()
					.filter(att -> att.getName().equals(params[0].trim()))
					.findFirst()
					.orElseThrow(() -> new NoSuchElementException("Syntax error - Attribute '" + params[0].trim() + "' not defined!"));
            String value = params[1].trim();
			
			predicate = new ConcretePredicate(null, attribute, (ConcreteOperator)Operator.getOperatorFromString(operator), value);
		
		// RECURSION: If children sub-predicates exist, the method is called recursively over them
		} else {
			
			Set<Predicate> preds = new HashSet<>();
			for (String innerPredStr : innerPredStrings)
				preds.add( getPredicateFromString(innerPredStr, declaredAttribs) );
			
			int lastIndexof1stPredicate = innerPredStrings.get(0).length()+2; // +2 takes into account of parentheses
			
			if (innerPredStrings.size() > 1 && predicateStr.substring(lastIndexof1stPredicate+1).strip().toLowerCase().startsWith(LogicOperator.OR.toString().toLowerCase()))
				predicate = new LogicPredicate(null, LogicOperator.OR).addChildren(preds);
			
			else
				predicate = new LogicPredicate(null, LogicOperator.AND).addChildren(preds);
		}
		
		return predicate;
	}
	
	public abstract Set<Attribute> getUnboundAttributes(Set<Attribute> bindings);
	
	public abstract Predicate makeOpposite();
		
	public abstract Predicate addChildren(Set<Predicate> children);
	
	public abstract Set<Predicate> getChildren();

	public abstract String toString();
		
	public abstract boolean isEmpty();
	
	public abstract int getSize();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predicate other = (Predicate) obj;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		return true;
	}
}

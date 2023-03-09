package main.java.declare.constraint.condition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import main.java.declare.attribute.Attribute;

public class ConcretePredicate extends Predicate {
	private Attribute attribute;
	private String value;
	
	public ConcretePredicate(Predicate parent, Attribute attribute, ConcreteOperator operator, String value) {
		super(parent, operator);
		this.attribute = attribute;
		this.value = value;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}
	
	public int getPredicateSize() {
		return 1;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public Predicate addChildren(Set<Predicate> children) {
		return this;
	}

	@Override
	public Set<Predicate> getChildren() {
		return null;
	}

	@Override
	public Predicate makeOpposite() {
		if (value.equalsIgnoreCase("true"))
			return new ConcretePredicate(null, attribute, ConcreteOperator.IS, "false");
		
		else if (value.equalsIgnoreCase("false"))
			return new ConcretePredicate(null, attribute, ConcreteOperator.IS, "true");
		
		else
			return new ConcretePredicate(null, attribute, (ConcreteOperator)Operator.getOpposite(this.getOperator()), value);
	}
	
	@Override
	public Set<Attribute> getUnboundAttributes(Set<Attribute> bindings) {
		Set<Attribute> unboundAttributes = new HashSet<>();
		
		if (!bindings.contains(attribute))
			unboundAttributes.add(attribute);
		
		return unboundAttributes;
	}
	
	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String toString() {
		return this.attribute.getName() + " " + this.getOperator() + " " + this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(attribute, value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConcretePredicate other = (ConcretePredicate) obj;
		return Objects.equals(attribute, other.attribute) && Objects.equals(value, other.value);
	}
}

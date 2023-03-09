package main.java.declare.constraint.condition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import main.java.declare.attribute.Attribute;

public class LogicPredicate extends Predicate {
	private Set<Predicate> children;
	
	public LogicPredicate(Predicate parent, LogicOperator operator) {
		super(parent, operator);
		this.children = new HashSet<>();
	}
	
	@Override
	public Set<Predicate> getChildren() {
		return children;
	}

	private void addChild(Predicate pred) {
		if (pred.getChildren() != null) {
			
			if (pred.getChildren().size()==1 || this.getOperator().equals(pred.getOperator())) {
				
				this.addChildren(pred.getChildren());
			
			} /*else if (this.getChildren().size() == 1) {
				
				this.setOperator(pred.getOperator());
				this.addChildren(pred.getChildren());
				
			}*/ else {
				pred.setParent(this);
				children.add(pred);
			}
				
		} else {
			pred.setParent(this);
			children.add(pred);
		}
	}
	
	@Override
	public Predicate addChildren(Set<Predicate> children) {
		
		for (Predicate child : children)
			this.addChild(child);
		
		return this.checkConsistency();
	}
	
	private Predicate checkConsistency() {
		if (children.size() == 1) {
			Predicate child = this.children.iterator().next();
			child.setParent(null);
			
			if (child.getChildren() == null) {
				ConcretePredicate concPr = (ConcretePredicate) child;
				return new ConcretePredicate(this.getParent(), concPr.getAttribute(), (ConcreteOperator)concPr.getOperator(), concPr.getValue());
				
			} else {
				LogicPredicate logPr = (LogicPredicate) child;
				return new LogicPredicate(this.getParent(), (LogicOperator)logPr.getOperator()).addChildren(logPr.getChildren());
			}
		}
		
		return this;
	}

	@Override
	public boolean isEmpty() {
		return children.stream().allMatch(child -> child.isEmpty());
	}
	
	@Override
	public Predicate makeOpposite() {
		Set<Predicate> oppositeChildren = new HashSet<>();
		for (Predicate child : children)
			oppositeChildren.add(child.makeOpposite());
		
		LogicOperator oppositeOperator = (LogicOperator) Operator.getOpposite(this.getOperator());
		return new LogicPredicate(null, oppositeOperator).addChildren(oppositeChildren);
	}
	
	@Override
	public Set<Attribute> getUnboundAttributes(Set<Attribute> bindings) {
		Set<Attribute> unboundAttributes = new HashSet<>();
		
		for (Predicate child : children)
			unboundAttributes.addAll(child.getUnboundAttributes(bindings));
		
		return unboundAttributes;
	}
	
	@Override
	public int getSize() {
		int size = 0;
		
		for (Predicate child : children)
			size += child.getSize();
		
		return size;
	}
	
	@Override
	public String toString() {
		String output = "";
		Iterator<Predicate> it = children.iterator();
		
		if (it.hasNext())
			output += "(" + it.next().toString() + ")";
		
		while (it.hasNext())
			output += " " + this.getOperator().toString() + " (" + it.next().toString() + ")";
		
		return output;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((children == null) ? 0 : children.hashCode());
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
		LogicPredicate other = (LogicPredicate) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		return true;
	}
}
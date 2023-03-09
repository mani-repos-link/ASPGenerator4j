package main.java.declare.constraint;

import java.util.List;
import java.util.Objects;

import main.java.declare.constraint.condition.Predicate;
import main.java.declare.constraint.condition.TimeCondition;
import main.java.declare.core.Activity;
import main.java.declare.core.Template;

public class BinaryConstraint extends Constraint {
	private Activity target;
	private Predicate targetCond;
	
	public BinaryConstraint(Template template, Activity activation, Activity target, 
			Predicate activationCond, Predicate targetCond, TimeCondition timeCond) {
		super(template, activation, activationCond, timeCond);
		this.target = target;
		this.targetCond = targetCond;
	}
	
	public Activity getTarget() {
		return target;
	}

	public Predicate getTargetCond() {
		return targetCond;
	}
	
	@Override
	public String toString() {
		String activities = String.join(", ", 
				this.getTemplate().getReverseActivationTarget() ? 
						List.of(target.toString(), this.getActivation().getName()) 
						: List.of(this.getActivation().getName(), target.toString())
		);
		
		return this.getTemplate() + "[" + activities + "]"
				+ " |" + this.getActivationCond() 
				+ " |" + targetCond 
				+ " |" + (this.getTimeCond()==null ? "" : this.getTimeCond().toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(target, targetCond);
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
		BinaryConstraint other = (BinaryConstraint) obj;
		return Objects.equals(target, other.target) && Objects.equals(targetCond, other.targetCond);
	}
}

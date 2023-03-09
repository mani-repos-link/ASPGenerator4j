package main.java.declare.constraint;

import java.util.Objects;

import main.java.declare.constraint.condition.Predicate;
import main.java.declare.constraint.condition.TimeCondition;
import main.java.declare.core.Activity;
import main.java.declare.core.Template;

public abstract class Constraint {
	private Template template;
	private Activity activation;
	private Predicate activationCond;
	private TimeCondition timeCond;
	
	public Constraint(Template template, Activity activation, Predicate activationCond, TimeCondition timeCond) {
		this.template = template;
		this.activation = activation;
		this.activationCond = activationCond;
		this.timeCond = timeCond;
	}

	public Template getTemplate() {
		return template;
	}

	public Activity getActivation() {
		return activation;
	}

	public Predicate getActivationCond() {
		return activationCond;
	}

	public TimeCondition getTimeCond() {
		return timeCond;
	}
	
	@Override
	public abstract String toString();

	@Override
	public int hashCode() {
		return Objects.hash(activation, activationCond, template, timeCond);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constraint other = (Constraint) obj;
		return Objects.equals(activation, other.activation) && Objects.equals(activationCond, other.activationCond)
				&& template == other.template && Objects.equals(timeCond, other.timeCond);
	}
}

package main.java.declare.core;

import java.util.Map;
import java.util.Set;

import main.java.declare.attribute.Attribute;
import main.java.declare.constraint.Constraint;

public class DeclareModel {
	private Set<Activity> activities;
	private Set<Attribute> attributes;
	private Map<Activity,Set<Attribute>> bindings;
	private Set<Constraint> constraints;
	
	public DeclareModel(Set<Activity> activities, Set<Attribute> attributes, Map<Activity,Set<Attribute>> bindings, Set<Constraint> constraints) {
		this.activities = activities;
		this.attributes = attributes;
		this.bindings = bindings;
		this.constraints = constraints;
	}

	public Set<Activity> getActivities() {
		return activities;
	}

	public Set<Attribute> getAttributes() {
		return attributes;
	}
	
	public Map<Activity,Set<Attribute>> getBindings() {
		return bindings;
	}

	public Set<Constraint> getConstraints() {
		return constraints;
	}
}

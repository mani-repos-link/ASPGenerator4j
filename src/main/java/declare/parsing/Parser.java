package main.java.declare.parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import main.java.declare.attribute.Attribute;
import main.java.declare.attribute.CategoricalAttribute;
import main.java.declare.attribute.FloatAttribute;
import main.java.declare.attribute.IntegerAttribute;
import main.java.declare.constraint.BinaryConstraint;
import main.java.declare.constraint.Constraint;
import main.java.declare.constraint.UnaryConstraint;
import main.java.declare.constraint.condition.Predicate;
import main.java.declare.constraint.condition.TimeCondition;
import main.java.declare.core.Activity;
import main.java.declare.core.DeclareModel;
import main.java.declare.core.Template;

public class Parser {
	private static String numericRegex = "[+-]?([0-9]*[.])?[0-9]+";
	private static String constraintRegex = "(.+)\\[(.+)\\]\\s*((\\|[^\\|]*)+)";	// ".+\\[.+\\]\\s*(\\|[^\\|\\n\\r]*){0,2}"
	
	private static List<String> activityLines;
	private static List<String> attributeLines;
	private static List<String> bindingLines;
	private static List<String> constraintLines;
	
	private Parser() {
	}
    
    public static DeclareModel parse(Path modelPath) throws IOException, NoSuchElementException {
    	activityLines = new ArrayList<>();
    	attributeLines = new ArrayList<>();
    	bindingLines = new ArrayList<>();
    	constraintLines = new ArrayList<>();
    	
    	Files.lines(modelPath).forEach(line -> {
    		if (isActivity(line))
    			activityLines.add(line);
    		else if (isAttribute(line))
    			attributeLines.add(line);
    		else if (isBinding(line))
    			bindingLines.add(line);
    		else if (isConstraint(line))
    			constraintLines.add(line);
    	});
    	
    	Set<Activity> activities = new HashSet<>();
    	for (String line : activityLines) {
			String name = line.substring("activity ".length());
			activities.add(new Activity(name));
    	}
    	
    	
    	Set<Attribute> attributes = new HashSet<>();
    	for (String line : attributeLines) {
    		String[] split = line.split(":\\s+", 2);
    		
    		if (split[1].matches("integer\\s+between\\s+"+numericRegex+"\\s+and\\s+"+numericRegex)) {
    			String[] numSplit = split[1].split("\\s+");
            	int lowerBound = Integer.parseInt(numSplit[2]);
            	int upperBound = Integer.parseInt(numSplit[4]);
            	attributes.add(new IntegerAttribute(split[0], lowerBound, upperBound));
            
            } else if (split[1].matches("float\\s+between\\s+"+numericRegex+"\\s+and\\s+"+numericRegex)) {
            	String[] numSplit = split[1].split("\\s+");
            	double lowerBound = Double.parseDouble(numSplit[2]);
            	double upperBound = Double.parseDouble(numSplit[4]);
            	attributes.add(new FloatAttribute(split[0], lowerBound, upperBound));
            
            } else {
            	String[] valSplit = split[1].split(",\\s+");
                attributes.add(new CategoricalAttribute(split[0], new HashSet<>(Arrays.asList(valSplit))));
            }
    	}
    	
    	
    	Map<Activity, Set<Attribute>> bindings = new HashMap<>();
    	for (String line : bindingLines) {
    		String[] split = line.substring("bind ".length()).split(":\\s+", 2);
    		
    		Activity act = activities.stream()
    				.filter(a -> a.getName().equals(split[0]))
    				.findFirst()
    				.orElseThrow(() -> new NoSuchElementException("Syntax error - Activity '" + split[0] + "' not defined!"));
    		
    		Set<Attribute> boundAttribs = new HashSet<>();
    		for (String attName : split[1].split(",\\s+")) {
    			Attribute att = attributes.stream()
    					.filter(a -> a.getName().equals(attName))
    					.findFirst()
    					.orElseThrow(() -> new NoSuchElementException("Syntax error - Attribute '" + attName + "' not defined!"));
    			
    			boundAttribs.add(att);
    		}
    		
    		bindings.put(act, boundAttribs);
    	}
    	
    	
    	Set<Constraint> constraints = new HashSet<>();
    	Pattern constraintPattern = Pattern.compile(constraintRegex);
    	for (String line : constraintLines) {
    		Matcher m = constraintPattern.matcher(line);
    		if (m.find()) {
    			Template template = Template.getByTemplateName(m.group(1));
    			Constraint c;
    			
    			if (!template.isBinary()) {
    				Activity activation = activities.stream()
    						.filter(activity -> activity.getName().equals(m.group(2)))
    						.findFirst()
    						.orElseThrow(() -> new NoSuchElementException("Syntax error - Activity '" + m.group(2) + "' not defined!"));
        			
        			String[] conditions = m.group(3).split("\\s*\\|", -1);	// conditions[0] is meaningless
        			
        			Predicate activationCond = Predicate.getPredicateFromString(conditions[1].replace("A.", ""), attributes);
        			
        			Set<Attribute> unboundAttribs = activationCond.getUnboundAttributes(bindings.get(activation));
        			if (!unboundAttribs.isEmpty()) {
        				throw new NoSuchElementException("Constraint '" + line + "'\n"
        						+ "\tAttributes [" + String.join(", ", unboundAttribs.stream().map(item -> item.getName()).collect(Collectors.toList())) + "]"
        						+ " are not bound to activity '" + activation + "'"
        				);
        			}
        			
        			TimeCondition timeCond = TimeCondition.getTimeConditionFromString(conditions[2]);
        			
        			c = new UnaryConstraint(template, activation, activationCond, timeCond);
        			
    			} else {
    				
    				String[] actNames = m.group(2).split(", ", 2);
    				String activationName = template.getReverseActivationTarget() ? actNames[1] : actNames[0];
    				String targetName = template.getReverseActivationTarget() ? actNames[0] : actNames[1];
    				
        			Activity activation = activities.stream()
        					.filter(activity -> activity.getName().equals(activationName))
        					.findFirst()
        					.orElseThrow(() -> new NoSuchElementException("Syntax error - Activity '" + activationName + "' not defined!"));
        			
        			Activity target = activities.stream()
        					.filter(activity -> activity.getName().equals(targetName))
        					.findFirst()
        					.orElseThrow(() -> new NoSuchElementException("Syntax error - Activity '" + targetName + "' not defined!"));
        			
        			String[] conditions = m.group(3).split("\\s*\\|", -1);	// conditions[0] is meaningless
        			
        			Predicate activationCond = Predicate.getPredicateFromString(conditions[1].replace("A.", ""), attributes);
        			
        			Set<Attribute> unboundAttribs = activationCond.getUnboundAttributes(bindings.get(activation));
        			if (!unboundAttribs.isEmpty()) {
        				throw new NoSuchElementException("Constraint '" + line + "'\n"
        						+ "\tAttributes [" + String.join(", ", unboundAttribs.stream().map(item -> item.getName()).collect(Collectors.toList())) + "]"
        						+ " are not bound to activity '" + activation + "'"
        				);
        			}
        			
        			Predicate targetCond = Predicate.getPredicateFromString(conditions[2].replace("T.", ""), attributes);
        			
        			unboundAttribs = targetCond.getUnboundAttributes(bindings.get(target));
        			if (!unboundAttribs.isEmpty()) {
        				throw new NoSuchElementException("Constraint '" + line + "'\n"
        						+ "\tAttributes [" + String.join(", ", unboundAttribs.stream().map(item -> item.getName()).collect(Collectors.toList())) + "]"
        						+ " are not bound to activity '" + activation + "'"
        				);
        			}
        			
        			TimeCondition timeCond = TimeCondition.getTimeConditionFromString(conditions[3]);
        			
        			c = new BinaryConstraint(template, activation, target, activationCond, targetCond, timeCond);
    			}
    			
    			constraints.add(c);
    		}
    	}
    	
    	return new DeclareModel(activities, attributes, bindings, constraints);
    }
    
    public static boolean isActivity(String line) {
        return line.startsWith("activity ");
    }
/*
    public static boolean isTraceAttribute(String line) {
        return line.startsWith("trace ");
    }
*/
    public static boolean isAttribute(String line) {
    	// regex for numeric data lines ".+:\\s+((integer|float)\\s+between\\s+-?\\d+(\\.\\d+)?\\s+and\\s+-?\\d+(\\.\\d+)?)"
    	return line.matches(".+:\\s+.+") && !isActivity(line) && !isBinding(line) /*&& !isTraceAttribute(line)*/;
    }

    public static boolean isBinding(String line) {
        return line.startsWith("bind ");
    }

    public static boolean isConstraint(String line) {
        return line.matches(constraintRegex);
    }
}

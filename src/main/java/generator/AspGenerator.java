package main.java.generator;

import main.java.declare.attribute.Attribute;
import main.java.declare.attribute.CategoricalAttribute;
import main.java.declare.attribute.FloatAttribute;
import main.java.declare.attribute.IntegerAttribute;
import main.java.declare.constraint.BinaryConstraint;
import main.java.declare.constraint.Constraint;
import main.java.declare.constraint.UnaryConstraint;
import main.java.declare.constraint.condition.*;
import main.java.declare.core.Activity;
import main.java.declare.core.DeclareModel;
import main.java.declare.parsing.Parser;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.*;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.solving.Model;
import org.potassco.clingo.solving.SolveHandle;
import org.potassco.clingo.solving.SolveMode;
import org.potassco.clingo.symbol.Function;
import org.potassco.clingo.symbol.Symbol;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AspGenerator {

	// Clingo accepts seeds in range [0, 2^32-1]
	private static final int MIN_CLINGO_SEED = 0;
	private static final int MAX_CLINGO_SEED = (int) Math.pow(2, 32) - 1;

	private static Random rnd = new Random();

	/*
    Since ASP isn't able to handle floating attributes (infinite values in a range), here they are
    discretized with a precision equals to the least significant floating digit of the range bounds and predicate values too.

    E.g.:
    The line "attribute: float between 1.120 and 2.0" will be treated as "attribute: integer between 112 and 200",
    then each computed integer will be scaled again to the correct floating that it represents.
    Note that if a predicate "attribute > 1.808" was present, then the values will be scaled by 3 digits,
    for example the data line would be "attribute: integer between 1120 and 2000".

    Below, the map floatingAttributes contains entries formed by the name of the attribute and the number
    of significant digit to scale.
    */
	private static Map<Attribute, Integer> floatingAttributes;
	private static SortedSet<Integer> extractedSeeds;
	private static SortedSet<Integer> invalidLengths;
	private static XLog generatedLog;

	private AspGenerator() {
	}

	public static XLog generateLog(
			Path declModelPath,
			int minTraceLength,
			int maxTraceLength,
			int logLength,
			LocalDateTime startTime,
			Duration interval) throws InterruptedException, IOException {

		floatingAttributes = new HashMap<>();
		generatedLog = new XLogImpl(new XAttributeMapImpl());
		extractedSeeds = new TreeSet<>();
		invalidLengths = new TreeSet<>();

		DeclareModel declModel = Parser.parse(declModelPath);

		String lpModelString = decl2lp(declModel);

		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (int i = 0; i < logLength; i++) {
			pool.execute(() -> {
				try {
					getXTraceFromClingo(declModel, lpModelString, minTraceLength, maxTraceLength, startTime, interval);
				} catch (IOException | InterruptedException e) {
					pool.shutdownNow();
					e.printStackTrace();
				}
			});
		}

		pool.shutdown();
		pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		return generatedLog;
	}

	private static void getXTraceFromClingo(
			DeclareModel declModel,
			String lpModel,
			int minTraceLength,
			int maxTraceLength,
			LocalDateTime startTime,
			Duration interval) throws IOException, InterruptedException {

		try {
			boolean isInvalidLength;
			do {
				int length;
				synchronized (invalidLengths) {
					length = getRandomWithoutExcluded(rnd, minTraceLength, maxTraceLength, invalidLengths);
				}

				int seed;
				synchronized (extractedSeeds) {
					seed = getRandomWithoutExcluded(rnd, MIN_CLINGO_SEED, MAX_CLINGO_SEED, extractedSeeds);
					extractedSeeds.add(seed);
				}
				Control control = new Control("-c",
						"t=" + length,
						String.valueOf(1),    // Means only one trace per single clingo run
						// The next options should make the output the more random as possible
						// (their meaning can be found on clingo user guide)
						"--project",
						"--sign-def=rnd",
						"--rand-freq=0.9",
						"--restart-on-model",
						"--seed=" + seed);
				control.add(lpModel);
				control.add(Global.LP_GENERATION_PROBLEM);
				control.add(Global.LP_TEMPLATES);
				control.ground();

				SolveHandle handle = control.solve(SolveMode.YIELD);
				isInvalidLength = handle.getSolveResult().unsatisfiable();

				while (handle.hasNext()) {
					Model model = handle.next();
					XTrace t = getXTraceFromClingoTrace(declModel, model, startTime, interval);
					synchronized (generatedLog) {
						generatedLog.add(t);
					}
				}

				control.close();

				if (isInvalidLength)
					synchronized (invalidLengths) {
						invalidLengths.add(length);
					}

			} while (isInvalidLength);

		} catch (IllegalArgumentException e) {
			throw new IOException("Cannot generate traces with this range of event numbers.");
		}
	}

	private static int getRandomWithoutExcluded(Random rnd, int start, int end, SortedSet<Integer> excluded) throws IllegalArgumentException {
		int newRandom = start + rnd.nextInt(end - start + 1 - excluded.size());

		if (!excluded.headSet(newRandom + 1).isEmpty()) {
			if (!excluded.contains(newRandom + excluded.headSet(newRandom + 1).size())) {
				newRandom += excluded.headSet(newRandom + 1).size();

			} else {
				int tmp = newRandom;
				while (excluded.contains(newRandom + excluded.headSet(tmp + 1).size()))
					tmp = newRandom + excluded.headSet(tmp + 1).size();

				newRandom = tmp + 1;
			}
		}

		return newRandom;
	}

	private static String decl2lp(DeclareModel declModel) {
		StringBuilder lpBuilder = new StringBuilder();
		String ls = System.lineSeparator();

		// Checking at first attribute and constraint definitions to identify the maximum precision of floating attributes
		setFloatMaxPrecision(declModel.getAttributes(), declModel.getConstraints());

		for (Activity act : declModel.getActivities()) {
			lpBuilder.append("activity(" + act.getEncodedName() + ").");
			lpBuilder.append(ls);
		}

		for (Attribute att : declModel.getAttributes()) {
			String attName = att.getEncodedName();

			if (att instanceof CategoricalAttribute) {
				CategoricalAttribute catAtt = (CategoricalAttribute) att;

				for (String val : catAtt.getEncodedValues()) {
					lpBuilder.append("value(" + attName + "," + val + ").");
					lpBuilder.append(ls);
				}

			} else {
				if (att instanceof IntegerAttribute) {
					IntegerAttribute intAtt = (IntegerAttribute) att;
					lpBuilder.append("value(" + attName + ","
							+ intAtt.getLowerBound() + ".."
							+ intAtt.getUpperBound() + ")."
					);

				} else {    // FloatAttribute
					FloatAttribute floatAtt = (FloatAttribute) att;
					int digitsToScale = floatingAttributes.get(floatAtt);

					lpBuilder.append("value(" + attName + ","
							+ Math.round(floatAtt.getLowerBound() * Math.pow(10, digitsToScale)) + ".."
							+ Math.round(floatAtt.getUpperBound() * Math.pow(10, digitsToScale)) + ")."
					);
				}

				lpBuilder.append(ls);
			}
		}

		for (Map.Entry<Activity, Set<Attribute>> binding : declModel.getBindings().entrySet()) {
			Activity act = binding.getKey();

			for (Attribute boundAtt : binding.getValue()) {
				lpBuilder.append("has_attribute(" + act.getEncodedName() + "," + boundAtt.getEncodedName() + ").");
				lpBuilder.append(ls);
			}
		}

		int constraintIndex = 0;
		for (Constraint c : declModel.getConstraints()) {
			lpBuilder.append("template(" + constraintIndex + ",\"" + c.getTemplate() + "\").");
			lpBuilder.append(ls);

			if (c instanceof UnaryConstraint) {
				lpBuilder.append("activation(" + constraintIndex + "," + c.getActivation().getEncodedName() + ").");
				lpBuilder.append(ls);

				for (String lpPred : getLPConditionsFromRootPredicate(c.getActivationCond(), constraintIndex, "activation"))
					lpBuilder.append(lpPred + ls);

			} else if (c instanceof BinaryConstraint) {
				BinaryConstraint binConstr = (BinaryConstraint) c;

				// Workaround for templates that reverse the order of activation and target activities (i.e. Precedence ones)
				// Since ASP templates are encoded without this reversion, activation and target activities and conditions must be changed accordingly
				if (!binConstr.getTemplate().getReverseActivationTarget()) {

					lpBuilder.append("activation(" + constraintIndex + "," + binConstr.getActivation().getEncodedName() + ").");
					lpBuilder.append(ls);

					for (String lpPred : getLPConditionsFromRootPredicate(binConstr.getActivationCond(), constraintIndex, "activation"))
						lpBuilder.append(lpPred + ls);

					lpBuilder.append("target(" + constraintIndex + "," + binConstr.getTarget().getEncodedName() + ").");
					lpBuilder.append(ls);

					for (String lpPred : getLPConditionsFromRootPredicate(binConstr.getTargetCond(), constraintIndex, "target"))
						lpBuilder.append(lpPred + ls);

				} else {

					lpBuilder.append("activation(" + constraintIndex + "," + binConstr.getTarget().getEncodedName() + ").");
					lpBuilder.append(ls);

					for (String lpPred : getLPConditionsFromRootPredicate(binConstr.getTargetCond(), constraintIndex, "activation"))
						lpBuilder.append(lpPred + ls);

					lpBuilder.append("target(" + constraintIndex + "," + binConstr.getActivation().getEncodedName() + ").");
					lpBuilder.append(ls);

					for (String lpPred : getLPConditionsFromRootPredicate(binConstr.getActivationCond(), constraintIndex, "target"))
						lpBuilder.append(lpPred + ls);
				}
			}

			constraintIndex++;
		}

		return lpBuilder.toString();
	}

	private static void setFloatMaxPrecision(Set<Attribute> attributes, Set<Constraint> constraints) {
		for (Attribute att : attributes) {
			if (att instanceof FloatAttribute) {
				FloatAttribute floatAtt = (FloatAttribute) att;
				setFloatMaxPrecision(floatAtt, String.valueOf(floatAtt.getLowerBound()));
				setFloatMaxPrecision(floatAtt, String.valueOf(floatAtt.getUpperBound()));
			}
		}

		for (Constraint c : constraints) {
			setFloatMaxPrecision(c.getActivationCond());

			if (c.getTemplate().isBinary())
				setFloatMaxPrecision(((BinaryConstraint) c).getTargetCond());
		}
	}

	private static void setFloatMaxPrecision(Attribute attribute, String floatString) {
		Pattern significantDecimalPartPattern = Pattern.compile("\\.(\\d*[1-9])");
		Matcher m = significantDecimalPartPattern.matcher(floatString);

		String significantDecimalPart = m.find() ? m.group(1) : "";

		if (!floatingAttributes.containsKey(attribute))
			floatingAttributes.put(attribute, significantDecimalPart.length());

		else if (significantDecimalPart.length() > floatingAttributes.get(attribute))
			floatingAttributes.replace(attribute, significantDecimalPart.length());
	}

	private static void setFloatMaxPrecision(Predicate root) {
		if (root instanceof ConcretePredicate) {
			ConcretePredicate p = (ConcretePredicate) root;

			if (floatingAttributes.keySet().contains(p.getAttribute()))
				setFloatMaxPrecision(p.getAttribute(), p.getValue());

		} else {
			for (Predicate p : root.getChildren())
				setFloatMaxPrecision(p);
		}
	}

	private static List<String> getLPConditionsFromRootPredicate(Predicate root, int constraintIndex, String mode) throws UnsupportedOperationException {
		List<String> conditions = new ArrayList<>();
		List<String> childLeftParts = new ArrayList<>();

		String prefix = mode.equals("activation") ? "activation" : "correlation";
		String leftPartCondition = prefix + "_condition(" + constraintIndex + ",T)";

		if (root instanceof ConcretePredicate) {
			getLPConditionFromSimplePredicate(conditions, (ConcretePredicate) root, constraintIndex, mode);
			String childCondition = conditions.get(conditions.size() - 1);
			conditions.add(leftPartCondition + " :- " + childCondition.substring(0, childCondition.indexOf(':')).trim() + ".");

		} else {
			if (!root.getChildren().isEmpty()) {
				for (Predicate child : root.getChildren()) {
					if (child instanceof ConcretePredicate)
						getLPConditionFromSimplePredicate(conditions, (ConcretePredicate) child, constraintIndex, mode);
					else
						getLPConditionFromNestedPredicate(conditions, (LogicPredicate) child, constraintIndex, mode);

					String childCondition = conditions.get(conditions.size() - 1);
					childLeftParts.add(childCondition.substring(0, childCondition.indexOf(':')).trim());
				}

				switch ((LogicOperator) root.getOperator()) {
					case AND:
						conditions.add(leftPartCondition + " :- " + String.join(",", childLeftParts) + ".");
						break;
					case OR:
						for (String c : childLeftParts)
							conditions.add(leftPartCondition + " :- " + c + ".");
						break;
					default:
						throw new UnsupportedOperationException(
								"Operator: " + root.getOperator() + " is not yet supported!"
						);
				}
			}
		}

		if (conditions.isEmpty())    // When there are no related conditions, LP format needs the time(T) predicate to be added
			conditions.add(leftPartCondition + " :- time(T).");

		return conditions;
	}

	private static void getLPConditionFromNestedPredicate(List<String> conditions, LogicPredicate nested, int constraintIndex, String mode) throws UnsupportedOperationException {

		List<String> childLeftParts = new ArrayList<>();
		for (Predicate child : nested.getChildren()) {
			if (child instanceof ConcretePredicate)
				getLPConditionFromSimplePredicate(conditions, (ConcretePredicate) child, constraintIndex, mode);
			else
				getLPConditionFromNestedPredicate(conditions, (LogicPredicate) child, constraintIndex, mode);

			String childCondition = conditions.get(conditions.size() - 1);
			childLeftParts.add(childCondition.substring(0, childCondition.indexOf(':')).trim());
		}

		String prefix = mode.equals("activation") ? "act" : "corr";
		String leftPart = prefix + "_p" + conditions.size() + "(" + constraintIndex + ",T)";
		switch ((LogicOperator) nested.getOperator()) {
			case AND:
				conditions.add(leftPart + " :- " + String.join(",", childLeftParts) + ".");
				break;
			case OR:
				for (String c : childLeftParts)
					conditions.add(leftPart + " :- " + c + ".");
				break;
			default:
				throw new UnsupportedOperationException(
						"Operator: " + nested.getOperator() + " is not yet supported!"
				);
		}
	}

	private static void getLPConditionFromSimplePredicate(List<String> conditions, ConcretePredicate attrPred, int constraintIndex, String mode) throws UnsupportedOperationException {
		String prefix = mode.equals("activation") ? "act" : "corr";
		String leftPart = prefix + "_p" + conditions.size() + "(" + constraintIndex + ",T)";

		List<String> rightParts = new ArrayList<>();

		Attribute att = attrPred.getAttribute();
		ConcreteOperator op = (ConcreteOperator) attrPred.getOperator();
		switch (op) {
			case EQ:
			case NEQ:
			case GEQ:
			case GT:
			case LEQ:
			case LT:
				boolean isFloatAtt = floatingAttributes.keySet().contains(att);
				String value;
				if (isFloatAtt) { // Need to scale floatings because ASP works only with integers
					int scaleNum = floatingAttributes.get(att);
					double doubleValue = Double.valueOf(attrPred.getValue()) * Math.pow(10, scaleNum);

					value = String.valueOf(Math.round(doubleValue));

				} else {
					value = attrPred.getValue();
				}

				rightParts.add("assigned_value(" + att.getEncodedName() + ",V,T),V" + attrPred.getOperator() + value + ".");
				break;

			case IN: {
				CategoricalAttribute concAtt = (CategoricalAttribute) att;
				for (String val : attrPred.getValue().substring(1, attrPred.getValue().length()).split(",\\s+"))
					rightParts.add("assigned_value(" + att.getEncodedName() + "," + concAtt.getEncodedValue(val) + ",T).");
				break;
			}
			case NOT_IN: {
				CategoricalAttribute concAtt = (CategoricalAttribute) att;
				List<String> values = new ArrayList<>();
				for (String val : attrPred.getValue().substring(1, attrPred.getValue().length()).split(",\\s+"))
					values.add("assigned_value(" + att.getEncodedName() + "," + concAtt.getEncodedValue(val) + ",T)");

				rightParts.add(String.join(",", values) + ".");
				break;
			}
			case IS: {
				CategoricalAttribute concAtt = (CategoricalAttribute) att;
				rightParts.add("assigned_value(" + concAtt.getEncodedName() + "," + concAtt.getEncodedValue(attrPred.getValue()) + ",T).");
				break;
			}
			case IS_NOT: {
				CategoricalAttribute concAtt = (CategoricalAttribute) att;
				rightParts.add("not assigned_value(" + concAtt.getEncodedName() + "," + concAtt.getEncodedValue(attrPred.getValue()) + ",T).");
				break;
			}
			default:
				throw new UnsupportedOperationException(
						"Operator: " + attrPred.getOperator() + " is not yet supported!"
				);
		}

		for (String rightPart : rightParts)
			conditions.add(leftPart + " :- " + rightPart);
	}

	private static XTrace getXTraceFromClingoTrace(DeclareModel declModel, Model clingoTrace, LocalDateTime startTime, Duration interval) {
		int traceLength = Math.toIntExact(Arrays.stream(clingoTrace.getSymbols()).filter(symb -> ((Function) symb).getName().equals("trace")).count());
		XTrace trace = new XTraceImpl(new XAttributeMapImpl());
		for (int i = 0; i < traceLength; i++)
			trace.add(new XEventImpl(new XAttributeMapImpl()));

		// Generating random timestamps for events
		long startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		long intervalMillis = interval.getSeconds() * 1000;
		List<Long> timestamps = rnd.longs(traceLength, startMillis, startMillis + intervalMillis + 1).sorted().boxed().collect(Collectors.toList());

		// Translating clingo symbols
		for (Symbol clingoEvt : clingoTrace.getSymbols()) {
			List<String> args = Arrays.stream(((Function) clingoEvt).getArguments()).map(symb -> symb.toString()).collect(Collectors.toList());
			int pos = Integer.parseInt(args.get(args.size() - 1)) - 1;    // The position is always the last split value inside a clingo element

			XEvent evt = trace.get(pos);

			switch (((Function) clingoEvt).getName()) {
				case "trace":    // Activity of the event
					// Restoring real (decoded) activity name
					String actName = declModel.getActivities().stream()
							.filter(act -> act.getEncodedName().equals(args.get(0)))
							.map(act -> act.getName())
							.findFirst().get();

					XConceptExtension.instance().assignName(evt, actName);
					// Assigning timestamp and transition to the event
					XLifecycleExtension.instance().assignStandardTransition(evt, StandardModel.COMPLETE);
					XTimeExtension.instance().assignTimestamp(evt, timestamps.get(pos));
					break;

				case "assigned_value":    // Attribute of the event
					Attribute att = declModel.getAttributes().stream()
							.filter(item -> item.getEncodedName().equals(args.get(0)))
							.findFirst().get();
					// Restoring real (decoded) attribute name
					String attName = att.getName();

					XAttribute xAttribute;
					if (att instanceof FloatAttribute) {
						double val = Integer.parseInt(args.get(1)) * Math.pow(10, -floatingAttributes.get(att));
						xAttribute = new XAttributeContinuousImpl(attName, val);

					} else if (att instanceof IntegerAttribute) {
						xAttribute = new XAttributeDiscreteImpl(attName, Long.parseLong(args.get(1)));

					} else {
						// Restoring real (decoded) categorical attribute value
						String value = ((CategoricalAttribute) att).getDecodedValue(args.get(1));
						xAttribute = new XAttributeLiteralImpl(attName, value);
					}

					trace.get(pos).getAttributes().put(attName, xAttribute);
					break;

				default:
					throw new NoSuchElementException("Error while running clingo. Unknown symbol '" + ((Function) clingoEvt).getName() + "'");
			}
		}

		return trace;
	}
}

package main.java.declare.core;

import java.util.NoSuchElementException;

public enum Template {
	ABSENCE("Absence", false, false, false),
	ABSENCE2("Absence2", false, false, false),
	ABSENCE3("Absence3", false, false, false),
	END("End", false, false, false),
	EXACTLY1("Exactly1", false, false, false),
	EXACTLY2("Exactly2", false, false, false),
	EXISTENCE("Existence", false, false, false),
	EXISTENCE2("Existence2", false, false, false),
	EXISTENCE3("Existence3", false, false, false),
	INIT("Init", false, false, false),
	ALTERNATE_PRECEDENCE("Alternate Precedence", true, false, true),
	ALTERNATE_RESPONSE("Alternate Response", true, false, false),
	ALTERNATE_SUCCESSION("Alternate Succession", true, false, false),
	CHAIN_PRECEDENCE("Chain Precedence", true, false, true),
	CHAIN_RESPONSE("Chain Response", true, false, false),
	CHAIN_SUCCESSION("Chain Succession", true, false, false),
	CHOICE("Choice", true, false, false),
	CO_EXISTENCE("Co-Existence", true, false, false),
	EXCLUSIVE_CHOICE("Exclusive Choice", true, false, false),
	PRECEDENCE("Precedence", true, false, true),
	RESPONDED_EXISTENCE("Responded Existence", true, false, false),
	RESPONSE("Response", true, false, false),
	SUCCESSION("Succession", true, false, false),
	NOT_CHAIN_PRECEDENCE("Not Chain Precedence", true, true, true),
	NOT_CHAIN_RESPONSE("Not Chain Response", true, true, false),
	NOT_CHAIN_SUCCESSION("Not Chain Succession", true, true, false),
	NOT_CO_EXISTENCE("Not Co-Existence", true, true, false),
	NOT_PRECEDENCE("Not Precedence", true, true, true),
	NOT_RESPONDED_EXISTENCE("Not Responded Existence", true, true, false),
	NOT_RESPONSE("Not Response", true, true, false),
	NOT_SUCCESSION("Not Succession", true, true, false);

	private String templateName;
	private boolean isBinary;
	private boolean isNegative;
	private boolean reverseActivationTarget;

	private Template(String templateName, boolean isBinary, boolean isNegative, boolean reverseActivationTarget) {
		this.templateName = templateName;
		this.isBinary = isBinary;
		this.isNegative = isNegative;
		this.reverseActivationTarget = reverseActivationTarget;
	}

	@Override
	public String toString() {
		return templateName;
	}

	public boolean isBinary() {
		return isBinary;
	}

	public boolean isNegative() {
		return isNegative;
	}

	public boolean getReverseActivationTarget() {
		return reverseActivationTarget;
	}

	public String getDisplayText() {
		if (isBinary)
			return templateName + (reverseActivationTarget ? "[B, A]" : "[A, B]");
		else
			return templateName + "[A]";
	}

	public static Template getByTemplateName(String templateName) throws NoSuchElementException {
		for (Template t : values())
			if (templateName.equals(t.toString()))
				return t;
		
		throw new NoSuchElementException("No template with name: " + templateName);
	}
}


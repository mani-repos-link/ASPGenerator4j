package main.java.declare.attribute;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import main.java.declare.encoding.Encoder;

public class CategoricalAttribute extends Attribute {
	private Set<String> values;
	private String[] encodedValues;
	
	public CategoricalAttribute(String name, Set<String> values) {
		super(name);
		this.values = values;
//		this.encodedValues = Encoder.getAttributeValuesEncoding(this.getEncodedName(), values.size());
		this.encodedValues = Encoder.getAttributeValuesEncoding(this.values.toArray(String[]::new));
	}
	
	public Set<String> getValues() {
		return this.values;
	}
	
	public Set<String> getEncodedValues() {
		return new LinkedHashSet<String>(Arrays.asList(this.encodedValues));
	}
	
	public String getDecodedValue(String encValue) {
		int ctr = 0;
		Iterator<String> it = values.iterator();
		
		while (it.hasNext()) {
			String val = it.next();
			if (encodedValues[ctr].equals(encValue))
				return val;
			ctr++;
		}
		
		throw new NoSuchElementException("Error while building XES trace from clingo.");
	}
	
	public String getEncodedValue(String realValue) {
		if (!values.contains(realValue))
			throw new NoSuchElementException("Syntax error - Attribute '" + realValue + "' not defined!");
		
		int ctr = 0;
		Iterator<String> it = values.iterator();
		
		while (it.hasNext()) {
			if (it.next().equals(realValue))
				break;
			ctr++;
		}
		
		return encodedValues[ctr];
	}
	
	@Override
	public String toString() {
		return this.getName() + ": " + String.join(", ", values);
	}
}

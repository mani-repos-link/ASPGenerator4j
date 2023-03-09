package main.java.declare.encoding;

public final class Encoder {
	private static long activityCtr;
	private static long attributeCtr;
	
	private Encoder() {
		activityCtr = 0;
		attributeCtr = 0;
	}
	
	public static String getActivityEncoding() {
		return "act" + (activityCtr++);
	}
	
	public static String getAttributeEncoding() {
		return "att" + (attributeCtr++);
	}
	
	public static String[] getAttributeValuesEncoding(String encAttName, int valuesSize) {
		String[] encVals = new String[valuesSize];
		
		for (int ctr=0; ctr < valuesSize; ctr++)
			encVals[ctr] = encAttName + "val" + ctr;
		
		return encVals;
	}
}

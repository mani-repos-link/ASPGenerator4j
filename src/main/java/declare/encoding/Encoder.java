package main.java.declare.encoding;

public final class Encoder {
	private static long activityCtr;
	private static long attributeCtr;
	
	private Encoder() {
		activityCtr = 0;
		attributeCtr = 0;
	}
	
	public static String getActivityEncoding(String nm) {
//		return "act" + (activityCtr++);
		return encodeStr(nm);
	}
	
	public static String getAttributeEncoding(String nm) {
//		return "att" + (attributeCtr++);
		return encodeStr(nm);
	}

	private static String encodeStr(String nm) {
		nm = nm.trim();
		Character s =  nm.charAt(0);
		if (Character.isUpperCase(s)){
			nm = "l" + nm;
		}
		nm = nm.replace(" ", "_");
		nm = nm.replace(":", "__");
		nm = nm.replace(",", "__");
		nm = nm.replace("=", "__");
		return nm;
	}
	
	public static String[] getAttributeValuesEncoding(String encAttName, int valuesSize) {
		String[] encVals = new String[valuesSize];
		
		for (int ctr=0; ctr < valuesSize; ctr++)
//			encVals[ctr] = encAttName + "val" + ctr;
			encVals[ctr] = encodeStr(encAttName + ctr);
		return encVals;
	}
	public static String[] getAttributeValuesEncoding(String[] encAttName) {
		String[] encVals = new String[encAttName.length];

		for (int ctr=0; ctr < encAttName.length; ctr++)
			encVals[ctr] = encodeStr(encAttName[ctr]);
		return encVals;
	}
}

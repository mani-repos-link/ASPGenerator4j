package main.java.declare.constraint.condition;

import java.util.Objects;

public class TimeCondition {
	int lowerBound;
	int upperBound;
	TimeUnitId unitId;
	
	public TimeCondition(int lowerBound, int upperBound, TimeUnitId unitId) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.unitId = unitId;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public TimeUnitId getUnitId() {
		return unitId;
	}
	
	public static TimeCondition getTimeConditionFromString(String str) {
		if (str.isBlank())
			return null;
		
		String[] split = str.trim().split(",");
		TimeCondition.TimeUnitId timeUnit;
		switch (split[2]) {
		case "s":
			timeUnit = TimeCondition.TimeUnitId.SECOND;
			break;
		case "m":
			timeUnit = TimeCondition.TimeUnitId.MINUTE;
			break;
		case "h":
			timeUnit = TimeCondition.TimeUnitId.HOUR;
			break;
		case "d":
			timeUnit = TimeCondition.TimeUnitId.DAY;
			break;
		default:
			throw new UnsupportedOperationException("Unsupported time unit in: " + str);
		}
		
		return new TimeCondition(Integer.parseInt(split[0]), Integer.parseInt(split[1]), timeUnit);
	}
	
	@Override
	public String toString() {
		return lowerBound + "," + upperBound + "," + unitId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(lowerBound, unitId, upperBound);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeCondition other = (TimeCondition) obj;
		return lowerBound == other.lowerBound && unitId == other.unitId && upperBound == other.upperBound;
	}


	public enum TimeUnitId {
		SECOND("s"),
		MINUTE("m"),
		HOUR("h"),
		DAY("d");
		
		private String stringDisplay;
		
		private TimeUnitId(String stringDisplay) {
			this.stringDisplay = stringDisplay;
		}
		
		@Override
		public String toString() {
			return this.stringDisplay;
		}
	}
}

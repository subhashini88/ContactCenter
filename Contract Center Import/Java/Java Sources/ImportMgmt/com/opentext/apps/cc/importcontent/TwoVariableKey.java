package com.opentext.apps.cc.importcontent;

import java.util.Locale;
import java.util.Objects;

public class TwoVariableKey 
{
	private final String variableOne;
	private final String variableTwo;

	public TwoVariableKey (final String culture, final String targetReportId)
	{
		this.variableOne = (culture == null ? "" : culture);
		this.variableTwo = (targetReportId == null ? "" : targetReportId);
	}

	@Override
	public boolean equals(final Object otherObject)
	{
		boolean isEqual = false;
		if(otherObject!= null && otherObject instanceof TwoVariableKey)
		{
			final TwoVariableKey incomingKey = (TwoVariableKey) otherObject;
			isEqual = Objects.equals(incomingKey.variableTwo.toLowerCase(Locale.ENGLISH), this.variableTwo.toLowerCase(Locale.ENGLISH)) &&
					Objects.equals(incomingKey.variableOne.toLowerCase(Locale.ENGLISH), this.variableOne.toLowerCase(Locale.ENGLISH));
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.variableOne.toLowerCase(), this.variableTwo.toLowerCase());
	}

	@Override
	public String toString() 
	{
		return "Variable One : " + this.variableOne + ", Variable Two : " + this.variableTwo;
	}
}

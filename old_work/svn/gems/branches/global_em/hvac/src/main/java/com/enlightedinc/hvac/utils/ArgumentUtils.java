package com.enlightedinc.hvac.utils;

import java.awt.Color;
import java.util.Collection;


/**
 * A utility class which checks for null parameters, ranges which would have
 * been passed as arguments to code
 * 
 */
public final class ArgumentUtils {

	public static boolean isNull(final Object argumentValue) {
		return (argumentValue == null);
	}

	public static boolean isNullOrEmpty(final String argumentValue) {
		return (isNull(argumentValue) || argumentValue.length() ==0);
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNullOrEmpty(Collection argumentValue) {
		return (isNull(argumentValue) || argumentValue.size() == 0);
	}

	public static void checkNull(final String argumentName, final Object argumentValue) {
		if (isNull(argumentValue))
			throw new IllegalArgumentException("The supplied property " + argumentName + " was null");
	}

	public static void checkNullOrEmpty(final String argumentName, final String argumentValue) {
		if (isNullOrEmpty(argumentValue))
			throw new IllegalArgumentException("The supplied String property " + argumentName + " was null or empty");
	}

	public static Color[] colors = {new Color(228,135,1),new Color(165,188,78),
		new Color(84,150,225),new Color(240,225,61),
		new Color(15,225,0),new Color(225,82,0),
		new Color(19,242,206),new Color(0,23,225),
		new Color(142,53,239),new Color(200,187,190),
		new Color(78,146,88),new Color(21,27,141),
		new Color(251,185,23),new Color(248,116,49),
		new Color(126,56,23),new Color(249,150,107),
		new Color(195,105,255),new Color(225,255,75),
		new Color(225,30,30),new Color(85,32,138)};
}

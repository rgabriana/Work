package com.ems.mvc.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component("passwordValidator")
public class PasswordValidator extends ENLValidator {

	private final static String passwordPatternKey = "key.passwords.policy";

	public boolean supports(Class<?> paramClass) {
		return String.class.equals(paramClass);
	}

	protected boolean performValidation(Object obj) {
		final String pass = (String) obj;
		final String patternStr = messageSource.getMessage(passwordPatternKey,
				null, LocaleContextHolder.getLocale());
		final Pattern pattern = Pattern.compile(patternStr);

		final Matcher matcher = pattern.matcher(pass);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

}

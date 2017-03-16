package com.sitech.crmpd.idmm2.broker.validate;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月15日 下午2:52:14
 */
public class SizeValidator implements ConstraintValidator {

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.ConstraintValidator#isValid(java.lang.Object,
	 *      java.lang.reflect.Field, java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isValid(Object object, Field field, Object value) {
		if (value == null) {
			return true;
		}
		final Size annotation = field.getAnnotation(Size.class);
		final int max = annotation.max();
		final int min = annotation.min();
		int length = 0;
		if (value instanceof CharSequence) {
			length = ((CharSequence) value).length();
		} else if (value instanceof Collection) {
			length = ((Collection) value).size();
		} else if (value instanceof Map) {
			length = ((Map) value).size();
		} else if (value.getClass().isArray()) {
			length = Array.getLength(value);
		}
		return length >= min && length <= max;
	}

}

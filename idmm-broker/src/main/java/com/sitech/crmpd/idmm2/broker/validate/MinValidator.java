package com.sitech.crmpd.idmm2.broker.validate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月15日 上午11:07:46
 */
public class MinValidator implements ConstraintValidator {

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.ConstraintValidator#isValid(java.lang.Object,
	 *      java.lang.reflect.Field, java.lang.Object)
	 */
	@Override
	public boolean isValid(Object object, Field field, Object value) {
		// null values are valid
		// null values are valid
		if (value == null) {
			return true;
		}

		// handling of NaN, positive infinity and negative infinity
		else if (value instanceof Double) {
			if ((Double) value == Double.POSITIVE_INFINITY) {
				return true;
			} else if (Double.isNaN((Double) value) || (Double) value == Double.NEGATIVE_INFINITY) {
				return false;
			}
		} else if (value instanceof Float) {
			if ((Float) value == Float.POSITIVE_INFINITY) {
				return true;
			} else if (Float.isNaN((Float) value) || (Float) value == Float.NEGATIVE_INFINITY) {
				return false;
			}
		}
		final Min annotation = field.getAnnotation(Min.class);
		final long minValue = annotation.value();
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).compareTo(BigDecimal.valueOf(minValue)) != -1;
		} else if (value instanceof BigInteger) {
			return ((BigInteger) value).compareTo(BigInteger.valueOf(minValue)) != -1;
		} else if (value instanceof Number) {
			final long longValue = ((Number) value).longValue();
			return longValue >= minValue;
		} else {
			return false;
		}
	}
}

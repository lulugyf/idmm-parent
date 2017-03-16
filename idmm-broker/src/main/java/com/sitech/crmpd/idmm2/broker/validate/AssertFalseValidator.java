package com.sitech.crmpd.idmm2.broker.validate;

import java.lang.reflect.Field;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月15日 上午11:07:46
 */
public class AssertFalseValidator implements ConstraintValidator {

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.ConstraintValidator#isValid(java.lang.Object,
	 *      java.lang.reflect.Field, java.lang.Object)
	 */
	@Override
	public boolean isValid(Object object, Field field, Object value) {
		return value == null || !((boolean) value);
	}
}

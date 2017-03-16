/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat, Inc. and/or its affiliates, and
 * individual contributors by the @authors tag. See the copyright.txt in the distribution for a full
 * listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.sitech.crmpd.idmm2.broker.validate;

import java.lang.reflect.Field;

/**
 * Defines the logic to validate a given constraint {@code A} for a given object type {@code T}.
 * <p/>
 * Implementations must comply to the following restriction:
 * <ul>
 * <li>{@code T} must resolve to a non parameterized type</li>
 * <li>or generic parameters of {@code T} must be unbounded wildcard types</li>
 * </ul>
 * <p/>
 * The annotation {@link SupportedValidationTarget} can be put on a {@code ConstraintValidator}
 * implementation to mark it as supporting cross-parameter constraints. Check out
 * {@link SupportedValidationTarget} and {@link Constraint} for more information.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintValidator {

	/**
	 *
	 * @param object
	 *            object to validate
	 * @param field
	 *            field to validate
	 * @param value
	 *            field value to validate
	 *
	 * @return {@code false} if {@code value} does not pass the constraint
	 */
	boolean isValid(Object object, Field field, Object value);
}

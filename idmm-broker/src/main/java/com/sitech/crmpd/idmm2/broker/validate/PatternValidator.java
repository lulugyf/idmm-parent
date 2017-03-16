package com.sitech.crmpd.idmm2.broker.validate;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月15日 下午2:23:11
 */
public class PatternValidator implements ConstraintValidator {
	/**
	 * {@link java.util.regex.Pattern }缓存
	 *
	 * @see LoadingCache
	 * @see CacheBuilder#newBuilder()
	 * @see CacheBuilder#weakKeys()
	 * @see CacheBuilder#weakValues()
	 * @see CacheBuilder#expireAfterAccess(long, TimeUnit)
	 * @see CacheLoader#load(Object)
	 */
	private LoadingCache<String, java.util.regex.Pattern> matchers = CacheBuilder.newBuilder()
			.weakKeys().weakValues().expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<String, java.util.regex.Pattern>() {

				@Override
				public java.util.regex.Pattern load(String key) {
					return java.util.regex.Pattern.compile(key);
				}

			});

	/**
	 * @see com.sitech.crmpd.idmm2.broker.validate.ConstraintValidator#isValid(java.lang.Object,
	 *      java.lang.reflect.Field, java.lang.Object)
	 */
	@Override
	public boolean isValid(Object object, Field field, Object value) {
		final String pattern = field.getAnnotation(Pattern.class).value();
		return matchers.getUnchecked(pattern).matcher(value.toString()).matches();
	}

}

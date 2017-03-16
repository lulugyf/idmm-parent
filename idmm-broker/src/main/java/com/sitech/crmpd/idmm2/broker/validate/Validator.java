package com.sitech.crmpd.idmm2.broker.validate;

import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月15日 上午10:44:16
 */
public class Validator {

	/** name="{@link com.sitech.crmpd.idmm2.broker.validate.Validator}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
	private static final Validator INSTANCE = new Validator();
	private Map<Class<? extends Annotation>, ConstraintValidator> handlers = Maps.newHashMap();

	/**
	 * 将默认构造方法私有化，防止实例化后使用
	 */
	private Validator() {
		handlers.put(NotNull.class, new NotNullValidator());
		handlers.put(AssertTrue.class, new AssertTrueValidator());
		handlers.put(NotEmpty.class, new NotEmptyValidator());
		handlers.put(Null.class, new NullValidator());
		handlers.put(AssertFalse.class, new AssertFalseValidator());
		handlers.put(Max.class, new MaxValidator());
		handlers.put(Min.class, new MinValidator());
		handlers.put(Pattern.class, new PatternValidator());
		handlers.put(Size.class, new SizeValidator());
	}

	/**
	 * 校验指定的对象上进行了{@link #handlers}中列举的注解类型注解的字段
	 *
	 * @param object
	 *            要校验的对象实例
	 * @throws RuntimeException
	 *             校验不通过时抛出该异常
	 */
	public void isValid(final Object object) {
		isValid(object, null);
	}

	/**
	 * 校验指定的对象上进行了{@link #handlers}中列举的注解类型注解的字段
	 *
	 * @param object
	 *            要校验的对象实例
	 * @param data
	 *            校验关联关系的值集合
	 * @throws RuntimeException
	 *             校验不通过时抛出该异常
	 */
	public <T extends UniqueKey> void isValid(final Object object,
			Map<Class<? extends UniqueKey>, Map<String, ? extends UniqueKey>> data) {
		final Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			final Field field = fields[i];
			LOGGER.trace("校验对象{}字段{}开始", object, field.getName());
			validateField(object, field, data);
			LOGGER.trace("校验对象{}字段{}通过", object, field.getName());
		}
	}

	/**
	 * 调用字段校验器进行字段值的校验
	 *
	 * @param object
	 *            要校验的对象实例
	 * @param field
	 *            要校验的对象的属性
	 * @throws ValidateException
	 *             校验不通过时抛出该异常
	 */
	private <T extends UniqueKey> void validateField(Object object, Field field,
			Map<Class<? extends UniqueKey>, Map<String, ? extends UniqueKey>> data) {
		final List<Annotation> annotations = getAnnotations(field);
		try {
			field.setAccessible(true);
			final Object value = field.get(object);
			LOGGER.trace("字段[{}]-->[{}]", field.getName(), value);
			for (int i = 0; i < annotations.size(); i++) {
				final Annotation annotation = annotations.get(i);
				final ConstraintValidator handler = handlers.get(annotation.annotationType());
				checkState(handler.isValid(object, field, value), "使用%s校验%s不通过", annotation,
						field.getName());
				LOGGER.trace("使用{}校验{}通过", annotation, field.getName());
			}
			isRefValid(field, value, data);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private <T extends UniqueKey> void isRefValid(Field field, Object value,
			Map<Class<? extends UniqueKey>, Map<String, ? extends UniqueKey>> data) {
		if (field.isAnnotationPresent(Ref.class)) {
			final Ref annotation = field.getAnnotation(Ref.class);
			final Class<?> type = annotation.value();
			final Map<String, ?> map = data.get(type);
			checkState(map != null && !map.isEmpty(), "校验字段%s值[%s]引用%s不通过，不存在引用源", field.getName(),
					value, type.getName());
			checkState(map.containsKey(value), "校验字段%s值[%s]引用%s不通过，不存在引用值，可引用值范围[%s]",
					field.getName(), value, type.getName(), map.keySet());
			LOGGER.trace("校验字段{}值[{}]引用{}通过", field.getName(), value, type.getName());
		}
	}

	/**
	 * 获取指定的字段上需要进行校验的注解类型的列表
	 *
	 * @param field
	 *            指定的字段
	 * @return 注解类型列表
	 */
	private List<Annotation> getAnnotations(Field field) {
		final List<Annotation> validateAnnotations = Lists.newArrayList();
		final Annotation[] annotations = field.getAnnotations();
		for (final Annotation annotation : annotations) {
			// 只提取handlers中配置了的注解
			if (handlers.containsKey(annotation.annotationType())) {
				validateAnnotations.add(annotation);
			}
		}
		return validateAnnotations;
	}

	/**
	 * @return {@link Validator} 对象实例
	 */
	public static Validator instance() {
		return INSTANCE;
	}
}

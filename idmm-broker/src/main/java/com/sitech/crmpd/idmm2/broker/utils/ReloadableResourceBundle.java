package com.sitech.crmpd.idmm2.broker.utils;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年8月24日 上午11:03:50
 */
public class ReloadableResourceBundle extends ReloadableResourceBundleMessageSource {

	private String basename;

	/**
	 * @param basename
	 */
	public ReloadableResourceBundle(String basename) {
		super();
		setBasename(basename);
		this.basename = basename;
	}

	/**
	 * Returns the number of key-value mappings in this map. If the map contains more than
	 * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of key-value mappings in this map
	 */
	public int size() {
		return getProperties(basename).getProperties().size();
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 *
	 * @return <tt>true</tt> if this map contains no key-value mappings
	 */
	public boolean isEmpty() {
		return getProperties(basename).getProperties().isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified key. More formally,
	 * returns <tt>true</tt> if and only if this map contains a mapping for a key <tt>k</tt> such
	 * that <tt>(key==null ? k==null : key.equals(k))</tt>. (There can be at most one such mapping.)
	 *
	 * @param key
	 *            key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key
	 * @throws ClassCastException
	 *             if the key is of an inappropriate type for this map (<a
	 *             href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException
	 *             if the specified key is null and this map does not permit null keys (<a
	 *             href="Collection.html#optional-restrictions">optional</a>)
	 */
	public boolean containsKey(Object key) {
		return getProperties(basename).getProperties().containsKey(key);
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null} if this map contains
	 * no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such
	 * that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise it returns {@code null}.
	 * (There can be at most one such mapping.)
	 *
	 * <p>
	 * If this map permits null values, then a return value of {@code null} does not
	 * <i>necessarily</i> indicate that the map contains no mapping for the key; it's also possible
	 * that the map explicitly maps the key to {@code null}. The {@link #containsKey containsKey}
	 * operation may be used to distinguish these two cases.
	 *
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null} if this map contains
	 *         no mapping for the key
	 * @throws ClassCastException
	 *             if the key is of an inappropriate type for this map (<a
	 *             href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException
	 *             if the specified key is null and this map does not permit null keys (<a
	 *             href="Collection.html#optional-restrictions">optional</a>)
	 */
	public String get(String key) {
		return getProperties(basename).getProperties().getProperty(key);
	}

}

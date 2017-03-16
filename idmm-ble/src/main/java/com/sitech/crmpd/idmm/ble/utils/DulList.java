package com.sitech.crmpd.idmm.ble.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 二级链表， 防止单个链表过长， 访问性能下降
 * @author guanyf
 *
 */
public class DulList <T>{
	ArrayList<List<T>> bulk = new ArrayList<List<T>>();
	static int MAX = 2000;
	int size = 0;

	public int size() {
		return size;
	}

	public T get(int i) {
		if (i >= size || i < 0)
			return null;
		for (List<T> l : bulk) {
			if (i < l.size()) {
				return l.get(i);
			} else {
				i -= l.size();
				if (i < 0)
					return null;
			}
		}
		return null;
	}

	public void add(T o) {
		List<T> l;
		if (bulk.size() == 0) {
			l = new ArrayList<T>();
			bulk.add(l);
		} else {
			l = bulk.get(bulk.size() - 1);
		}
		if (l.size() >= MAX) {
			l = new ArrayList<T>();
			bulk.add(l);
		}
		l.add(o);
		size++;
	}

	public T remove(int idx) {
		if (idx >= size || idx < 0)
			return null;
		for (int i = 0; i < bulk.size(); i++) {
			List<T> l = bulk.get(i);
			if (idx < l.size()) {
				T ret = l.remove(idx);
				if (l.size() == 0)
					bulk.remove(i);
				size--;
				return ret;
			} else {
				idx -= l.size();
			}
		}
		return null;
	}
}

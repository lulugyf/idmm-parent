package com.sitech.crmpd.idmm.ble.store;

final class OPAns {
	static enum Type{
		OK,
		FAIL,
		NONE,
	}
	volatile public Type tp;
	volatile public JournalOP op;
}

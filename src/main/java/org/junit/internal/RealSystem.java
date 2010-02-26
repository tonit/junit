package org.junit.internal;

import java.io.PrintStream;

/**
 * This is the default implementation of JUnitSystem, which maps
 * to {@link java.lang.System}.
 */
public class RealSystem implements JUnitSystem {
	public void exit(int code) {
		System.exit(code);
	}

	public PrintStream out() {
		return System.out;
	}
}

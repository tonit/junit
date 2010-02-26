package org.junit.internal;

import java.io.PrintStream;

/**
 * This interface helps JUnit abstract out calls that would normally be
 * made to {@link java.lang.System}.  IDE vendors and build tools can
 * implement this class with their own "System".  Essentially, it provides
 * an easy way to redirect System.out to any PrintStream and tell the
 * hosting system that JUnit is done.
 */
public interface JUnitSystem {
	/**
	 * Tell the host system that JUnit is done.  The execution threads for
	 * JUnit will be killed and the host system is free to reclaim the
	 * resources JUnit was using.
	 *
	 * TODO: we should make this simply a boolean and handle the 0/1 in RealSystem
	 * @param status  the exit status (0 = no errors)
	 */
	void exit(int status);

	/**
	 * The PrintStream that will be used to send the test information.
	 *
	 * @return  the PrintStream the system supplies
	 */
	PrintStream out();
}

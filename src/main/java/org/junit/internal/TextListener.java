package org.junit.internal;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * The TextListener outputs the results of the tests to the supplied
 * PrintStream.
 */
public class TextListener extends RunListener {

	private final PrintStream fWriter;

	/**
	 * Create an instance of TextListener to output results to
	 * {@link org.junit.internal.JUnitSystem#out()}.
	 *
	 * @param system  The JUnitSystem being used.
	 */
	public TextListener(JUnitSystem system) {
		this(system.out());
	}

	/**
	 * Create an instance of TextListener to output results to
	 * the provided PrintStream.
	 *
	 * @param writer
	 */
	public TextListener(PrintStream writer) {
		this.fWriter= writer;
	}

	/**
	 * Print the result summary to the PrintStream.  This includes
	 * the header, the failures, and the footer.
	 *
	 * @param result the summary of the test run, including all the tests that failed
	 */
	@Override
	public void testRunFinished(Result result) {
		printHeader(result.getRunTime());
		printFailures(result);
		printFooter(result);
	}

	/**
	 * Display "." to indicate the test has started.
	 *
	 * @param description the description of the test that is about to be run
	 */
	@Override
	public void testStarted(Description description) {
		getWriter().append('.');
	}

	/**
	 * Display "E" to indicate the test failed.
	 *
	 * @param failure describes the test that failed and the exception that was thrown
	 */
	@Override
	public void testFailure(Failure failure) {
		getWriter().append('E');
	}

	/**
	 * Display "I" to indicate the test was ignored.
	 *
	 * @param description describes the test that will not be run
	 */
	@Override
	public void testIgnored(Description description) {
		getWriter().append('I');
	}

	/*
	 * Internal methods
	 */

	private PrintStream getWriter() {
		return fWriter;
	}

	/**
	 * Ends the current line and prints the amount of time taken.
	 *
	 * @param runTime the amount of time taken
	 */
	protected void printHeader(long runTime) {
		getWriter().println();
		getWriter().println("Time: " + elapsedTimeAsString(runTime));
	}

	/**
	 * Print the failures found.  Provides a summary line:
	 * "There (was|were) X failure(s):".  The failures are then
	 * listed one by one.
	 *
	 * @param result  the results
	 */
	protected void printFailures(Result result) {
		List<Failure> failures= result.getFailures();
		if (failures.size() == 0)
			return;
		if (failures.size() == 1)
			getWriter().println("There was " + failures.size() + " failure:");
		else
			getWriter().println("There were " + failures.size() + " failures:");
		int i= 1;
		for (Failure each : failures)
			printFailure(each, Integer.toString(i++));
	}

	/**
	 * Print the failure with the provided prefix (the failure number).
	 * "X) {testHeader}"
	 * {stack trace}
	 *
	 * @param failure  the failure
	 * @param prefix   the prefix
	 */
	protected void printFailure(Failure failure, String prefix) {
		getWriter().println(prefix + ") " + failure.getTestHeader());
		getWriter().print(failure.getTrace());
	}

	/**
	 * Print the footer.  Either prints:
	 * "OK ({runCount} test(s))"
	 * or prints:
	 * "FAILURES!!!"
	 * "Tests run: {runCount}, Failures: {failureCount}"
	 *
	 * @param result  the test results
	 */
	protected void printFooter(Result result) {
		if (result.wasSuccessful()) {
			getWriter().println();
			getWriter().print("OK");
			getWriter().println(" (" + result.getRunCount() + " test" + (result.getRunCount() == 1 ? "" : "s") + ")");

		} else {
			getWriter().println();
			getWriter().println("FAILURES!!!");
			getWriter().println("Tests run: " + result.getRunCount() + ",  Failures: " + result.getFailureCount());
		}
		getWriter().println();
	}

	/**
	 * Returns the formatted string of the elapsed time. Duplicated from
	 * BaseTestRunner. Fix it.
	 */
	protected String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000);
	}
}

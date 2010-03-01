/**
 * Provides standard {@link org.junit.runner.Runner Runner} implementations.  These runners
 * enable different sets of functionality:
 *
 * <ul>
 * <li>{@link JUnit4} - standard JUnit 4 tests, this is the default runner so you don't
 *    have to specify it directly.</li>
 * <li>{@link Parameterized} - parameterized tests that provide a set of test parameters
 *    for the test to be run against.</li>
 * <li>{@link Suite} - allows you to collect a group of test cases together to run with
 *    one class.</li>
 * </ul>
 *
 * @since 4.0
 * @see org.junit.runner.Runner
 * @see org.junit.runners.BlockJUnit4ClassRunner
 */
package org.junit.runners;
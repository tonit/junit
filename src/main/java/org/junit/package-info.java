/**
 * JUnit 4 provides several unit testing features using annotations to
 * mark your test methods.  Many IDEs and build tools provide direct
 * support to JUnit test cases, but if you want to run your tests from
 * the command line directly follow these instructions:
 *
 * <pre>
 * java -cp "your/classpath/here" org.junit.runner.JUnitCore com.yourcom.YourSuite
 * </pre>
 *
 * The life cycle of the average JUnit test follows this pattern:
 * <ol>
 *   <li>{@link @BeforeClass} - called at the beginning of the test case</li>
 *   <li>{@link @Before} - called just before each test</li>
 *   <li>{@link @Test} - the actual test method</li>
 *   <li>{@link @After} - called just after each test</li>
 *   </li>{@link @AfterClass} - called at the end of the test case</li>
 * </ol>
 *
 * {@link @Rule} members all represent an {@link MethodRule}, which is applied to
 * every one of these life cycle methods.
 *
 * You can also organize your tests using inheritance.  If you have some common
 * setup code in an @BeforeClass, you can easily share that setup with every
 * sub class.  The base class &x40;BeforeClass method is called before your
 * current test class &x40;BeforeClass.  The process is reversed for the
 * &x40;AfterClass.
 *
 * @since 4.0
 */
package org.junit;
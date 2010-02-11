/**
 * 
 */
package org.junit.experimental.theories;

import org.junit.Assert;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Theories extends BlockJUnit4ClassRunner {
	public Theories(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void collectInitializationErrors(List<Throwable> errors) {
		super.collectInitializationErrors(errors);
		validateDataPointFields(errors);
	}
	
	private void validateDataPointFields(List<Throwable> errors) {
		Field[] fields= getTestClass().getJavaClass().getDeclaredFields();
		
		for (Field each : fields)
			if (each.getAnnotation(DataPoint.class) != null && !Modifier.isStatic(each.getModifiers()))
                //noinspection ThrowableInstanceNeverThrown
                errors.add(new Error("DataPoint field " + each.getName() + " must be static"));
	}
	
	@Override
	protected void validateConstructor(List<Throwable> errors) {
		validateOnlyOneConstructor(errors);
	}
	
	@Override
	protected void validateTestMethods(List<Throwable> errors) {
		for (FrameworkMethod each : computeTestMethods())
			if(each.getAnnotation(Theory.class) != null)
				each.validatePublicVoid(false, errors);
			else
				each.validatePublicVoidNoArg(false, errors);
	}
	
	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		List<FrameworkMethod> testMethods= super.computeTestMethods();
		List<FrameworkMethod> theoryMethods= getTestClass().getAnnotatedMethods(Theory.class);
		testMethods.removeAll(theoryMethods);
        List<FrameworkMethod> discreteTheoryMethods= new ArrayList<FrameworkMethod>(theoryMethods.size());
        Iterator<FrameworkMethod> it= theoryMethods.iterator();
        while(it.hasNext()) {
            FrameworkMethod method= it.next();
            if (runsDiscretely(method)) {
                try {
                    discreteTheoryMethods.addAll(TheoryMethod.createFromMethod(method.getMethod(),getTestClass()));
                    it.remove();
                } catch (Throwable problem) {
                    // the theory wasn't removed from the original list, so we will ignore it for now.
                    // TODO: proper handling here.  Is there an initalization exception?
                }
            }
        }

		testMethods.addAll(theoryMethods);
        testMethods.addAll(discreteTheoryMethods);
		return testMethods;
	}

    private boolean runsDiscretely(FrameworkMethod method) {
        Theory annotation= method.getAnnotation(Theory.class);
        return annotation != null && annotation.runDiscretely();
    }

	@Override
	public Statement methodBlock(final FrameworkMethod method) {
		return new TheoryAnchor(method, getTestClass());
	}

    private static class TheoryMethod extends FrameworkMethod {
        private final String fName;
        private final Assignments fAssignments;
        public TheoryMethod(Method method, Assignments assignments) throws Throwable {
            super(method);
            fAssignments = assignments;
            Theory annotation= getMethod().getAnnotation(Theory.class);
            boolean nullsOK=  annotation != null && annotation.nullsAccepted();
            fName= buildName(method,assignments.getMethodArguments(nullsOK));
        }

        private String buildName(Method method, Object[] assignments) {
            StringBuilder builder = new StringBuilder(method.getName());

            boolean first = true;
            builder.append("[");
            for(Object obj : assignments) {
                if (first) {
                    first= false;
                } else {
                    builder.append(",");
                }
                builder.append(obj);
            }
            builder.append("]");

            return builder.toString();
        }

        @Override
        public String getName() {
            return fName;
        }

        public Assignments getAssignments() {
            return fAssignments;
        }

        public static Collection<TheoryMethod> createFromMethod(Method testMethod, TestClass testClass) throws Throwable {
            Collection<TheoryMethod> discreteTests = new ArrayList<TheoryMethod>(17);

            buildFromAssignment(testMethod, Assignments.allUnassigned(testMethod, testClass), discreteTests);

            if (discreteTests.size() == 0)
                Assert.fail("Never found parameters that satisfied method signatures.");
            return discreteTests;
        }

        private static void buildFromAssignment(Method testMethod, Assignments assignments,
                                                Collection<TheoryMethod> discreteTests) throws Throwable {
            if(!assignments.isComplete()) {
                buildFromIncompleteAssignment(testMethod, assignments, discreteTests);
            } else {
                buildFromCompleteAssignment(testMethod, assignments, discreteTests);
            }
        }

        private static void buildFromIncompleteAssignment(Method testMethod,
                                                          Assignments incomplete,
                                                          Collection<TheoryMethod> discreteTests) throws Throwable {
            for (PotentialAssignment source : incomplete.potentialsForNextUnassigned()) {
                buildFromAssignment(testMethod, incomplete.assignNext(source), discreteTests) ;
            }
        }

        private static void buildFromCompleteAssignment(Method testMethod,
                                                        Assignments assignments,
                                                        Collection<TheoryMethod> discreteTests) throws Throwable {
            discreteTests.add(new TheoryMethod(testMethod, assignments));
        }
    }

	public static class TheoryAnchor extends Statement {
		private int successes= 0;

		private FrameworkMethod fTestMethod;
        private TestClass fTestClass;

		private List<AssumptionViolatedException> fInvalidParameters= new ArrayList<AssumptionViolatedException>();

		public TheoryAnchor(FrameworkMethod method, TestClass testClass) {
			fTestMethod= method;
            fTestClass= testClass;
		}

        private TestClass getTestClass() {
            return fTestClass;
        }

		@Override
		public void evaluate() throws Throwable {
            // If we already made the assignments, use them
            if ( fTestMethod instanceof TheoryMethod ) {
                runWithCompleteAssignment(((TheoryMethod)fTestMethod).getAssignments());
            } else {
                // otherwise compute and use them now
			    runWithAssignment(Assignments.allUnassigned(
				    	fTestMethod.getMethod(), getTestClass()));
            }

			if (successes == 0)
				Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: "
								+ fInvalidParameters);
		}

		protected void runWithAssignment(Assignments parameterAssignment)
				throws Throwable {
			if (!parameterAssignment.isComplete()) {
				runWithIncompleteAssignment(parameterAssignment);
			} else {
				runWithCompleteAssignment(parameterAssignment);
			}
		}

		protected void runWithIncompleteAssignment(Assignments incomplete)
				throws Throwable {
			for (PotentialAssignment source : incomplete
					.potentialsForNextUnassigned()) {
				runWithAssignment(incomplete.assignNext(source));
			}
		}

		protected void runWithCompleteAssignment(final Assignments complete)
				throws Throwable {
			new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
				@Override
				protected void collectInitializationErrors(
						List<Throwable> errors) {
					// do nothing
				}

				@Override
				public Statement methodBlock(FrameworkMethod method) {
					final Statement statement= super.methodBlock(method);
					return new Statement() {
						@Override
						public void evaluate() throws Throwable {
							try {
								statement.evaluate();
								handleDataPointSuccess();
							} catch (AssumptionViolatedException e) {
								handleAssumptionViolation(e);
							} catch (Throwable e) {
								reportParameterizedError(e, complete
										.getArgumentStrings(nullsOk()));
							}
						}

					};
				}

				@Override
				protected Statement methodInvoker(FrameworkMethod method, Object test) {
					return methodCompletesWithParameters(method, complete, test);
				}

				@Override
				public Object createTest() throws Exception {
					return getTestClass().getOnlyConstructor().newInstance(
							complete.getConstructorArguments(nullsOk()));
				}
			}.methodBlock(fTestMethod).evaluate();
		}

		private Statement methodCompletesWithParameters(
				final FrameworkMethod method, final Assignments complete, final Object freshInstance) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					try {
						final Object[] values= complete.getMethodArguments(
								nullsOk());
						method.invokeExplosively(freshInstance, values);
					} catch (CouldNotGenerateValueException e) {
						// ignore
					}
				}
			};
		}

		protected void handleAssumptionViolation(AssumptionViolatedException e) {
			fInvalidParameters.add(e);
		}

		protected void reportParameterizedError(Throwable e, Object... params)
				throws Throwable {
			if (params.length == 0)
				throw e;
			throw new ParameterizedAssertionError(e, fTestMethod.getName(),
					params);
		}

        private boolean nullsOk() {
            Theory annotation= fTestMethod.getMethod().getAnnotation(
                    Theory.class);
            return annotation != null && annotation.nullsAccepted();
        }

		protected void handleDataPointSuccess() {
			successes++;
		}
	}
}

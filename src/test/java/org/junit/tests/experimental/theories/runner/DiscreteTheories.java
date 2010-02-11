package org.junit.tests.experimental.theories.runner;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DiscreteTheories {
    private JUnitCore fCore;
    private DiscreteRunListener fListener;

    @Before
    public void setUpJUnitCore() {
        fCore = new JUnitCore();
        fListener = new DiscreteRunListener();
        fCore.addListener(fListener);
    }

    @Test
    public void verifyTestsRunDiscretely() {
        fCore.run(TestWithSingleParameter.class);

        assertThat(fListener.numberOfTests(), equalTo(2));
    }

    @Test
    public void verifyDiscreteTestsNamedProperly() {
        fCore.run(TestWithSingleParameter.class);

        for(Description description : fListener) {
            assertThat(description.getMethodName(), anyOf(equalTo("test[1]"),equalTo("test[2]")));
        }
    }

    @Test
    public void manyParamsAndDataPoints() {
        fCore.run(TestWithTwoParameters.class);

        assertThat(fListener.numberOfTests(), equalTo(36));

        for(Description description : fListener) {
            assertTrue(description.getMethodName().matches("multipleParameters\\[\\d,\\d\\]"));
        }
    }

    @RunWith(Theories.class)
    public static class TestWithSingleParameter {
        @DataPoint
        public static String one= "1";
        @DataPoint
        public static String two= "2";

        @Theory(runDiscretely=true)
        public void test(String value) {
            assertThat(value, anyOf(equalTo(one),equalTo(two)));
        }
    }

    @RunWith(Theories.class)
    public static class TestWithTwoParameters {
        @DataPoints
        public static int[] fibonacciSeries() {
            return new int[]{1,1,2,3,5,8};
        }

        @Theory(runDiscretely=true)
        public void multipleParameters(int x, int y) {
            assertTrue((x + y) > 0);
        }
    }

    private class DiscreteRunListener extends RunListener implements Iterable<Description> {
        List<Description> fTestsRun = new ArrayList<Description>(2);

        @Override
        public void testStarted(Description description) {
            fTestsRun.add(description);
        }

        public Iterator<Description> iterator() {
            return fTestsRun.iterator();
        }

        public int numberOfTests() {
            return fTestsRun.size();
        }
    }
}

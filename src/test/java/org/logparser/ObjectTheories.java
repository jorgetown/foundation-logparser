package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Theories to test adherence to Object's equals and hashcode contracts.
 * 
 * @see <a href="http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#equals(java.lang.Object)">Object#equals(java.lang.Object)</a>
 * @see <a href="http://download.oracle.com/javase/6/docs/api/java/lang/Object.html#hashCode()">Object#hashCode()</a>
 * @see <a href="https://docs.google.com/viewer?url=http://java.sun.com/developer/Books/effectivejava/Chapter3.pdf">Effective Java, chapter 3: Methods Common to All Objects</a>
 * @author jorge.decastro
 */
@RunWith(Theories.class)
public abstract class ObjectTheories {

	// For any non-null reference value x, x.equals(x) should return true.
	@Theory
	public void equalsIsReflexive(Object x) {
		assumeThat(x, is(not(equalTo(null))));
		assertThat(x.equals(x), is(true));
	}

	// For any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) returns true.
	@Theory
	public void equalsIsSymmetric(Object x, Object y) {
		assumeThat(x, is(not(equalTo(null))));
		assumeThat(y, is(not(equalTo(null))));
		assumeThat(y.equals(x), is(true));
		assertThat(x.equals(y), is(true));
	}

	// For any non-null reference values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, 
	// then x.equals(z) should return true.
	@Theory
	public void equalsIsTransitive(Object x, Object y, Object z) {
		assumeThat(x, is(not(equalTo(null))));
		assumeThat(y, is(not(equalTo(null))));
		assumeThat(z, is(not(equalTo(null))));
		assumeThat(x.equals(y) && y.equals(z), is(true));
		assertThat(x.equals(z), is(true));
	}

	// For any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true or consistently return false,
	// provided no information used in equals comparisons on the objects is modified.
	@Theory
	public void equalsIsConsistent(Object x, Object y) {
		assumeThat(x, is(not(equalTo(null))));
		boolean alwaysTheSame = x.equals(y);

		for (int i = 0; i < 30; i++) {
			assertThat(x.equals(y), is(alwaysTheSame));
		}
	}

	// For any non-null reference value x, x.equals(null) should return false.
	@Theory
	public void equalsReturnsFalseGivenNull(Object x) {
		assumeThat(x, is(not(equalTo(null))));
		assertThat(x.equals(null), is(false));
	}
	
	// Test that x.equals(y) where x and y refer to the same object.
	@Theory
	public void equalsIsConsistentWithReferentialEquality(Object x, Object y) {
		assumeThat(x, is(not(equalTo(null))));
		assumeThat(x == y, is(true));
		assertThat(x.equals(y), is(true));
	}

	// Whenever it is invoked on the same object more than once during an execution of an application, 
	// the hashCode method must consistently return the same integer, provided no information used in equals comparisons on the object is modified.
	@Theory
	public void hashCodeIsConsistent(Object x) {
		assumeThat(x, is(not(equalTo(null))));
		int alwaysTheSame = x.hashCode();

		for (int i = 0; i < 30; i++) {
			assertThat(x.hashCode(), is(alwaysTheSame));
		}
	}

	// If two objects are equal according to the equals(Object) method, 
	// then calling the hashCode method on each of the two objects must produce the same integer result.
	@Theory
	public void hashCodeIsConsistentWithEquals(Object x, Object y) {
		assumeThat(x, is(not(equalTo(null))));
		assumeThat(x.equals(y), is(true));
		assertThat(x.hashCode(), is(equalTo(y.hashCode())));
	}

	// It is not required that if two objects are unequal according to the equals(Object) method,
	// then calling the hashCode method on each of the two objects must produce distinct integer results.
	// However, the programmer should be aware that producing distinct integer results for unequal objects may improve the performance of hash tables.
	@Theory
	public void hashCodeIsStronglyConsistentWithEquals(Object x, Object y) {
		assumeThat(x, is(not(equalTo(null))));
		assumeThat(y, is(not(equalTo(null))));
		assumeThat(x.equals(y), is(false));
		assertThat(x.hashCode(), is(not(equalTo(y.hashCode()))));
	}
}
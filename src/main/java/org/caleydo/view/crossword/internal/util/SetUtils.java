/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.util;

import java.util.BitSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class SetUtils {

	public static Set<Integer> intersection(Set<Integer> a, Set<Integer> b) {
		if (a.isEmpty()) // return empty
			return a;
		if (b.isEmpty())
			return b;
		if (a instanceof BitSetSet) {
			return intersection((BitSetSet) a,b);
		}
		if (b instanceof BitSetSet)
			return intersection((BitSetSet) b,a);

		if (a.size() < b.size()) { // smaller at the beginning
			Set<Integer> tmp = a;
			a = b;
			b = tmp;
		}
		return ImmutableSet.copyOf(Sets.intersection(a, b));
	}

	public static int and(Set<Integer> a, Set<Integer> b) {
		if (a.isEmpty()) // return empty
			return 0;
		if (b.isEmpty())
			return 0;
		if (a instanceof BitSetSet) {
			return and((BitSetSet) a, b);
		}
		if (b instanceof BitSetSet)
			return and((BitSetSet) b, a);

		if (a.size() < b.size()) { // smaller at the beginning
			Set<Integer> tmp = a;
			a = b;
			b = tmp;
		}
		return Sets.intersection(a, b).size();
	}

	public static Set<Integer> union(Set<Integer> a, Set<Integer> b) {
		if (a.isEmpty()) // return empty
			return b;
		if (b.isEmpty())
			return a;
		if (a instanceof BitSetSet) {
			return union((BitSetSet) a, b);
		}
		if (b instanceof BitSetSet)
			return union((BitSetSet) b, a);

		if (a.size() < b.size()) { // smaller at the beginning
			Set<Integer> tmp = a;
			a = b;
			b = tmp;
		}
		return ImmutableSet.copyOf(Sets.union(a, b));
	}

	public static int or(Set<Integer> a, Set<Integer> b) {
		if (a.isEmpty()) // return empty
			return b.size();
		if (b.isEmpty())
			return a.size();
		if (a instanceof BitSetSet) {
			return or((BitSetSet) a, b);
		}
		if (b instanceof BitSetSet)
			return or((BitSetSet) b, a);

		if (a.size() < b.size()) { // smaller at the beginning
			Set<Integer> tmp = a;
			a = b;
			b = tmp;
		}
		return Sets.union(a, b).size();
	}

	public static int without(Set<Integer> a, Set<Integer> b) {
		if (a.isEmpty()) // return empty
			return 0;
		if (b.isEmpty())
			return a.size();
		if (a instanceof BitSetSet) {
			return without((BitSetSet) a, b);
		}
		return Sets.difference(a, b).size();
	}

	public static Set<Integer> intersection(BitSetSet a, Set<Integer> b) {
		if (b instanceof BitSetSet) {
			BitSet clone = (BitSet)a.getBitSet().clone();
			clone.and(((BitSetSet)b).getBitSet());
			return new BitSetSet(clone);
		}
		return ImmutableSet.copyOf(Sets.intersection(b, a)); // as the predicate is: in the second argument
	}

	public static Set<Integer> union(BitSetSet a, Set<Integer> b) {
		if (b instanceof BitSetSet) {
			BitSet clone = (BitSet) a.getBitSet().clone();
			clone.or(((BitSetSet) b).getBitSet());
			return new BitSetSet(clone);
		}
		return ImmutableSet.copyOf(Sets.union(b, a)); // as the predicate is: in the second argument
	}

	private static int and(BitSetSet a, Set<Integer> b) {
		if (b instanceof BitSetSet) {
			BitSet clone = (BitSet) a.getBitSet().clone();
			clone.and(((BitSetSet) b).getBitSet());
			return clone.cardinality();
		}
		return Sets.intersection(b, a).size(); // as the predicate is: in the second argument
	}

	private static int or(BitSetSet a, Set<Integer> b) {
		if (b instanceof BitSetSet) {
			BitSet clone = (BitSet) a.getBitSet().clone();
			clone.or(((BitSetSet) b).getBitSet());
			return clone.cardinality();
		}
		return Sets.union(b, a).size(); // as the predicate is: in the second argument
	}

	private static int without(BitSetSet a, Set<Integer> b) {
		if (b instanceof BitSetSet) {
			BitSet clone = (BitSet) a.getBitSet().clone();
			clone.andNot(((BitSetSet) b).getBitSet());
			return clone.cardinality();
		}
		return Sets.difference(a, b).size(); // as the predicate is: in the second argument
	}
}

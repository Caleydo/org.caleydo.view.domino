/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed.util;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * a set implementation based on a {@link BitSet}
 *
 * @author Samuel Gratzl
 *
 */
public class BitSetSet extends AbstractSet<Integer> {
	private final BitSet positives;
	private final BitSet negatives;

	public BitSetSet() {
		this(new BitSet(), new BitSet());
	}

	public BitSetSet(Set<Integer> ids) {
		this();
		addAll(ids);
	}

	/**
	 * @param clone
	 */
	public BitSetSet(BitSet positives, BitSet negatives) {
		this.positives = positives;
		this.negatives = negatives;
	}

	/**
	 * @param per
	 * @return
	 */
	public static BitSetSet of(Iterable<Integer> it) {
		BitSetSet r = new BitSetSet();
		for (Integer id : it)
			r.add(id);
		return r;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer))
			return false;
		final int i = ((Integer) o).intValue();
		if (i < 0)
			return negatives.get(-i);
		return positives.get(i);
	}

	@Override
	public void clear() {
		positives.clear();
		negatives.clear();
	}

	@Override
	public boolean isEmpty() {
		return positives.isEmpty() && negatives.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		if (!contains(o))
			return false;
		final int i = ((Integer) o).intValue();
		if (i < 0)
			negatives.clear(-i);
		else
			positives.clear(i);
		return true;
	}

	@Override
	public boolean add(Integer e) {
		Preconditions.checkNotNull(e);
		if (contains(e))
			return false;
		final int i = e.intValue();
		if (i < 0)
			negatives.set(-i);
		else
			positives.set(i);
		return true;
	}

	@Override
	public Iterator<Integer> iterator() {
		if (positives.isEmpty() && negatives.isEmpty())
			return Iterators.emptyIterator();
		if (positives.isEmpty())
			return new BitSetIterator(negatives, false);
		if (negatives.isEmpty())
			return new BitSetIterator(positives, true);
		return Iterators.concat(new BitSetIterator(negatives, false), new BitSetIterator(positives, true));
	}

	@Override
	public int size() {
		return positives.cardinality() + negatives.cardinality();
	}

	private static class BitSetIterator implements Iterator<Integer> {
		private final BitSet bitSet;
		private final boolean forward;

		private int i;

		public BitSetIterator(BitSet bitSet, boolean forward) {
			this.bitSet = bitSet;
			this.forward = forward;
			this.i = forward ? bitSet.nextSetBit(0) : bitSet.previousSetBit(bitSet.size());
		}

		@Override
		public void remove() {
			bitSet.clear(forward ? bitSet.previousSetBit(i - 1) : bitSet.nextSetBit(i + 1));
		}

		@Override
		public Integer next() {
			int bak = i;
			i = forward ? bitSet.nextSetBit(i + 1) : bitSet.previousSetBit(i - 1);
			return forward ? bak : -bak;
		}

		@Override
		public boolean hasNext() {
			return i != -1;
		}
	}

	public static BitSetSet and(BitSetSet a, BitSetSet b) {
		BitSet p = (BitSet) a.positives.clone();
		p.and(b.positives);
		BitSet n = (BitSet) a.negatives.clone();
		n.and(b.negatives);
		return new BitSetSet(p, n);
	}

	public static BitSetSet or(BitSetSet a, BitSetSet b) {
		BitSet p = (BitSet) a.positives.clone();
		p.or(b.positives);
		BitSet n = (BitSet) a.negatives.clone();
		n.or(b.negatives);
		return new BitSetSet(p, n);
	}

	public static BitSetSet andNot(BitSetSet a, BitSetSet b) {
		BitSet p = (BitSet) a.positives.clone();
		p.andNot(b.positives);
		BitSet n = (BitSet) a.negatives.clone();
		n.andNot(b.negatives);
		return new BitSetSet(p, n);
	}

}

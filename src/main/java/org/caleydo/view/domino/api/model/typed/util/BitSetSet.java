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

/**
 * a set implementation based on a {@link BitSet}
 *
 * @author Samuel Gratzl
 *
 */
public class BitSetSet extends AbstractSet<Integer> {
	private final BitSet bitSet;

	public BitSetSet() {
		this(new BitSet());
	}

	public BitSetSet(Set<Integer> ids) {
		this();
		addAll(ids);
	}

	/**
	 * @param clone
	 */
	public BitSetSet(BitSet bitSet) {
		this.bitSet = bitSet;
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
			return false;
		return bitSet.get(i);
	}

	@Override
	public void clear() {
		bitSet.clear();
	}

	@Override
	public boolean isEmpty() {
		return bitSet.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		if (!contains(o))
			return false;
		bitSet.clear(((Integer) o).intValue());
		return true;
	}

	@Override
	public boolean add(Integer e) {
		Preconditions.checkNotNull(e);
		if (contains(e))
			return false;
		bitSet.set(e.intValue());
		return true;
	}

	/**
	 * @return the bitSet, see {@link #bitSet}
	 */
	public BitSet getBitSet() {
		return bitSet;
	}


	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int i = bitSet.nextSetBit(0);

			@Override
			public void remove() {
				bitSet.clear(bitSet.previousSetBit(i - 1));
			}

			@Override
			public Integer next() {
				int bak = i;
				i = bitSet.nextSetBit(i + 1);
				return bak;
			}

			@Override
			public boolean hasNext() {
				return i != -1;
			}
		};
	}

	@Override
	public int size() {
		return bitSet.cardinality();
	}
}

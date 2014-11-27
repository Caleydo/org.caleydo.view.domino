/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * an immutable list of items in a read only fashion
 *
 * @author Samuel Gratzl
 *
 */
public final class ConcatedList<T> extends AbstractList<T> {
	private final int[] ends;
	private final List<? extends List<T>> groups;

	public ConcatedList(List<? extends List<T>> groups) {
		ends = new int[groups.size()];
		this.groups = groups;
		int c = 0;
		for (int i = 0; i < ends.length; ++i) {
			final List<T> group = groups.get(i);
			c += group.size();
			ends[i] = c;
		}
	}

	@SafeVarargs
	public ConcatedList(List<T>... groups) {
		this(Arrays.asList(groups));
	}

	@Override
	public Iterator<T> iterator() {
		return Iterables.concat(groups).iterator();
	}

	@Override
	public T get(int index) {
		for (int i = 0; i < ends.length; ++i) {
			if (index < ends[i]) {
				final List<T> l = groups.get(i);
				return l.get(index - ends[i] + l.size());
			}
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int size() {
		return ends.length == 0 ? 0 : ends[ends.length - 1];
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

/**
 * @author Samuel Gratzl
 *
 */
public class IndexedSort {
	public static <T extends Comparable<T>> int[] sortIndex(List<T> list) {
		return sortIndex(list, Ordering.natural());
	}

	/**
	 * sort the given list and return the sorted list of indices
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public static <T> int[] sortIndex(final List<T> list, final Comparator<? super T> comparator) {
		final int size = list.size();
		int[] indices = new int[size];
		for (int i = 0; i < size; ++i)
			indices[i] = i;
		if (size <= 1)
			return indices;

		Collections.sort(Ints.asList(indices), new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return comparator.compare(list.get(o1.intValue()), list.get(o2.intValue()));
			}
		});

		return indices;
	}
}


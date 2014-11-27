/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import static java.lang.Double.isNaN;

import java.util.Arrays;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.function.ArrayDoubleList;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.IDoubleSizedIterator;
import org.caleydo.view.domino.internal.util.IndexedSort;

/**
 * @author Samuel Gratzl
 *
 */
public enum ECorrelation implements ILabeled {
	PEARSON, SPEARMAN;

	@Override
	public String getLabel() {
		return WordUtils.capitalize(name().toLowerCase());
	}

	public double apply(IDoubleList a, IDoubleList b) {
		switch(this) {
		case PEARSON:
			return pearson(a, b);
		case SPEARMAN:
			return spearman(a, b);
		}
		throw new IllegalStateException();
	}

	private double pearson(final IDoubleList a, final IDoubleList b) {
		final double a_mean = mean(a.iterator());
		final double b_mean = mean(b.iterator());

		final IDoubleSizedIterator a_it = a.iterator();
		final IDoubleSizedIterator b_it = b.iterator();

		int n = 0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;
		double sum_coproduct = 0;

		while (a_it.hasNext() && b_it.hasNext()) {
			double a_d = a_it.nextPrimitive() - a_mean;
			double b_d = b_it.nextPrimitive() - b_mean;
			if (!isNaN(a_d) && !isNaN(b_d)) {
				sum_sq_x += a_d * a_d;
				sum_sq_y += b_d * b_d;
				sum_coproduct += a_d * b_d;
				n++;
			}
		}

		if (n == 0)
			return 0;

		final double pop_sd_x = Math.sqrt(sum_sq_x);
		final double pop_sd_y = Math.sqrt(sum_sq_y);
		final double cov_x_y = sum_coproduct;
		final double correlation = cov_x_y / (pop_sd_x * pop_sd_y);

		return correlation; // convert to similarity measure
	}

	private double spearman(final IDoubleList a, final IDoubleList b) {
		double[] a_rank = toRank(a);
		double[] b_rank = toRank(b);
		return pearson(new ArrayDoubleList(a_rank), new ArrayDoubleList(b_rank));
	}

	/**
	 * @param a
	 * @return
	 */
	private static double[] toRank(IDoubleList a) {
		final int size = a.size();
		double[] r = new double[size];
		int[] indices = IndexedSort.sortIndex(a);
		// convert entries to their rank position starting with 1 and in case of ties use mean
		for(int i = 0; i < size; ++i) {
			int index = indices[i];
			double v = a.getPrimitive(index);
			int ilast = i + 1;
			if (ilast < size) {
				int in = indices[ilast];
				double vlast = a.getPrimitive(in);
				while (vlast == v && ilast < size - 1) {
					ilast++;
					in = indices[ilast];
					vlast = a.getPrimitive(in);
				}
			}
			double rank = (ilast == (i + 1)) ? i + 1 : (ilast + i + 1) / (double) (ilast - i);
			for (int j = i; j < ilast; ++j)
				r[indices[j]] = rank;
			i = ilast - 1;
		}

		return r;
	}

	public static void main(String[] args) {
		// System.err.println(Arrays.toString(toRank(new ArrayDoubleList(new double[] { 1, 2, 3, 4 }))));
		System.err.println(Arrays.toString(toRank(new ArrayDoubleList(new double[] { 1, 3, 3, 4 }))));
	}

	private static double mean(IDoubleSizedIterator it) {
		int n = 0;
		double acc = 0;
		while (it.hasNext()) {
			double d = it.nextPrimitive();
			if (!isNaN(d)) {
				n++;
				acc += d;
			}
		}
		return n == 0 ? 0 : acc / n;
	}
}

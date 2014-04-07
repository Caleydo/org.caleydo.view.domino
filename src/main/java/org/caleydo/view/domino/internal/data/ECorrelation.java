/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import static java.lang.Double.isNaN;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.IDoubleSizedIterable;
import org.caleydo.core.util.function.IDoubleSizedIterator;

/**
 * @author Samuel Gratzl
 *
 */
public enum ECorrelation implements ILabeled {
	PEARSON;

	@Override
	public String getLabel() {
		return WordUtils.capitalize(name().toLowerCase());
	}

	public double apply(IDoubleList a, IDoubleList b) {
		switch(this) {
		case PEARSON:
			return pearson(a, b);
		}
		throw new IllegalStateException();
	}

	private double pearson(final IDoubleSizedIterable a, final IDoubleSizedIterable b) {
		final double a_mean = mean(a.iterator());
		final double b_mean = mean(b.iterator());

		final IDoubleSizedIterator a_it = a.iterator();
		final IDoubleSizedIterator b_it = a.iterator();

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

	private double spearman(final IDoubleSizedIterable a, final IDoubleSizedIterable b) {
		// FIXME
		return Double.NaN;
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

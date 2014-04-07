/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.function.DoubleStatistics;

/**
 * @author Samuel Gratzl
 *
 */
public enum EProjection implements ILabeled {
	MEAN, MIN, MAX, SUM, VARIANCE, SD;

	@Override
	public String getLabel() {
		return WordUtils.capitalize(name().toLowerCase());
	}

	public double select(DoubleStatistics stats) {
		switch (this) {
		case MAX:
			return stats.getMax();
		case MIN:
			return stats.getMin();
		case MEAN:
			return stats.getMean();
		case SUM:
			return stats.getSum();
		case VARIANCE:
			return stats.getVar();
		case SD:
			return stats.getSd();
		}
		throw new IllegalStateException();
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import org.caleydo.core.util.color.Color;
import org.caleydo.view.rnb.internal.prefs.MyPreferences;

/**
 * @author Samuel Gratzl
 *
 */
public class Constants {
	public static final float LABEL_SIZE = 14;

	public static final float SCATTER_POINT_SIZE = 4;
	public static final float PARALLEL_LINE_SIZE = 2;

	public static final float TARGET_MAX_VIEW_SIZE = 0.5f;
	public static final float TARGET_MIN_VIEW_SIZE = 0.25f;

	/**
	 * @param vs
	 * @return
	 */
	public static Color colorMapping(float a) {
		if (Float.isNaN(a))
			return Color.NOT_A_NUMBER_COLOR;
		Color max = MyPreferences.getNumericalMappingMaxColor();
		Color min = MyPreferences.getNumericalMappingMinColor();
		// v0*(1-t)+v1*t
		float ma = 1 - a;
		return new Color(min.r * ma + max.r * a, min.g * ma + max.g * a, min.b * ma + max.b * a, min.a * ma + max.a * a);
	}
}

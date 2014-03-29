/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import org.caleydo.core.util.function.Function2;

/**
 * @author Samuel Gratzl
 *
 */
public class Functions2s {

	/**
	 * @param aDataDomainDataValues
	 * @return
	 */
	public static <F1, F2, T> Function2<F2, F1, T> swap(final Function2<F1, F2, T> in) {
		return new Function2<F2, F1, T>() {
			@Override
			public T apply(F2 input1, F1 input2) {
				return in.apply(input2, input1);
			}
		};
	}

}

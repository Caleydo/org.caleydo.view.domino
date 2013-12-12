/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * @author Samuel Gratzl
 *
 */
public class Edges {

	public static final Predicate<Object> SAME_SORTING = Predicates.not(Predicates.instanceOf(ISortBarrier.class));
	public static final Predicate<Object> SAME_STRATIFICATION = Predicates.not(Predicates
			.instanceOf(IStratificationBarrier.class));

	public static final Predicate<Object> BANDS = Predicates.instanceOf(BandEdge.class);

}

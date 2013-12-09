/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;

/**
 * @author Samuel Gratzl
 *
 */
public interface ISortableNode extends INode {
	String SORTING_PRIORITY = "sortingPriority";

	int NO_SORTING = Integer.MAX_VALUE;

	int TOP_PRIORITY = 0;

	int MINIMUM_PRIORITY = 2;

	boolean isSortable(EDimension dim);

	int getSortingPriority(EDimension dim);

	void setSortingPriority(EDimension dim, int sortingPriority);


	ITypedComparator getComparator(EDimension dim);
}

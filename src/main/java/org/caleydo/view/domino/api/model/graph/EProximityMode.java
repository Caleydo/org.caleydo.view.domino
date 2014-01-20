/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2.EVisScaleType;
import org.caleydo.view.domino.spi.model.graph.IEdge;

import com.google.common.base.Predicate;

/**
 * @author Samuel Gratzl
 *
 */
public enum EProximityMode implements Predicate<EVisScaleType> {
	ATTACHED, DETACHED, FREE;


	@Override
	public boolean apply(EVisScaleType input) {
		if (this == ATTACHED && input != EVisScaleType.DATA_DEPENDENT)
			return false;
		return true;
	}

	public static EProximityMode min(EProximityMode a, EProximityMode b) {
		return a.ordinal() < b.ordinal() ? a : b;
	}

	public static EProximityMode min(Iterable<IEdge> edges) {
		EProximityMode m = EProximityMode.FREE;
		for (IEdge edge : edges) {
			m = min(m, edge.asMode());
		}
		return m;
	}
}

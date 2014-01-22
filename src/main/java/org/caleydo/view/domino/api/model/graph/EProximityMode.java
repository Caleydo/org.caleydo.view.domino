/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.view.opengl.layout2.manage.EVisScaleType;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementMetaData;

import com.google.common.base.Predicate;

/**
 * @author Samuel Gratzl
 *
 */
public enum EProximityMode implements Predicate<String> {
	ATTACHED, DETACHED, FREE;


	/**
	 * predicate for a visualization type selector
	 */
	@Override
	public boolean apply(String input) {
		if (this == ATTACHED && getScaleType(input) != EVisScaleType.DATADEPENDENT)
			return false;
		return true;
	}

	/**
	 * @param input
	 * @return
	 */
	private EVisScaleType getScaleType(String input) {
		IGLElementMetaData m = GLElementFactories.getMetaData(input);
		return m == null ? EVisScaleType.FIX : m.getScaleType();
	}

	public static EProximityMode min(EProximityMode a, EProximityMode b) {
		return a.ordinal() < b.ordinal() ? a : b;
	}
}

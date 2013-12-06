/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import java.util.Set;

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroup extends TypedSet implements ILabeled {
	private final Color color;
	private final String label;

	public TypedGroup(Set<Integer> wrappee, IDType idType, Color color, String label) {
		super(wrappee, idType);
		this.color = color;
		this.label = label;
	}

	/**
	 * @return the label, see {@link #label}
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @return the color, see {@link #color}
	 */
	public Color getColor() {
		return color;
	}

}

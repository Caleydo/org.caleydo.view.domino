/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import org.caleydo.core.util.color.Color;

import com.google.common.base.Predicate;

/**
 * @author Samuel Gratzl
 *
 */
public enum EEdgeType implements Predicate<IGraphEdge> {
	SHARED, PARENT_CHILD, SIBLING;

	@Override
	public boolean apply(IGraphEdge input) {
		return input != null && input.getType() == this;
	}

	public Color getColor() {
		switch(this) {
		case SHARED:
			return new Color(0.9f, 0.9f, 0.9f, 0.5f);
		case PARENT_CHILD:
			return new Color(0.9f, 0f, 0f, 0.5f);
		case SIBLING:
			return new Color(0.9f, 0.9f, 0.9f, 0.5f);
		}
		throw new IllegalStateException();
	}
}
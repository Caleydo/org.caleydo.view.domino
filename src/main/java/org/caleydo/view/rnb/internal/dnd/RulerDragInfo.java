/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.view.rnb.internal.ui.Ruler;

/**
 * @author Samuel Gratzl
 *
 */
public class RulerDragInfo extends ADragInfo {
	private final Ruler ruler;

	public RulerDragInfo(Vec2f mousePos, Ruler ruler) {
		super(mousePos);
		this.ruler = ruler;
	}

	/**
	 * @return the ruler, see {@link #ruler}
	 */
	public Ruler getRuler() {
		return ruler;
	}

	@Override
	public String getLabel() {
		return "Ruler " + ruler.getIDCategory().getCategoryName();
	}

	@Override
	protected Vec2f getSize() {
		return ruler.getSize();
	}

}

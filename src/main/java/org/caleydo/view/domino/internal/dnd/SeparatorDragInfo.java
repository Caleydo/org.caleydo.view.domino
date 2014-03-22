/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.view.domino.internal.ui.Separator;

/**
 * @author Samuel Gratzl
 *
 */
public class SeparatorDragInfo extends ADragInfo {
	private final Separator separator;

	public SeparatorDragInfo(Vec2f mousePos, Separator separator) {
		super(mousePos);
		this.separator = separator;
	}

	/**
	 * @return the separator, see {@link #separator}
	 */
	public Separator getSeparator() {
		return separator;
	}

	@Override
	public String getLabel() {
		return "Separator";
	}

	@Override
	protected Vec2f getSize() {
		return separator.getSize();
	}
}

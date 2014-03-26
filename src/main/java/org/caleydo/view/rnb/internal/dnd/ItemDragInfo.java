/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.view.rnb.internal.ui.AItem;

/**
 * @author Samuel Gratzl
 *
 */
public class ItemDragInfo extends ADragInfo {
	private final AItem item;

	public ItemDragInfo(Vec2f mousePos, AItem separator) {
		super(mousePos);
		this.item = separator;
	}

	/**
	 * @return the separator, see {@link #item}
	 */
	public AItem getItem() {
		return item;
	}

	@Override
	public String getLabel() {
		return item.getClass().getSimpleName();
	}

	@Override
	protected Vec2f getSize() {
		return item.getSize();
	}
}

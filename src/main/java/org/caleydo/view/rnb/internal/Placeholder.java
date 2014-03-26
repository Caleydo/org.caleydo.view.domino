/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.rnb.api.model.EDirection;

/**
 * @author Samuel Gratzl
 *
 */
public class Placeholder extends APlaceholder {
	private final Node neighbor;
	private final EDirection dir;
	private final boolean transpose;

	public Placeholder(Node neighbor, EDirection dir, boolean transpose) {
		this.neighbor = neighbor;
		this.dir = dir;
		this.transpose = transpose;
		Vec2f size = neighbor.getSize();
		Vec2f loc = neighbor.getAbsoluteLocation();
		final float c = Block.DETACHED_OFFSET;
		final float offset = 0;
		switch (dir) {
		case NORTH:
			setBounds(loc.x(), loc.y() - c - offset, size.x(), c);
			break;
		case SOUTH:
			setBounds(loc.x(), loc.y() + size.y() + offset, size.x(), c);
			break;
		case WEST:
			setBounds(loc.x() - c - offset, loc.y(), c, size.y());
			break;
		case EAST:
			setBounds(loc.x() + size.x() + offset, loc.y(), c, size.y());
			break;
		}
	}

	@Override
	protected EDimension getDimension() {
		return dir.asDim();
	}

	@Override
	protected Color getColor() {
		return Color.LIGHT_GRAY;
	}

	@Override
	protected void dropNode(Node node) {
		RnB rnb = findParent(RnB.class);
		if (transpose) {
			node.transposeMe();
		}
		rnb.addPreview(neighbor, dir, node);
	}
}

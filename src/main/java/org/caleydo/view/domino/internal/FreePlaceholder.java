/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.EDirection;

/**
 * @author Samuel Gratzl
 *
 */
public class FreePlaceholder extends APlaceholder {
	private static final float FREE_OFFSET = 200;
	private final EDirection dir;
	private final boolean transpose;

	public FreePlaceholder(EDirection dir, Rect bounds) {
		this.dir = dir;
		this.transpose = false;
		setBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height());
	}

	/**
	 * @param transposed
	 * @param dir2
	 * @param n
	 */
	public FreePlaceholder(EDirection dir, Node neighbor, boolean transpose) {
		this.dir = dir;
		this.transpose = transpose;
		Vec2f size = neighbor.getSize();
		Vec2f loc = neighbor.getAbsoluteLocation();
		final float c = Block.DETACHED_OFFSET;
		final float offset = FREE_OFFSET;
		switch (dir) {
		case SOUTH:
			setBounds(loc.x(), loc.y() + size.y() + offset, size.x(), c);
			break;
		case EAST:
			setBounds(loc.x() + size.x() + offset, loc.y(), c, size.y());
			break;
		default:
			break;
		}
	}

	@Override
	protected Color getColor() {
		return Color.LIGHT_BLUE;
	}

	@Override
	protected EDimension getDimension() {
		return dir.asDim();
	}

	@Override
	protected void updatedPreview(Node preview) {
		super.updatedPreview(preview);
		final Block block = preview.getBlock();
		Vec2f size = block.getSize();
		Vec2f loc = getLocation();
		Vec2f shift = getSize().minus(size).times(0.5f);
		switch (dir) {
		case EAST:
			loc.setX(loc.x() + Block.DETACHED_OFFSET);
			loc.setY(loc.y() + shift.y());
			break;
		case SOUTH:
			loc.setX(loc.x() + shift.x());
			loc.setY(loc.y() + Block.DETACHED_OFFSET);
			break;
		default:
			break;
		}
		block.setLocation(loc.x(), loc.y());
	}

	@Override
	protected void dropNode(Node node) {
		Domino domino = findParent(Domino.class);
		if (transpose) {
			node.transposeMe();
		}
		Block b = new Block(node);
		Vec2f loc = getLocation();
		switch (dir) {
		case EAST:
			loc.setX(loc.x() + Block.DETACHED_OFFSET);
			break;
		case SOUTH:
			loc.setY(loc.y() + Block.DETACHED_OFFSET);
			break;
		default:
			break;
		}
		b.setLocation(loc.x(), loc.y());
		domino.addBlock(b);
		updatedPreview(node);
	}
}

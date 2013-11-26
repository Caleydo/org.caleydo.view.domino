/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.crossword.spi.ui.layout.IGraphLayout;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AGraphLayout implements IGraphLayout {
	/**
	 * @param edge
	 * @return
	 */
	protected final float addStart(List<Vec2f> curve, IGraphEdge edge, float shift) {
		Rect self = edge.getSource().getBounds();
		Rect opposite = edge.getTarget().getBounds();

		IVertexConnector connector = edge.getSourceConnector();
		final float radius = toPosition(curve, self, opposite, connector);

		if (shift > 0)
			curve.add(shiftedPos(shift, self, opposite, connector, curve.get(curve.size() - 1)));
		return radius;

	}

	protected final float addEnd(List<Vec2f> curve, IGraphEdge edge, float shift) {
		Rect self = edge.getTarget().getBounds();
		Rect opposite = edge.getSource().getBounds();

		IVertexConnector connector = edge.getTargetConnector();
		final float radius = toPosition(curve, self, opposite, connector);

		if (shift > 0)
			curve.add(curve.size() - 1, shiftedPos(shift, self, opposite, connector, curve.get(curve.size() - 1)));
		return radius;

	}

	private Vec2f shiftedPos(float shift, Rect self, Rect opposite, IVertexConnector connector, Vec2f pos) {
		Vec2f shifted = pos.copy();
		switch (connector.getDimension()) {
		case RECORD:
			if (self.x2() < opposite.x())
				shifted.setX(pos.x() + shift);
			else
				shifted.setX(pos.x() - shift);
			break;
		case DIMENSION:
			if (self.y2() < opposite.y())
				shifted.setY(pos.y() + shift);
			else
				shifted.setY(pos.y() - shift);
			break;
		}
		return shifted;
	}

	private float toPosition(List<Vec2f> curve, Rect self, Rect opposite, IVertexConnector connector) {
		float center = connector.getCenter();
		Vec2f posSize = connector.getDimension().select(new Vec2f(self.x(), self.width()),
				new Vec2f(self.y(), self.height()));
		final float radius = connector.getRadius() * posSize.y();

		Vec2f pos = self.xy();
		switch (connector.getDimension()) {
		case RECORD:
			if (self.x2() < opposite.x())
				pos.setX(self.x2());
			pos.setY(pos.y() + center * posSize.y());
			break;
		case DIMENSION:
			if (self.y2() < opposite.y())
				pos.setY(self.y2());
			pos.setX(pos.x() + center * posSize.y());
			break;
		}

		curve.add(pos);
		return radius;
	}
}

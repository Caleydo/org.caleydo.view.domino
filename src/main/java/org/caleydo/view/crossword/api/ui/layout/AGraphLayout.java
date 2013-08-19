/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import gleem.linalg.Vec2f;

import java.util.Arrays;
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
	protected final List<Vec2f> toPath(IGraphEdge edge) {
		Rect sRect = edge.getSource().getBounds();
		Rect tRect = edge.getTarget().getBounds();

		Vec2f source = toPosition(edge.getSourceConnector(), sRect, tRect);
		Vec2f target = toPosition(edge.getTargetConnector(), tRect, sRect);
		return Arrays.asList(source, target);
	}


	/**
	 * @param sourceConnector
	 * @param sRect
	 * @param tRect
	 * @return
	 */
	private Vec2f toPosition(IVertexConnector connector, Rect self, Rect opposite) {
		float center = connector.getCenter();
		Vec2f posSize = connector.getConnectorType().extract(self);

		Vec2f pos = self.xy();
		switch (connector.getConnectorType()) {
		case RECORD:
			if (self.x2() < opposite.x())
				pos.setX(self.x2());
			pos.setY(pos.y() + center * posSize.y());
			break;
		case COLUMN:
			if (self.y2() < opposite.y())
				pos.setY(self.y2());
			pos.setX(pos.x() + center * posSize.y());
			break;
		}
		return pos;
	}
}

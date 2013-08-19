/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.crossword.api.model.BandRoute;
import org.caleydo.view.crossword.api.ui.layout.IGraphEdge;
import org.caleydo.view.crossword.api.ui.layout.IGraphVertex;
import org.caleydo.view.crossword.api.ui.layout.IVertexConnector;
import org.caleydo.view.crossword.spi.ui.layout.IGraphLayout;

/**
 * @author Samuel Gratzl
 *
 */
public class DefaultGraphLayout implements IGraphLayout {

	@Override
	public GraphLayoutModel doLayout(Set<? extends IGraphVertex> vertices, Set<? extends IGraphEdge> edges) {
		float acc = 10;
		float x_shift = 0;
		float y_shift = 0;

		for (IGraphVertex vertex : vertices) {
			Vec2f loc = vertex.getLocation();
			loc.setX(Float.isNaN(loc.x()) ? acc : loc.x());
			loc.setY(Float.isNaN(loc.y()) ? acc : loc.y());
			Vec2f msize = vertex.getSize();
			vertex.setBounds(loc, msize);
			// new loc
			loc = vertex.getLocation();
			x_shift = Math.min(loc.x(), x_shift);
			y_shift = Math.min(loc.y(), y_shift);

			// FIXME
			acc += msize.x() + 10;
		}

		if (x_shift < 0 || y_shift < 0) {
			// shift all
			for (IGraphVertex vertex : vertices) {
				vertex.move(-x_shift, -y_shift);
			}
		}

		Collection<BandRoute> routes = new ArrayList<>();

		for (IGraphEdge edge : edges) {
			routes.add(new BandRoute(toPath(edge), edge.getType().getColor(), edge.getIntersection()));
		}
		return new GraphLayoutModel(false, routes);
	}

	/**
	 * @param edge
	 * @return
	 */
	private List<Vec2f> toPath(IGraphEdge edge) {
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

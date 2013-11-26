/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.ui.layout;

import java.util.List;
import java.util.Set;

import org.caleydo.view.domino.api.ui.layout.IGraphEdge;
import org.caleydo.view.domino.api.ui.layout.IGraphVertex;
import org.caleydo.view.domino.spi.model.IBandRenderer;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphLayout {
	/**
	 * performs the layouting
	 *
	 * @param vertices
	 * @return the model instance
	 */
	GraphLayoutModel doLayout(Set<? extends IGraphVertex> vertices, Set<? extends IGraphEdge> edges);

	public final static class GraphLayoutModel {
		private final boolean requireAnotherRound;
		private final List<? extends IBandRenderer> routes;

		public GraphLayoutModel(boolean requireAnotherRound, List<? extends IBandRenderer> routes) {
			this.requireAnotherRound = requireAnotherRound;
			this.routes = routes;
		}

		/**
		 * @return the routes, see {@link #routes}
		 */
		public List<? extends IBandRenderer> getRoutes() {
			return routes;
		}

		/**
		 * @return the requireAnotherRound, see {@link #requireAnotherRound}
		 */
		public boolean isRequireAnotherRound() {
			return requireAnotherRound;
		}
	}
}

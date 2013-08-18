/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import java.util.Collection;
import java.util.Set;

import org.caleydo.view.crossword.internal.model.BandRoute;

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
		private final Collection<BandRoute> routes;

		public GraphLayoutModel(boolean requireAnotherRound, Collection<BandRoute> routes) {
			this.requireAnotherRound = requireAnotherRound;
			this.routes = routes;
		}

		/**
		 * @return the routes, see {@link #routes}
		 */
		public Collection<BandRoute> getRoutes() {
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

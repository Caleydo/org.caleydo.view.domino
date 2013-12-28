/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Pseudograph;

/**
 * @author Samuel Gratzl
 *
 */
public interface IListenableUndirectedGraph<V, E> extends ListenableGraph<V, E>, UndirectedGraph<V, E> {
	public static class ListenableUndirectedMultigraph<V, E> extends DefaultListenableGraph<V, E> implements
			IListenableUndirectedGraph<V, E> {
		private static final long serialVersionUID = 1L;

		public ListenableUndirectedMultigraph(Class<E> edgeClass) {
			super(new Pseudograph<V, E>(edgeClass));
		}
	}
}
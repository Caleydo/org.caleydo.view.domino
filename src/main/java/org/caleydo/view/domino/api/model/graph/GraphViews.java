/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.view.domino.spi.model.graph.IEdge;
import org.caleydo.view.domino.spi.model.graph.INode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedMaskSubgraph;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.graph.UndirectedMaskSubgraph;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * views on a graph based on a predicate filter
 *
 * @author Samuel Gratzl
 *
 */
public final class GraphViews {
	/**
	 * view on a graph
	 *
	 * @param base
	 * @param node
	 * @param edge
	 * @return
	 */
	public static <N extends INode, E extends IEdge> UndirectedGraph<N, E> graphView(
			UndirectedGraph<? super N, ? super E> base, Class<N> node, Class<E> edge) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<>(node, edge);
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E> UndirectedGraph<N, E> nodeView(UndirectedGraph<? super N, E> base, Class<N> node) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<N, E>(node, Object.class);
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N, E extends IEdge> UndirectedGraph<N, E> edgeView(UndirectedGraph<N, ? super E> base, Class<E> edge) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<N, E>(Object.class, edge);
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E extends IEdge> UndirectedGraph<N, E> graphView(
			UndirectedGraph<? super N, ? super E> base, Predicate<? super N> node, Predicate<? super E> edge) {
		EdgePredicate<N, E> p = new EdgePredicate<>(node, edge);
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E> UndirectedGraph<N, E> nodeView(UndirectedGraph<? super N, E> base, Predicate<? super N> node) {
		EdgePredicate<N, E> p = new EdgePredicate<N, E>(node, Predicates.alwaysTrue());
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N, E extends IEdge> UndirectedGraph<N, E> edgeView(UndirectedGraph<N, ? super E> base,
			Predicate<? super E> edge) {
		EdgePredicate<N, E> p = new EdgePredicate<N, E>(Predicates.alwaysTrue(), edge);
		@SuppressWarnings("unchecked")
		UndirectedGraph<N, E> b = (UndirectedGraph<N, E>) base;
		return new UndirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E extends IEdge> DirectedGraph<N, E> graphView(
			DirectedGraph<? super N, ? super E> base, Class<N> node, Class<E> edge) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<>(node, edge);
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E> DirectedGraph<N, E> nodeView(DirectedGraph<? super N, E> base, Class<N> node) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<N, E>(node, Object.class);
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N, E extends IEdge> DirectedGraph<N, E> edgeView(DirectedGraph<N, ? super E> base, Class<E> edge) {
		EdgeTypePredicate<N, E> p = new EdgeTypePredicate<N, E>(Object.class, edge);
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E extends IEdge> DirectedGraph<N, E> graphView(
			DirectedGraph<? super N, ? super E> base, Predicate<? super N> node, Predicate<? super E> edge) {
		EdgePredicate<N, E> p = new EdgePredicate<>(node, edge);
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N extends INode, E> DirectedGraph<N, E> nodeView(DirectedGraph<? super N, E> base,
			Predicate<? super N> node) {
		EdgePredicate<N, E> p = new EdgePredicate<N, E>(node, Predicates.alwaysTrue());
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	public static <N, E extends IEdge> DirectedGraph<N, E> edgeView(DirectedGraph<N, ? super E> base,
			Predicate<? super E> edge) {
		EdgePredicate<N, E> p = new EdgePredicate<N, E>(Predicates.alwaysTrue(), edge);
		@SuppressWarnings("unchecked")
		DirectedGraph<N, E> b = (DirectedGraph<N, E>) base;
		return new DirectedMaskSubgraph<N, E>(b, p);
	}

	private final static class EdgeTypePredicate<N, E> implements MaskFunctor<N, E> {
		private final Class<? super N> node;
		private final Class<? super E> edge;

		public EdgeTypePredicate(Class<? super N> node, Class<? super E> edge) {
			this.node = node;
			this.edge = edge;
		}

		@Override
		public boolean isEdgeMasked(E edge) {
			return !this.edge.isInstance(edge);
		}

		@Override
		public boolean isVertexMasked(N vertex) {
			return !this.node.isInstance(vertex);
		}
	}

	private final static class EdgePredicate<N, E> implements MaskFunctor<N, E> {
		private final Predicate<? super N> node;
		private final Predicate<? super E> edge;

		public EdgePredicate(Predicate<? super N> node, Predicate<? super E> edge) {
			this.node = node;
			this.edge = edge;
		}

		@Override
		public boolean isEdgeMasked(E edge) {
			return !this.edge.apply(edge);
		}

		@Override
		public boolean isVertexMasked(N vertex) {
			return !this.node.apply(vertex);
		}
	}
}

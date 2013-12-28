/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphDragInfo extends ANodeDragInfo implements INodeCreator {
	private final Set<INodeCreator> nodes;
	private final Table<INodeCreator, INodeCreator, EDirection> edges;
	private final INodeCreator primary;

	public GraphDragInfo(Table<INodeCreator, INodeCreator, EDirection> edges, INodeCreator primary, Vec2f mousePos) {
		super(mousePos);
		this.edges = edges;
		this.primary = primary;
		this.nodes = ImmutableSet.<INodeCreator> builder().addAll(edges.columnKeySet()).addAll(edges.rowKeySet())
				.build();
	}

	@Override
	public INode apply(EDnDType input) {
		return primary.apply(input);
	}

	/**
	 * @return the nodes, see {@link #nodes}
	 */
	public Set<INodeCreator> getNodes() {
		return nodes;
	}

	/**
	 * @return the edges, see {@link #edges}
	 */
	public Table<INodeCreator, INodeCreator, EDirection> getEdges() {
		return edges;
	}

	@Override
	public String getLabel() {
		return StringUtils.join(Collections2.transform(nodes, Labels.TO_LABEL), ", ");
	}

	public void dragGraph(DominoGraph graph, INode primary, EDnDType type) {
		Table<INodeCreator, INodeCreator, EDirection> ledges = HashBasedTable.create(edges);
		dragGraph(graph, ledges, this.primary, primary, type);
	}


	private static void dragGraph(DominoGraph graph, Table<INodeCreator, INodeCreator, EDirection> edges,
			INodeCreator start, INode startNode, EDnDType type) {
		final Map<INodeCreator, EDirection> row = ImmutableMap.copyOf(edges.row(start));
		final Map<INodeCreator, EDirection> col = ImmutableMap.copyOf(edges.column(start));
		edges.row(start).clear();
		edges.column(start).clear();

		for (Map.Entry<INodeCreator, EDirection> entry : row.entrySet()) {
			INode node = entry.getKey().apply(type);
			addToGraph(graph, startNode, node, entry.getValue());
			dragGraph(graph, edges, entry.getKey(), node, type);
		}
		for (Map.Entry<INodeCreator, EDirection> entry : col.entrySet()) {
			INode node = entry.getKey().apply(type);
			addToGraph(graph, startNode, node, entry.getValue().opposite());
			dragGraph(graph, edges, entry.getKey(), node, type);
		}
	}

	private static void addToGraph(DominoGraph graph, INode a, INode b, EDirection dir) {
		if (!graph.contains(b)) {
			graph.addVertex(b);
		} else {
			graph.moveToAlone(b);
		}
		graph.magnetic(a, dir, b);
	}

	/**
	 * @return
	 */
	public Set<EDirection> getFreePrimaryDirections() {
		EnumSet<EDirection> r = EnumSet.allOf(EDirection.class);
		r.removeAll(edges.row(primary).values());
		r.removeAll(Collections2.transform(edges.column(primary).values(), EDirection.TO_OPPOSITE));
		return r;
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.internal.ui.DominoBandLayer;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2, IPickingListener {


	private final DominoGraph graph = new DominoGraph();
	private final Routes routes = new Routes();
	private final DominoNodeLayer nodes;

	/**
	 *
	 */
	public GraphElement() {
		setLayout(GLLayouts.LAYERS);

		this.add(new PickableGLElement().onPick(this).setzDelta(-0.1f));

		DominoBandLayer band = new DominoBandLayer(routes);
		this.add(band);

		this.nodes = new DominoNodeLayer(this, graph);
		this.add(nodes);
	}


	@Override
	public void pick(Pick pick) {
		if (pick.getPickingMode() == PickingMode.MOUSE_WHEEL) {
			for (NodeElement elem : nodes.getNodes()) {
				elem.pick(pick);
			}
		}
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		Function<INode, NodeLayoutElement> lookup = Functions.forMap(asLookup(children));
		List<Set<INode>> sets = this.graph.connectedSets();

		List<LayoutBlock> blocks = new ArrayList<>();
		for (Set<INode> block : sets) {
			blocks.add(LayoutBlock.create(block.iterator().next(), graph, lookup));
		}

		float x = 0;
		for (LayoutBlock block : blocks) {
			Vec2f size = block.getSize();
			block.shift(x, 0);
			block.run();
			x += size.x() + 20;
		}

		routes.update(graph, lookup);
		return false;
	}

	/**
	 * @return the graph, see {@link #graph}
	 */
	public DominoGraph getGraph() {
		return graph;
	}

	/**
	 * @param children
	 * @return
	 */
	private Map<INode, NodeLayoutElement> asLookup(List<? extends IGLLayoutElement> children) {
		ImmutableMap.Builder<INode, NodeLayoutElement> b = ImmutableMap.builder();
		for (IGLLayoutElement elem : children)
			b.put(elem.getLayoutDataAs(INode.class, GLLayoutDatas.<INode> throwInvalidException()),
					new NodeLayoutElement(elem));
		return b.build();
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new GraphElement());
	}
}

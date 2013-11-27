/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.prototype.Graph;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2 {

	private final DirectedGraph<INode, IEdge> graph;

	/**
	 *
	 */
	public GraphElement() {
		setLayout(this);
		this.graph = Graph.build();

		createStuff();
	}

	/**
	 *
	 */
	private void createStuff() {
		for (INode node : this.graph.vertexSet()) {
			this.add(new NodeElement(node));
		}
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		List<NodeLayoutElement> nodes = asNodeElements(children);
		float x = 0;
		for (NodeLayoutElement node : nodes) {
			Vec2f s = node.getSize();
			node.asElem().setBounds(x, 0, s.x(), s.y());
			x += s.x() + 2;
		}
		return false;
	}

	/**
	 * @param children
	 * @return
	 */
	private List<NodeLayoutElement> asNodeElements(List<? extends IGLLayoutElement> children) {
		Builder<NodeLayoutElement> builder = ImmutableList.builder();
		for (IGLLayoutElement elem : children)
			builder.add(new NodeLayoutElement(elem));
		return builder.build();
	}

	private static class NodeLayoutElement {
		private final IGLLayoutElement elem;
		private final NodeElement node;
		private final DominoLayoutInfo info;

		public NodeLayoutElement(IGLLayoutElement elem) {
			this.elem = elem;
			this.node = elem.getLayoutDataAs(NodeElement.class, GLLayoutDatas.<NodeElement> throwInvalidException());
			this.info = elem.getLayoutDataAs(DominoLayoutInfo.class,
					GLLayoutDatas.<DominoLayoutInfo> throwInvalidException());
		}

		public INode asNode() {
			return node.getNode();
		}

		public IGLLayoutElement asElem() {
			return elem;
		}

		public Vec2f getSize() {
			return info.getSize();
		}
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new GraphElement());
	}
}

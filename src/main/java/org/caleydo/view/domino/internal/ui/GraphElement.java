/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2 {

	private final MainToolBar topToolBar;
	private final LeftToolBar leftToolBar;

	private final DominoGraph graph = new DominoGraph();
	private final DominoNodeLayer nodes;

	/**
	 *
	 */
	public GraphElement() {
		setLayout(this);

		this.nodes = new DominoNodeLayer(graph);
		this.add(nodes);

		DominoBandLayer band = new DominoBandLayer(graph, nodes);
		band.setzDelta(-0.2f);
		this.add(band);

		this.topToolBar = new MainToolBar(nodes, graph);
		graph.addGraphListener(this.topToolBar);
		this.add(0, this.topToolBar);
		this.leftToolBar = new LeftToolBar();
		this.add(1, this.leftToolBar);
		this.add(2, new DominoBackgroundLayer(nodes, graph));
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		children.get(0).setBounds(0, 0, w, 24);
		children.get(1).setBounds(0, 24, 24, h - 24);

		for (IGLLayoutElement elem : children.subList(2, children.size()))
			elem.setBounds(24, 24, w - 24, h - 24);
		return false;
	}

	/**
	 * @return the topToolBar, see {@link #topToolBar}
	 */
	public MainToolBar getTopToolBar() {
		return topToolBar;
	}


	/**
	 * @return the graph, see {@link #graph}
	 */
	public DominoGraph getGraph() {
		return graph;
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new GraphElement());
	}
}

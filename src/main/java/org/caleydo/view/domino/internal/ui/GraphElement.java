/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer {


	private final DominoGraph graph = new DominoGraph();
	private final Routes routes = new Routes();
	private final DominoNodeLayer nodes;

	/**
	 *
	 */
	public GraphElement() {
		setLayout(GLLayouts.LAYERS);


		DominoBandLayer band = new DominoBandLayer(routes);
		this.add(band);

		this.nodes = new DominoNodeLayer(graph);
		this.add(nodes);

		this.add(0, new DominoBackgroundLayer(nodes, graph));

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

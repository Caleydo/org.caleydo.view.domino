/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;
import org.caleydo.view.domino.internal.ui.model.IDominoGraphListener;
import org.caleydo.view.domino.internal.ui.model.IEdge;
import org.caleydo.view.domino.internal.ui.model.NodeUIState;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;

/**
 * @author Samuel Gratzl
 *
 */
public class MainToolBar extends GLElementContainer implements PropertyChangeListener, IDominoGraphListener,
		ISelectionMixinCallback {

	private final DominoGraph graph;
	private final DominoNodeLayer nodes;

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	public MainToolBar(DominoNodeLayer nodes, DominoGraph graph) {
		super(GLLayouts.flowHorizontal(3));
		this.nodes = nodes;
		this.graph = graph;
		setRenderer(GLRenderers.fillRect(new Color(0.95f)));
		selections.add(DominoGraph.newNodeSelectionManager());
	}

	@Override
	public void vertexAdded(INode vertex, Collection<IEdge> edges) {
		vertex.addPropertyChangeListener(this);
	}

	@Override
	public void vertexRemoved(INode vertex, Collection<IEdge> edges) {
		vertex.removePropertyChangeListener(this);
	}

	@Override
	public void vertexSortingChanged(ISortableNode vertex, EDimension dim) {

	}

	public void addSeparator() {
		this.add(new Separator());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case NodeUIState.PROP_PROXIMITY_MODE:
			break;
		}
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		Set<Integer> items = manager.getElements(SelectionType.SELECTION);
		this.clear();
		for (Integer id : items) {
			final INode node = graph.apply(id);
			if (node != null) {
				this.add(new GLElement(GLRenderers.drawText(node.getLabel())));
				if (node instanceof ISortableNode) {
					this.add(new GLButton().setCallback(new ISelectionCallback() {
						@Override
						public void onSelectionChanged(GLButton button, boolean selected) {
							graph.sortBy((ISortableNode) node, EDimension.DIMENSION);
						}
					}).setRenderer(GLButton.createCheckRenderer("Sort By Dim")));
				}
			}
		}
	}

	private static class Separator extends GLElement {
		public Separator() {
			setSize(5, -1);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.DARK_GRAY);
			g.drawLine(w * 0.5f, 0, w * 0.5f, h);
			super.renderImpl(g, w, h);
		}
	}
}

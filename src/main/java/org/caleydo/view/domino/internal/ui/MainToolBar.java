/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.IStratisfyingableNode;
import org.caleydo.view.domino.api.model.graph.NodeUIState;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.spi.model.graph.IDominoGraphListener;
import org.caleydo.view.domino.spi.model.graph.IEdge;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class MainToolBar extends GLElementContainer implements PropertyChangeListener, IDominoGraphListener,
		ISelectionMixinCallback, ISelectionCallback {

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
		selections.add(DominoNodeLayer.newNodeGroupSelectionManager());
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
	public void vertexSortingChanged(ISortableNode vertex, EDimension dim, boolean stratisfy) {

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
		if (manager.getIDType() == DominoGraph.NODE_IDTYPE) {
			// Set<Integer> items = manager.getElements(SelectionType.SELECTION);
			// this.clear();
			// for (Integer id : items) {
			// final INode node = graph.apply(id);
			// if (node != null) {
			// this.add(new GLElement(GLRenderers.drawText(node.getLabel())));
			// this.add(new NodeToolBar(node, graph));
			// }
			// }
		} else if (manager.getIDType() == DominoNodeLayer.NODE_GROUP_IDTYPE) {
			Set<Integer> items = manager.getElements(SelectionType.SELECTION);
			this.clear();
			for (Integer id : items) {
				NodeGroupElement group = nodes.getGroupNodeById(id);
				if (group != null) {
					this.add(new GLElement(GLRenderers.drawText(group.getLabel())));
					this.add(new NodeGroupToolBar(group, graph));
				}
			}
			if (items.size() > 1) {
				addSeparator();
				addButton("Remove All Nodes", Resources.ICON_DELETE_ALL);
			}
		}
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		switch (button.getTooltip()) {
		case "Remove All Nodes":
			for (NodeGroupToolBar t : Iterables.filter(this, NodeGroupToolBar.class)) {
				graph.remove(t.node);
			}
			break;
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

	/**
	 * @param string
	 * @param iconSortDim
	 */
	private void addButton(String string, URL iconSortDim) {
		GLButton b = new GLButton();
		b.setCallback(this);
		b.setRenderer(GLRenderers.fillImage(iconSortDim));
		b.setTooltip(string);
		b.setSize(24, -1);
		this.add(b);
	}

	private static class NodeGroupToolBar extends GLElementContainer implements GLButton.ISelectionCallback, IGLLayout2 {
		private final INode node;
		private final DominoGraph graph;
		private final NodeGroupElement elem;

		public NodeGroupToolBar(NodeGroupElement elem, DominoGraph graph) {
			this.graph = graph;
			this.node = elem.asNode();
			this.elem = elem;
			setLayout(this);
			if (node instanceof ISortableNode) {
				ISortableNode snode = (ISortableNode) node;
				if (snode.isSortable(EDimension.DIMENSION))
					addButton("Sort Dim", Resources.ICON_SORT_DIM);
				if (snode.isSortable(EDimension.RECORD)) {
					addButton("Sort Rec", Resources.ICON_SORT_REC);
				}
			}
			if (node instanceof IStratisfyingableNode) {
				IStratisfyingableNode snode = (IStratisfyingableNode) node;
				if (snode.isStratisfyable(EDimension.DIMENSION))
					addButton("Stratify Dim", Resources.ICON_STRATIFY_DIM);
				if (snode.isStratisfyable(EDimension.RECORD)) {
					addButton("Stratify Rec", Resources.ICON_STRATIFY_REC);
				}
			}
			addButton("Remove Node", Resources.ICON_DELETE);
			if (elem.canBeRemoved())
				addButton("Remove Group", Resources.ICON_DELETE);
		}

		@Override
		public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
				int deltaTimeMs) {
			float x = 0;
			for (IGLLayoutElement child : children) {
				child.setBounds(x, 0, h, h);
				x += h + 3;
			}
			return false;
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			switch (button.getTooltip()) {
			case "Sort Dim":
				graph.sortBy((ISortableNode) node, EDimension.DIMENSION);
				break;
			case "Sort Rec":
				graph.sortBy((ISortableNode) node, EDimension.RECORD);
				break;
			case "Stratify Dim":
				graph.stratifyBy((IStratisfyingableNode) node, EDimension.DIMENSION);
				break;
			case "Stratify Rec":
				graph.stratifyBy((IStratisfyingableNode) node, EDimension.RECORD);
				break;
			case "Remove Node":
				graph.remove(node);
				break;
			case "Remove Group":
				elem.removeGroup();
			}
		}

		/**
		 * @param string
		 * @param iconSortDim
		 */
		private void addButton(String string, URL iconSortDim) {
			GLButton b = new GLButton();
			b.setCallback(this);
			b.setRenderer(GLRenderers.fillImage(iconSortDim));
			b.setTooltip(string);
			this.add(b);
		}

	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveDragInfo;
import org.caleydo.view.domino.internal.ui.DominoBandLayer;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.Nodes;
import org.caleydo.view.domino.internal.ui.prototype.StratificationNode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2, IPickingListener {


	private final DominoGraph graph = new DominoGraph();
	private final Routes routes = new Routes();
	private final DominoNodeLayer nodes;
	private final IDropGLTarget dropTarget = new IDropGLTarget() {
		@Override
		public void onItemChanged(IDnDItem item) {

		}

		@Override
		public void onDrop(IDnDItem item) {
			IDragInfo info = item.getInfo();
			Vec2f pos = toRelative(item.getMousePos());
			if (info instanceof NodeDragInfo) {
				NodeDragInfo ni = (NodeDragInfo) info;
				INode n = ni.getNode();
				if (item.getType() == EDnDType.COPY) {
					n = n.clone();
					n.setLayoutData(pos);
					graph.addVertex(n);
				} else {
					graph.detach(n, true);
					ANodeElement elem = nodes.find(n);
					if (elem != null)
						elem.setLocation(pos.x(), pos.y());
				}
			} else if (info instanceof TablePerspectiveDragInfo) {
				INode node = Nodes.create(((TablePerspectiveDragInfo) info).getTablePerspective());
				node.setLayoutData(pos);
				graph.addVertex(node);
			} else if (info instanceof PerspectiveDragInfo) {
				PerspectiveDragInfo pinfo = (PerspectiveDragInfo) info;
				StratificationNode node = new StratificationNode(pinfo.getPerspective(), pinfo.getDim(),
						pinfo.getReferenceID());
				node.setLayoutData(pos);
				graph.addVertex(node);
			}
		}

		@Override
		public EDnDType defaultSWTDnDType(IDnDItem item) {
			return EDnDType.MOVE;
		}

		@Override
		public boolean canSWTDrop(IDnDItem item) {
			IDragInfo info = item.getInfo();
			return info instanceof NodeDragInfo || info instanceof TablePerspectiveDragInfo
					|| info instanceof PerspectiveDragInfo;
		}
	};

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
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			for (ANodeElement elem : nodes.getNodes())
				elem.pick(pick);
			break;
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(dropTarget);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(dropTarget);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		if (children.isEmpty())
			return false;
		Function<INode, NodeLayoutElement> lookup = Functions.forMap(asLookup(children));
		List<Set<INode>> sets = this.graph.connectedSets();

		List<LayoutBlock> blocks = new ArrayList<>();
		for (Set<INode> block : sets) {
			if (areAllDragged(Collections2.transform(block, lookup)))
				continue;
			blocks.add(LayoutBlock.create(block.iterator().next(), graph, lookup));
		}

		float x = 0;
		for (LayoutBlock block : blocks) {
			Vec2f size = block.getSize();
			float xi = block.getInfo().x;
			if (GLLayouts.isDefault(xi))
				xi = x;
			float yi = block.getInfo().y;
			if (GLLayouts.isDefault(yi))
				yi = 0;
			block.shift(xi, yi);
			block.run();
			x += size.x() + 20;
		}

		routes.update(graph, lookup);
		return false;
	}

	/**
	 * @param transform
	 * @return
	 */
	private boolean areAllDragged(Collection<NodeLayoutElement> t) {
		for (NodeLayoutElement elem : t)
			if (!elem.isDragged())
				return false;
		return true;
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

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;
import org.caleydo.view.domino.internal.ui.model.NodeUIState;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;

import com.google.common.base.Supplier;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANodeElement extends GLElementContainer implements IHasMinSize, IGLLayout2, IPickingListener,
		PropertyChangeListener,INodeUI {
	protected static final int BORDER = 2;
	protected final PickingBarrier content;
	protected final INode node;
	private final INodeUI nodeUI;

	private int mainPickingId;

	public ANodeElement(INode node) {
		setLayout(this);
		this.node = node;
		this.node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);

		Vec2f pos = node.getLayoutDataAs(Vec2f.class, null);
		if (pos != null)
			setLocation(pos.x(), pos.y());
		this.nodeUI = node.createUI();
		this.content = new PickingBarrier(this.nodeUI.asGLElement());
		this.add(content);

		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		guessZoomSettings();
		Vec2f s = getPreferredSize();
		this.setSize(s.x(), s.y());
	}

	/**
	 *
	 */
	private void guessZoomSettings() {

	}

	@Override
	protected void init(IGLElementContext context) {
		mainPickingId = context.registerPickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onMainPick(pick);
			}
		});
		super.init(context);
	}

	@Override
	protected void takeDown() {
		context.unregisterPickingListener(mainPickingId);
		super.takeDown();
	}

	protected void setContentPickable(boolean pickable) {
		this.content.setVisibility(pickable ? EVisibility.PICKABLE : EVisibility.VISIBLE);
	}

	/**
	 * @param pick
	 */
	protected void onMainPick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		DominoGraph graph = findGraph();
		switch (pick.getPickingMode()) {
		case RIGHT_CLICKED:
			graph.remove(node);
			// graph.transpose(node);
			break;
		default:
			break;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ANodeElement other = (ANodeElement) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	public NodeUIState getUIState() {
		return node.getUIState();
	}

	/**
	 * @return the nodeUI, see {@link #nodeUI}
	 */
	public INodeUI getNodeUI() {
		return nodeUI;
	}

	@Override
	public GLElement getToolBar() {
		return nodeUI.getToolBar();
	}

	@Override
	public double getSize(EDimension dim) {
		return nodeUI.getSize(dim);
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		DominoGraph graph = findGraph();
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			node.getUIState().zoom(event);
			break;
		case RIGHT_CLICKED:
			graph.remove(node);
			break;
		case DOUBLE_CLICKED:
			if (node instanceof ISortableNode)
				graph.sortBy((ISortableNode) node, EDimension.DIMENSION);
			break;
		default:
			break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case INode.PROP_TRANSPOSE:
			NodeUIState s = node.getUIState();
			Vec2f z = s.getZoom();
			s.setZoom(z.y(), z.x());
			break;
		}
	}

	/**
	 * @return
	 */
	protected final DominoGraph findGraph() {
		return findParent(GraphElement.class).getGraph();
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		GLLayouts.LAYERS.doLayout(children, w, h, parent, deltaTimeMs);
		return false;
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (getVisibility() == EVisibility.PICKABLE) {
			g.pushName(mainPickingId);
			g.fillRect(0, 0, w, h);
			g.popName();
		}
		super.renderPickImpl(g, w, h);
	}


	protected Vec2f getNodeSize() {
		return new Vec2f(fix(nodeUI.getSize(EDimension.DIMENSION)), fix(nodeUI.getSize(EDimension.RECORD)));
	}

	/**
	 * @param size
	 * @return
	 */
	private float fix(double size) {
		return size <= 0 ? (node instanceof PlaceholderNode ? 100 : 1) : (float) size;
	}


	/**
	 * @return the node, see {@link #node}
	 */
	@Override
	public INode asNode() {
		return node;
	}

	@Override
	public GLElement asGLElement() {
		return this;
	}

	@Override
	public boolean setData(EDimension dim, TypedList data) {
		return nodeUI.setData(dim, data);
	}

	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(node))
			return clazz.cast(node);
		return super.getLayoutDataAs(clazz, default_);
	}

	/**
	 * @return
	 */
	public Vec2f getPreferredSize() {
		Vec2f s = getNodeSize().copy();
		Vec2f z = node.getUIState().getZoom();
		s.setX(s.x() * z.x());
		s.setY(s.y() * z.y());
		return s;
	}
}

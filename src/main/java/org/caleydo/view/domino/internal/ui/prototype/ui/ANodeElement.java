/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

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
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;
import org.caleydo.view.domino.spi.config.ElementConfig;

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
	private float scale = 20;
	protected DominoLayoutInfo info;

	private int mainPickingId;

	public ANodeElement(INode node) {
		setLayout(this);
		this.info = new DominoLayoutInfo(this, ElementConfig.ALL);
		DominoLayoutInfo old = node.getLayoutDataAs(DominoLayoutInfo.class, null);
		if (old != null)
			this.info.fromOld(old);
		else {
			Vec2f pos = node.getLayoutDataAs(Vec2f.class, null);
			if (pos != null)
				setLocation(pos.x(), pos.y());
			this.info.setZoomFactor(0.2f, 0.2f); // todo better values
			node.setLayoutData(this.info);
		}
		this.nodeUI = node.createUI();
		this.content = new PickingBarrier(this.nodeUI.asGLElement());
		this.node = node;
		this.node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);
		this.add(content);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		Vec2f s = this.info.getSize();
		this.setSize(s.x(), s.y());
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

	/**
	 * @return the info, see {@link #info}
	 */
	public DominoLayoutInfo getInfo() {
		return info;
	}

	/**
	 * @return the nodeUI, see {@link #nodeUI}
	 */
	public INodeUI getNodeUI() {
		return nodeUI;
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			info.zoom(event);
			break;
		default:
			break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case INode.PROP_TRANSPOSE:
			this.info.transpose();
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
		return new Vec2f(fix(node.getSize(EDimension.DIMENSION)), fix(node.getSize(EDimension.RECORD)));
	}

	/**
	 * @param size
	 * @return
	 */
	private float fix(int size) {
		return size <= 0 ? (node instanceof PlaceholderNode ? 100 : 1) : size;
	}

	/**
	 * @return the scale, see {@link #scale}
	 */
	public float getScale() {
		return scale;
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
	public void setData(EDimension dim, TypedList data) {
		nodeUI.setData(dim, data);
	}

	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(node))
			return clazz.cast(node);
		if (clazz.isInstance(info))
			return clazz.cast(info);
		return super.getLayoutDataAs(clazz, default_);
	}
}

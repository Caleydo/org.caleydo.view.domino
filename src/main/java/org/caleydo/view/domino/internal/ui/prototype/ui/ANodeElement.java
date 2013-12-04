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
		PropertyChangeListener {
	protected static final int BORDER = 2;
	protected final GLElement content;
	protected final INode node;
	private float scale = 20;
	protected DominoLayoutInfo info;

	private int mainPickingId;

	public ANodeElement(INode node) {
		setLayout(this);
		this.info = new DominoLayoutInfo(this, ElementConfig.ALL);
		this.content = node.createUI();
		this.node = node;
		this.node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);
		this.add(content);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
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

	/**
	 * @param pick
	 */
	protected void onMainPick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		DominoGraph graph = findGraph();
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			info.zoom(event);
			break;
		case RIGHT_CLICKED:
			graph.transpose(node);
			break;
		default:
			break;
		}
	}

	/**
	 * @return the info, see {@link #info}
	 */
	public DominoLayoutInfo getInfo() {
		return info;
	}

	@Override
	public void pick(Pick pick) {

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
		return size <= 0 ? (node instanceof PlaceholderNode ? 10 : 1) : size;
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
	public INode getNode() {
		return node;
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

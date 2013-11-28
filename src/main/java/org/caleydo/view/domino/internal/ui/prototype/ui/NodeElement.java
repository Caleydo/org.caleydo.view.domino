/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.ReScaleBorder;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;
import org.caleydo.view.domino.spi.config.ElementConfig;
import org.eclipse.swt.SWT;

import com.google.common.base.Supplier;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeElement extends GLElementContainer implements IHasMinSize, IGLLayout2, IPickingListener {
	private static final int BORDER = 2;
	private final GLElement content;
	private final INode node;
	private float scale = 20;
	private DominoLayoutInfo info;
	private final IBorderGLRenderer border;

	public NodeElement(INode node) {
		setLayout(this);
		this.info = new DominoLayoutInfo(this, ElementConfig.ALL);
		this.content = node.createUI();
		this.node = node;
		this.add(content);
		this.border = Borders.createBorder(Color.BLACK);
		this.add(new ReScaleBorder(info).setRenderer(border));
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		boolean isToolBar = pick.getObjectID() > 0;
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!isToolBar) {
				border.setColor(SelectionType.MOUSE_OVER.getColor());
				info.setHovered(true);
			}
			break;
		case MOUSE_OUT:
			if (!isToolBar) {
				border.setColor(Color.BLACK);
				info.setHovered(info.isSelected());
			}
			break;
		case CLICKED:
			if (((isToolBar && !event.isCtrlDown()) || (!isToolBar && event.isCtrlDown())) && !pick.isAnyDragging()) {
				pick.setDoDragging(true);
			}
			if (isToolBar)
				info.setSelected(true);
			break;
		case DRAGGED:
			if (!pick.isDoDragging() || !info.getConfig().canMove())
				return;
			context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
			info.shift(pick.getDx(), pick.getDy());
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging()) {
				context.getSWTLayer().setCursor(-1);
			}
			if (isToolBar)
				info.setSelected(event.isCtrlDown()); // multi selection
			break;
		case MOUSE_WHEEL:
			info.zoom(event);
			break;
		case DOUBLE_CLICKED:
			if (node instanceof ISortableNode) {
				Vec2f relative = toRelative(pick.getPickedPoint());
				Vec2f size = getSize();
				ISortableNode s = (ISortableNode) node;

				if (s.isSortable(EDimension.DIMENSION) != s.isSortable(EDimension.RECORD)) { // just in one direction
					findGraph().sortBy(s, EDimension.get(s.isSortable(EDimension.DIMENSION)));
				} else {
					boolean inX = relative.x() < size.x() * 0.25f || relative.x() > size.x() * 0.75f;
					boolean inY = relative.y() < size.y() * 0.25f || relative.y() > size.y() * 0.75f;
					if (inX != inY) // not the same, so one of them but not both or none
						findGraph().sortBy(s, EDimension.get(inX));
				}
			}
			findGraph().remove(node);
			break;
		default:
			break;
		}
	}

	/**
	 * @return
	 */
	private DominoGraph findGraph() {
		return findParent(GraphElement.class).getGraph();
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		GLLayouts.LAYERS.doLayout(children, w, h, parent, deltaTimeMs);
		return false;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// g.color(Color.BLACK).drawRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}

	@Override
	public Vec2f getMinSize() {
		Vec2f s = getNodeSize();
		s.add(new Vec2f(BORDER * 2, BORDER * 2));
		return s;
	}

	private Vec2f getNodeSize() {
		return new Vec2f(fix(node.getSize(EDimension.DIMENSION)), fix(node.getSize(EDimension.RECORD)));
	}

	/**
	 * @param size
	 * @return
	 */
	private float fix(int size) {
		return size <= 0 ? 1 : size;
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

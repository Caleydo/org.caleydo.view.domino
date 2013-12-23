/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.Nodes;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Supplier;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceholderNodeElement extends PickableGLElement implements IDropGLTarget, INodeElement {

	private final PlaceholderNode node;
	private boolean armed;

	public PlaceholderNodeElement(PlaceholderNode node) {
		this.node = node;
		Vec2f pos = node.getLayoutDataAs(Vec2f.class, null);
		if (pos != null)
			setLocation(pos.x(), pos.y());
		setPicker(GLRenderers.fillRect(Color.LIGHT_BLUE));

		setSize((float) getSize(EDimension.DIMENSION), (float) getSize(EDimension.RECORD));
	}

	@Override
	public PlaceholderNode asNode() {
		return node;
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDropTarget(this);
		super.takeDown();
	}

	protected final DominoGraph findGraph() {
		return findParent(GraphElement.class).getGraph();
	}

	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(node))
			return clazz.cast(node);
		return super.getLayoutDataAs(clazz, default_);
	}

	@Override
	protected void onMouseOver(Pick pick) {
		context.getMouseLayer().addDropTarget(this);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		context.getMouseLayer().removeDropTarget(this);
		if (armed) {
			armed = false;
			repaint();
		}
	}

	@Override
	public double getSize(EDimension dim) {
		return 100;
	}

	@Override
	public boolean setData(EDimension dim, TypedGroupList data) {
		return false;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(Color.DARK_BLUE).fillRect(0, 0, w, h);
		g.gl.glEnable(GL2.GL_LINE_STIPPLE);
		g.gl.glLineStipple(2, (short) 0xAAAA);
		g.lineWidth(2);
		g.color(armed ? 0.80f : 0.95f).fillRoundedRect(0, 0, w, h, 5);
		g.color(Color.GRAY).drawRoundedRect(0, 0, w, h, 5);
		g.gl.glDisable(GL2.GL_LINE_STIPPLE);
		g.lineWidth(1);
		super.renderImpl(g, w, h);
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		boolean b = Nodes.canExtract(item);
		return b;
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	public void onDrop(IDnDItem item) {
		INode n = Nodes.extract(item);
		DominoGraph graph = findGraph();
		final DominoNodeLayer nodes = findParent(DominoNodeLayer.class);
		graph.replace(this.node, n);
		EventPublisher.trigger(new HidePlaceHoldersEvent().to(nodes));
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		if (!armed) {
			armed = true;
			repaint();
		}
	}

	@Override
	public void pick(Pick pick) {
		this.onPicked(pick);
	}
}

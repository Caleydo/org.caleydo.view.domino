/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.internal.dnd.DragElement;

/**
 * @author Samuel Gratzl
 *
 */
public class Placeholder extends PickableGLElement implements IDropGLTarget {
	private final Node neighbor;
	private final EDirection dir;
	private final boolean transpose;
	private boolean isDetached;

	public Placeholder(Node neighbor, EDirection dir, boolean transpose) {
		this(neighbor, dir, transpose, 0);
	}

	public Placeholder(Node neighbor, EDirection dir, boolean transpose, float detachedOffset) {
		this.neighbor = neighbor;
		this.dir = dir;
		this.transpose = transpose;
		this.isDetached = detachedOffset > 0;

		Vec2f size = neighbor.getSize();
		Vec2f loc = neighbor.getAbsoluteLocation();
		final int c = 50;
		final float offset = detachedOffset;
		switch (dir) {
		case ABOVE:
			setBounds(loc.x(), loc.y() - c - offset, size.x(), c);
			break;
		case BELOW:
			setBounds(loc.x(), loc.y() + size.y() + offset, size.x(), c);
			break;
		case LEFT_OF:
			setBounds(loc.x() - c - offset, loc.y(), c, size.y());
			break;
		case RIGHT_OF:
			setBounds(loc.x() + size.x() + offset, loc.y(), c, size.y());
			break;
		}
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDropTarget(this);
		super.takeDown();
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		return info instanceof ADragInfo || Nodes.canExtract(item);
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(g.getGroup().toNode());
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode());
		} else {
			Node node = Nodes.extract(item);
			dropNode(node);
		}
	}

	private void dropNode(Node node) {
		Domino domino = findParent(Domino.class);
		domino.placeAt(neighbor, dir, node, transpose, isDetached);
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		DragElement current = findParent(Domino.class).getCurrentlyDraggedVis();
		if (current == null)
			return;
		Vec2f loc = getAbsoluteLocation();
		current.stickTo(loc, getSize(), null);
	}

	@Override
	protected void onMouseOver(Pick pick) {
		context.getMouseLayer().addDropTarget(this);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		context.getMouseLayer().removeDropTarget(this);
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		renderPlaceholder(g, 0, 0, w, h, isDetached ? Color.LIGHT_BLUE : Color.LIGHT_GRAY);
	}

	private static void renderPlaceholder(GLGraphics g, float x, float y, float w, float h, Color c) {
		g.color(c);
		g.fillRoundedRect(x, y, w, h, 5);
		g.lineStippled(true).lineWidth(2);
		g.color(Color.GRAY).drawRoundedRect(x, y, w, h, 5);
		g.lineStippled(false).lineWidth(1);
	}

}

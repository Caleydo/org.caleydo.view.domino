/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import javax.media.opengl.GL2;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;

/**
 * @author Samuel Gratzl
 *
 */
public class Placeholder extends PickableGLElement implements IDropGLTarget {
	private final Node neighbor;
	private final EDirection dir;

	private boolean armed;

	public Placeholder(Node neighbor, EDirection dir) {
		this.neighbor = neighbor;
		this.dir = dir;

		Vec2f size = neighbor.getSize();
		Vec2f loc = neighbor.getAbsoluteLocation();
		final int c = 50;
		switch (dir) {
		case ABOVE:
			setBounds(loc.x(), loc.y() - c, size.x(), c);
			break;
		case BELOW:
			setBounds(loc.x(), loc.y() + size.y(), size.x(), c);
			break;
		case LEFT_OF:
			setBounds(loc.x() - c, loc.y(), c, size.y());
			break;
		case RIGHT_OF:
			setBounds(loc.x() + size.x(), loc.y(), c, size.y());
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
		return info instanceof ADragInfo;
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(item, g.getGroup().toNode(), g);
		}
		if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item, item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode(), g);
		}
	}

	private void dropNode(IDnDItem item, Node node, ADragInfo g) {
		Domino domino = findParent(Domino.class);
		domino.placeAt(neighbor, dir, node);
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		if (!armed) {
			armed = true;
			repaint();
		}
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
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
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

}

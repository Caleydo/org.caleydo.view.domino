/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.mock.MockDataDomain;

import v2.data.Categorical2DDataDomainValues;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class Domino extends GLElementContainer implements IDropGLTarget, IPickingListener {
	private int backgroundPickingId;
	/**
	 *
	 */
	public Domino() {
		MockDataDomain d = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "a", "b");
		Node node = new Node(new Categorical2DDataDomainValues(d.getDefaultTablePerspective()));
		Block b = new Block(node);
		setPicker(null);
		this.add(b);
	}


	public static void main(String[] args) {
		GLSandBox.main(args, new Domino());
	}

	@Override
	protected void init(IGLElementContext context) {
		backgroundPickingId = context.registerPickingListener(this);
		super.init(context);
	}

	@Override
	protected void takeDown() {
		context.unregisterPickingListener(backgroundPickingId);
		super.takeDown();
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			break;
		default:
			break;
		}
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.pushName(backgroundPickingId).fillRect(0, 0, w, h).popName();
		super.renderPickImpl(g, w, h);
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		return item.getInfo() instanceof ADragInfo;
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
			dropNode(item, g.getNode(), g);
		}
	}

	private void dropNode(IDnDItem item, Node node, ADragInfo g) {
		if (item.getType() == EDnDType.COPY)
			node = new Node(node);
		else {
			Block block = getBlock(node);
			if (block != null && block.removeNode(node))
				this.remove(block);
		}
		Block b = new Block(node);
		Vec2f pos = toRelative(item.getMousePos());
		b.setLocation(pos.x(), pos.y());
		this.add(b);
	}

	/**
	 * @param node
	 */
	private Block getBlock(Node node) {
		for (Block block : Iterables.filter(this, Block.class))
			if (block.containsNode(node))
				return block;
		return null;
	}

	@Override
	public void onItemChanged(IDnDItem item) {

	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}
}

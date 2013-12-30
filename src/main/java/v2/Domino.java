/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
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
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.mock.MockDataDomain;
import org.caleydo.view.domino.api.model.graph.EDirection;

import v2.data.Categorical2DDataDomainValues;
import v2.data.StratificationDataValue;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class Domino extends GLElementContainer implements IDropGLTarget, IPickingListener {
	private int backgroundPickingId;

	private GLElementContainer placeholders;
	/**
	 *
	 */
	public Domino() {
		setVisibility(EVisibility.PICKABLE);
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT)
					removePlaceholder();
			}
		});

		MockDataDomain d = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "a", "b");
		Node node = new Node(new Categorical2DDataDomainValues(d.getDefaultTablePerspective()));
		this.add(new Block(node));

		MockDataDomain d2 = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "c", "d", "e");
		node = new Node(new Categorical2DDataDomainValues(d2.getDefaultTablePerspective()));
		this.add(new Block(node).setLocation(250, 250));

		TablePerspective grouping = MockDataDomain.addRecGrouping(d2, 100, 50);
		node = new Node(new StratificationDataValue(grouping.getRecordPerspective(), EDimension.RECORD, null));
		this.add(new Block(node).setLocation(20, 250));
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
	protected boolean hasPickAbles() {
		return true;
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
		return item.getInfo() instanceof ADragInfo || Nodes.canExtract(item);
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(item, g.getGroup().toNode());
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item, item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode());
		} else {
			Node node = Nodes.extract(item);
			dropNode(item, node);
		}
	}

	private void dropNode(IDnDItem item, Node node) {
		removeNode(node);
		Block b = new Block(node);
		Vec2f pos = toRelative(item.getMousePos());
		b.setLocation(pos.x(), pos.y());
		this.add(b);
		removePlaceholder();
	}

	/**
	 * @param node
	 */
	public void removeNode(Node node) {
		Block block = getBlock(node);
		if (block != null && block.removeNode(node))
			this.remove(block);
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

	public void addPlaceholdersFor(Node node) {
		if (placeholders != null)
			return;

		placeholders = new GLElementContainer();
		placeholders.setSize(getSize().x(), getSize().y());
		this.add(placeholders);

		final List<GLElement> l = placeholders.asList();
		for(Block block : Iterables.filter(this, Block.class)) {
			l.addAll(block.addPlaceholdersFor(node));
		}
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		if (placeholders == null) {
			if (item.getInfo() instanceof ADragInfo)
				addPlaceholdersFor(((ADragInfo) item.getInfo()).getBaseNode());
			else {
				Node node = Nodes.extract(item);
				addPlaceholdersFor(node);
			}
		}

	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	/**
	 * @param neighbor
	 * @param dir
	 * @param node
	 */
	public void placeAt(Node neighbor, EDirection dir, Node node) {
		removeNode(node);
		Block block = getBlock(neighbor);
		block.addNode(neighbor, dir, node);
		removePlaceholder();
	}

	private void removePlaceholder() {
		this.remove(placeholders);
		placeholders = null;
	}
}

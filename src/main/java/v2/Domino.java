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
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.mock.MockDataDomain;
import org.caleydo.view.domino.api.model.graph.EDirection;

import v2.data.Categorical2DDataDomainValues;
import v2.data.StratificationDataValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class Domino extends GLElementContainer implements IDropGLTarget, IPickingListener {
	private GLElementContainer placeholders;
	private final Bands bands;
	private final GLElementContainer nodes;
	/**
	 *
	 */
	public Domino() {
		super(GLLayouts.LAYERS);
		setVisibility(EVisibility.PICKABLE);
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT)
					removePlaceholder();
			}
		});

		// fakeData();
		this.nodes = new GLElementContainer();
		nodes.setzDelta(0.1f);
		this.add(nodes);

		this.bands = new Bands();
		this.bands.setVisibility(EVisibility.PICKABLE);
		this.bands.setzDelta(0.01f);
		this.bands.onPick(this);
		this.add(this.bands);
	}


	private void fakeData() {
		MockDataDomain d = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "a", "b");
		Node node = new Node(new Categorical2DDataDomainValues(d.getDefaultTablePerspective()));
		nodes.add(new Block(node));

		MockDataDomain d2 = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "c", "d", "e");
		node = new Node(new Categorical2DDataDomainValues(d2.getDefaultTablePerspective()));
		nodes.add(new Block(node).setLocation(250, 250));

		TablePerspective grouping = MockDataDomain.addRecGrouping(d2, 100, 50);
		node = new Node(new StratificationDataValue(grouping.getRecordPerspective(), EDimension.RECORD, null));
		nodes.add(new Block(node).setLocation(20, 250));
	}


	public static void main(String[] args) {
		GLSandBox.main(args, new Domino());
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
		nodes.add(b);
		removePlaceholder();
		bands.relayout();
	}

	/**
	 * @param node
	 */
	public void removeNode(Node node) {
		Block block = getBlock(node);
		if (block != null && block.removeNode(node)) {
			nodes.remove(block);
			bands.relayout();
		}
	}

	/**
	 * @param node
	 */
	private Block getBlock(Node node) {
		for (Block block : Iterables.filter(nodes, Block.class))
			if (block.containsNode(node))
				return block;
		return null;
	}

	public void addPlaceholdersFor(Node node) {
		if (placeholders != null)
			return;

		placeholders = new GLElementContainer();
		this.add(placeholders);

		final List<GLElement> l = placeholders.asList();
		for (Block block : Iterables.filter(nodes, Block.class)) {
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
		bands.relayout();
	}

	private void removePlaceholder() {
		this.remove(placeholders);
		placeholders = null;
	}

	/**
	 * @return
	 */
	public List<Block> getBlocks() {
		return ImmutableList.copyOf(Iterables.filter(nodes, Block.class));
	}
}

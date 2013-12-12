/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.MagneticEdge;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class SuperBlock {
	private final Set<NodeLayoutElement> nodes = Sets.newIdentityHashSet();

	private List<LinearBlock> dimBlocks = new ArrayList<>();
	private List<LinearBlock> recBlocks = new ArrayList<>();

	private Vec2f position;

	public SuperBlock(DominoGraph graph, Set<INode> nodes, Function<INode, NodeLayoutElement> lookup) {
		this.nodes.addAll(Collections2.transform(nodes, lookup));
		for (INode node : nodes) {
			findBlock(node, graph, lookup);
		}

		for (LinearBlock block : blocks()) {
			block.updateBounds(block.getDim().opposite());
			block.applyBounds(block.getDim().opposite());
			block.resort();
			block.apply();
		}

		for (LinearBlock block : blocks()) {
			block.updateBounds(block.getDim());
			block.applyBounds(block.getDim());
		}
		LinearBlock start = blocks().iterator().next();
		NodeLayoutElement first = start.iterator().next();
		placeAll(firsöt, start, 0, 0);

	}

	private void placeAll(NodeLayoutElement shared, LinearBlock block, float x, float y) {
		final EDimension dim = block.getDim();
		final EDimension other = dim.opposite();
		final List<LinearBlock> others = getBlocks(other);
		shared.setLocation(x, y);

		for (NodeLayoutElement b : Lists.reverse(block.before(shared))) {
			if (dim.isHorizontal())
				x -= b.getSize().x();
			else
				y -= b.getSize().y();
			b.setLocation(x, y);
			if (b.asNode().hasDimension(other)) {
				placeAll(b, findBlock(others, b), x, y);
			}
		}
		x = shared.getRectBounds().x2();
		y = shared.getRectBounds().y2();
		for (NodeLayoutElement b : Lists.reverse(block.after(shared))) {
			b.setLocation(x, y);
			if (b.asNode().hasDimension(other)) {
				placeAll(b, findBlock(others, b), x, y);
			}
			if (dim.isHorizontal())
				x += b.getSize().x();
			else
				y += b.getSize().y();
		}
	}

	private List<LinearBlock> getBlocks(EDimension dim) {
		return dim.select(dimBlocks, recBlocks);
	}

	private Iterable<LinearBlock> blocks() {
		return Iterables.concat(dimBlocks, recBlocks);
	}

	private static LinearBlock findBlock(Iterable<LinearBlock> blocks, NodeLayoutElement nodee) {
		for (LinearBlock block : blocks)
			if (block.contains(nodee))
				return block;
		return null;
	}

	/**
	 * @param node
	 * @param graph
	 */
	private void findBlock(INode node, DominoGraph graph, Function<INode, NodeLayoutElement> lookup) {
		for(EDimension dim : EDimension.values()) {
			if (!node.hasDimension(dim))
				continue;
			NodeLayoutElement nodee = lookup.apply(node);
			List<LinearBlock> b = getBlocks(dim);
			LinearBlock block = findBlock(b, nodee);
			if (block != null) // already handled
				continue;
			block = new LinearBlock(dim);
			// find all in that block
			List<INode> neighbors = graph.walkAlong(dim, node, Predicates.instanceOf(MagneticEdge.class));
			block.addAll(Collections2.transform(neighbors, lookup));
		}
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.view.domino.api.model.graph.EDirection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeSelections {
	private final SetMultimap<SelectionType, NodeGroup> selections = HashMultimap.create();

	public static Node getSingleNode(Set<NodeGroup> selection) {
		if (selection.isEmpty())
			return null;
		Set<Node> nodes = getFullNodes(selection);
		if (nodes.size() == 1)
			return nodes.iterator().next();
		return null;
	}

	public static Set<Node> getFullNodes(Set<NodeGroup> selection) {
		if (selection.isEmpty())
			return Collections.emptySet();
		Multiset<Node> nodes = HashMultiset.create();
		for (NodeGroup group : selection) {
			Node n = group.getNode();
			nodes.add(n);
		}
		for (Iterator<Node> it = nodes.elementSet().iterator(); it.hasNext();) {
			Node node = it.next();
			if (node.size() != nodes.count(node)) {
				it.remove();// not all groups
			}
		}
		return nodes.elementSet();
	}

	/**
	 * @param selected
	 * @return
	 */
	public static Block getSingleBlock(Set<NodeGroup> selection) {
		if (selection.isEmpty())
			return null;
		Set<Block> blocks = getFullBlocks(selection);
		if (blocks.size() == 1)
			return blocks.iterator().next();
		return null;
	}

	public static Set<Block> getFullBlocks(Set<NodeGroup> selection) {
		if (selection.isEmpty())
			return Collections.emptySet();
		Set<Node> nodes = getFullNodes(selection);
		if (nodes.isEmpty())
			return Collections.emptySet();
		Multiset<Block> blocks = HashMultiset.create();
		for (Node node : nodes) {
			Block n = node.getBlock();
			blocks.add(n);
		}
		for (Iterator<Block> it = blocks.elementSet().iterator(); it.hasNext();) {
			Block block = it.next();
			if (block.size() != blocks.count(block)) {
				it.remove();// not all groups
			}
		}
		return blocks.elementSet();
	}

	/**
	 * @param selected
	 * @return
	 */
	public static Set<NodeGroup> compress(NodeGroup start, Set<NodeGroup> selected) {
		Set<NodeGroup> linked = new HashSet<>(selected.size());
		compress(start, selected, linked, null);
		return linked;
	}

	private static void compress(NodeGroup n, Set<NodeGroup> selected, Set<NodeGroup> linked, EDirection commingFrom) {
		linked.add(n);
		selected.remove(n);
		for (EDirection dir : EDirection.values()) {
			if (dir == commingFrom)
				continue;
			NodeGroup f = n.findNeigbhor(dir, selected);
			if (f != null) {
				compress(f, selected, linked, dir);
			}
		}
	}

	public Set<SelectionType> cleanup(Node node) {
		Set<SelectionType> r = new HashSet<>(2);
		for (SelectionType type : selections.keySet()) {
			Set<NodeGroup> c = selections.get(type);
			boolean changed = false;
			for (Iterator<NodeGroup> it = c.iterator(); it.hasNext();) {
				NodeGroup g = it.next();
				if (g.getNode() == node) {
					it.remove();
					changed = true;
				}
			}
			if (changed)
				r.add(type);
		}
		return r;
	}

	public Set<SelectionType> cleanup(Block block) {
		Set<SelectionType> r = new HashSet<>(2);
		for (SelectionType type : selections.keySet()) {
			Set<NodeGroup> c = selections.get(type);
			boolean changed = false;
			for (Iterator<NodeGroup> it = c.iterator(); it.hasNext();) {
				NodeGroup g = it.next();
				if (block.containsNode(g.getNode())) {
					it.remove();
					changed = true;
				}
			}
			if (changed)
				r.add(type);
		}
		return r;
	}

	public boolean isSelected(SelectionType type, NodeGroup group) {
		return selections.get(type).contains(group);
	}

	public void select(SelectionType type, NodeGroup group, boolean additional) {
		Set<NodeGroup> c = selections.get(type);
		if (!additional)
			c.clear();
		c.add(group);
	}

	public Set<NodeGroup> getSelection(SelectionType type) {
		return selections.get(type);
	}

	public boolean clear(SelectionType type, NodeGroup group) {
		Set<NodeGroup> c = selections.get(type);
		boolean changed = !c.isEmpty();
		if (group != null)
			changed = c.remove(group);
		else
			c.clear();
		return changed;
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.data.SelectionCommandEvent;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.view.domino.api.model.EDirection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeSelections {
	private final SetMultimap<SelectionType, NodeGroup> selections = HashMultimap.create();
	private final SetMultimap<SelectionType, Block> blockSelections = HashMultimap.create();

	private Collection<ICallback<SelectionType>> selectionChanges = new ArrayList<>();
	private Collection<ICallback<SelectionType>> blockSelectionChanges = new ArrayList<>();

	public void onNodeGroupSelectionChanges(ICallback<SelectionType> callback) {
		this.selectionChanges.add(callback);
	}

	public void onBlockSelectionChanges(ICallback<SelectionType> callback) {
		this.blockSelectionChanges.add(callback);
	}

	public void removeOnNodeGroupSelectionChanges(ICallback<SelectionType> callback) {
		this.selectionChanges.remove(callback);
	}

	public void removeOnBlockSelectionChanges(ICallback<SelectionType> callback) {
		this.blockSelectionChanges.remove(callback);
	}

	private void fire(SelectionType type) {
		for (ICallback<SelectionType> c : selectionChanges)
			c.on(type);
	}

	private void fireBlock(SelectionType type) {
		for (ICallback<SelectionType> c : blockSelectionChanges)
			c.on(type);
	}

	public boolean isSelected(SelectionType type, NodeGroup group) {
		return selections.get(type).contains(group);
	}

	public boolean isSelected(SelectionType type, Block block) {
		return blockSelections.get(type).contains(block);
	}

	public void select(SelectionType type, NodeGroup group, boolean additional) {
		Set<NodeGroup> c = selections.get(type);
		if (!additional)
			c.clear();
		c.add(group);
		fire(type);
	}

	public Set<NodeGroup> getSelection(SelectionType type) {
		return selections.get(type);
	}

	public void select(SelectionType type, Block block, boolean additional) {
		Set<Block> c = blockSelections.get(type);
		if (!additional)
			c.clear();
		c.add(block);
		fireBlock(type);
	}

	public Set<Block> getBlockSelection(SelectionType type) {
		return blockSelections.get(type);
	}

	public void cleanup(Node node) {
		for (SelectionType type : ImmutableSet.copyOf(selections.keySet())) {
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
				fire(type);
		}
	}

	public void cleanup(Block block) {
		for (SelectionType type : ImmutableSet.copyOf(blockSelections.keySet())) {
			if (blockSelections.get(type).remove(block))
				fireBlock(type);
		}

		for (SelectionType type : ImmutableSet.copyOf(selections.keySet())) {
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
				fire(type);
		}
	}



	public boolean clear(SelectionType type, NodeGroup group) {
		Set<NodeGroup> c = selections.get(type);
		boolean changed = !c.isEmpty();
		if (group != null)
			changed = c.remove(group);
		else
			c.clear();
		if (changed)
			fire(type);
		return changed;
	}

	public boolean clear(SelectionType type, Block block) {
		Set<Block> c = blockSelections.get(type);
		boolean changed = !c.isEmpty();
		if (block != null)
			changed = c.remove(block);
		else
			c.clear();
		if (changed)
			fireBlock(type);
		return changed;
	}

	@ListenTo
	private void on(SelectionCommandEvent event) {
		if (event.getSender() == this)
			return;
		SelectionCommand cmd = event.getSelectionCommand();

		switch (cmd.getSelectionCommandType()) {
		case CLEAR:
			clear(cmd.getSelectionType(), (NodeGroup) null);
			clear(cmd.getSelectionType(), (Block) null);
			break;
		case CLEAR_ALL:
		case RESET:
			ImmutableSet<SelectionType> toSend = ImmutableSet.copyOf(blockSelections.keySet());
			blockSelections.clear();
			for (SelectionType t : toSend)
				fireBlock(t);
			toSend = ImmutableSet.copyOf(selections.keySet());
			selections.clear();
			for (SelectionType t : toSend)
				fire(t);
			break;
		}
	}

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
			final int expected = node.groupCount();
			if (expected != nodes.count(node)) {
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
}

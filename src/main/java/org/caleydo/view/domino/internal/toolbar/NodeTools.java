/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;
import org.caleydo.view.domino.internal.NodeSelections;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.undo.ChangeVisTypeToCmd;
import org.caleydo.view.domino.internal.undo.ExplodeSlicesCmd;
import org.caleydo.view.domino.internal.undo.LimitToNodeCmd;
import org.caleydo.view.domino.internal.undo.MergeGroupsCmd;
import org.caleydo.view.domino.internal.undo.RemoveBlockCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeGroupCmd;
import org.caleydo.view.domino.internal.undo.RemoveSliceCmd;
import org.caleydo.view.domino.internal.undo.SortByNodesCmd;
import org.caleydo.view.domino.internal.undo.TransposeBlocksCmd;
import org.caleydo.view.domino.internal.undo.TransposeNodeViewCmd;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeTools extends AItemTools {

	private final Set<NodeGroup> selection;

	public NodeTools(UndoStack undo, NodeGroup group) {
		this(undo, Collections.singleton(group));
	}

	/**
	 * @param selection
	 */
	public NodeTools(UndoStack undo, Set<NodeGroup> selection) {
		super(undo);
		this.selection = selection;
		if (selection.size() == 1) {
			createSingle(selection.iterator().next());
		} else
			createMulti();
	}

	/**
	 * @return the selection, see {@link #selection}
	 */
	public Set<NodeGroup> getSelection() {
		return selection;
	}

	/**
		 *
		 */
	private void createMulti() {
		Set<Node> nodes = NodeSelections.getFullNodes(selection);
		Set<Block> blocks = NodeSelections.getFullBlocks(selection);
		if (nodes.size() == 1) {
			Node node = nodes.iterator().next();
			addSingleNode(node);

			if (blocks.size() == 1) // a single node with a single block
				addBlockActions(blocks);
			if (node.groupCount() == selection.size()) {
				if (blocks.size() == 0)
					addButton("Transpose View", Resources.ICON_TRANSPOSE);
				addButton("Remove Node", Resources.ICON_DELETE_ALL);
			}
		} else if (!nodes.isEmpty() && blocks.isEmpty()) {
			addMultiNodes(nodes);
			addButton("Remove Nodes", Resources.ICON_DELETE_ALL);
		} else if (!blocks.isEmpty()) {
			if (areAllSingleBlocks(blocks)) {
				outer: for (EDimension dim : EDimension.values()) {
					for (Block block : blocks) {
						if (!((Node) block.get(0)).has(dim))
							continue outer;
					}
					if (dim.isHorizontal()) {
						addButton("Sort Dims", Resources.ICON_SORT_DIM);
						addButton("Stratify Dims", Resources.ICON_SORT_DIM);
					} else {
						addButton("Sort Recs", Resources.ICON_SORT_REC);
						addButton("Stratify Recs", Resources.ICON_SORT_REC);
					}
				}
			}
			addBlockActions(blocks);
			addButton("Transpose Blocks", Resources.ICON_TRANSPOSE);
			addMultiNodes(nodes);
			if (blocks.size() == 1)
				addButton("Remove Block", Resources.ICON_DELETE);
			else
				addButton("Remove Blocks", Resources.ICON_DELETE_ALL);
		} else {
			Node node = NodeSelections.getSingleNode(selection, false);
			if (node != null) {
				EDimension dim = node.getSingleGroupingDimension();
				if (dim != null)
					addButton("Merge Groups", dim.select(Resources.ICON_MERGE_DIM, Resources.ICON_MERGE_REC));
			}
		}
	}

	void addBlockActions(Set<Block> blocks) {
		for (EDimension dim : EDimension.values()) {
			boolean canExplode = true;
			for (Block block : blocks) {
				canExplode = canExplode && block.canExplode(dim);
			}

			if (!canExplode)
				continue;

			if (dim.isHorizontal())
				addButton("Explode Dim", Resources.ICON_EXPLODE_DIM);
			else
				addButton("Explode Rec", Resources.ICON_EXPLODE_REC);
		}
	}

	/**
	 * @param blocks
	 * @return
	 */
	private static boolean areAllSingleBlocks(Set<Block> blocks) {
		for (Block b : blocks)
			if (b.size() != 1)
				return false;
		return true;
	}

	private void addMultiNodes(Set<Node> nodes) {
		ButtonBarBuilder b = new ButtonBarBuilder();
		b.layoutAs(EButtonBarLayout.SLIDE_DOWN);
		b.customCallback(new ChangeVisTypeTo(nodes));
		GLElementFactorySwitcher start = nodes.iterator().next().getRepresentableSwitcher();
		if (nodes.size() == 1) {
			this.add(b.build(start, start.getActiveId()));
		} else {
			Collection<GLElementSupplier> s = Lists.newArrayList(start);
			Multiset<String> actives = HashMultiset.create();
			for (Node node : nodes) {
				final GLElementFactorySwitcher swi = node.getRepresentableSwitcher();
				Set<String> ids = getIds(swi);
				for (Iterator<GLElementSupplier> it = s.iterator(); it.hasNext();) {
					if (!ids.contains(it.next().getId()))
						it.remove();
				}
				actives.add(swi.getActiveId());
			}
			if (s.isEmpty())
				return;
			String initialID = mostFrequent(actives);
			this.add(b.build(s, initialID));
		}
	}

	/**
	 * @param actives
	 * @return
	 */
	private static <T> T mostFrequent(Multiset<T> sets) {
		if (sets.isEmpty())
			return null;
		Set<T> elems = sets.elementSet();
		T maxV = elems.iterator().next();
		int max = sets.count(maxV);
		for (T elem : elems) {
			int c = sets.count(elem);
			if (c > max) {
				max = c;
				maxV = elem;
			}
		}
		return maxV;
	}

	private Set<String> getIds(final GLElementFactorySwitcher switcher) {
		final Set<String> ids = new HashSet<>();
		for (GLElementSupplier s : switcher)
			ids.add(s.getId());
		return ids;
	}

	/**
	 * @param next
	 */
	private void createSingle(NodeGroup group) {
		Node node = group.getNode();
		addSingleNode(node);
		final int ngroups = node.groupCount();
		final int nnodes = node.getBlock().size();
		final boolean dimngroups = node.getData(EDimension.DIMENSION).getGroups().size() > 1;
		final boolean recngroups = node.getData(EDimension.RECORD).getGroups().size() > 1;

		if (nnodes > 1 && ngroups == 1)
			addButton("Transpose View", Resources.ICON_TRANSPOSE);

		if (group.canBeRemoved()) {
			if (nnodes == 1)
				addButton("Remove Group", Resources.ICON_DELETE);
			else {
				EDimension dim = node.getSingleGroupingDimension();
				addButton("Remove " + dim.select("Dim", "Rec") + " Slice", Resources.ICON_DELETE);
			}
		} else {
			if (dimngroups && recngroups) {
				addButton("Remove Dim Slice", Resources.ICON_DELETE);
				addButton("Remove Rec Slice", Resources.ICON_DELETE);
			} else if (dimngroups)
				addButton("Remove Dim Slice", Resources.ICON_DELETE);
			else if (recngroups)
				addButton("Remove Rec Slice", Resources.ICON_DELETE);
			else
				addButton("Remove Node", Resources.ICON_DELETE_ALL);
		}

		if (ngroups > 1) {
			addButton("Select All In Node", Resources.ICON_SELECT_ALL);
		}

		if (nnodes > 1) {
			addButton("Select All In Block", Resources.ICON_SELECT_ALL);
		}

		if (group.getNeighbor(EDirection.WEST) != null || group.getNeighbor(EDirection.EAST) != null)
			addButton("Select Hor", Resources.ICON_SELECT_DIM);
		if (group.getNeighbor(EDirection.NORTH) != null || group.getNeighbor(EDirection.SOUTH) != null)
			addButton("Select Ver", Resources.ICON_SELECT_REC);

		GLElement parameter = group.createVisParameter();
		if (parameter != null)
			this.add(parameter);
	}

	/**
	 * @param node
	 */
	private void addSingleNode(Node node) {
		if (node.has(EDimension.DIMENSION)) {
			addButton("Sort Dim", Resources.ICON_SORT_DIM);
			if (node.getUnderlyingData(EDimension.DIMENSION).getGroups().size() > 1)
				addButton("Stratify Dim", Resources.ICON_STRATIFY_DIM);
		}
		if (node.has(EDimension.RECORD)) {
			addButton("Sort Rec", Resources.ICON_SORT_REC);
			if (node.getUnderlyingData(EDimension.RECORD).getGroups().size() > 1)
				addButton("Stratify Rec", Resources.ICON_STRATIFY_REC);
		}
		final boolean recAlone = node.isAlone(EDimension.RECORD);
		if (node.has(EDimension.DIMENSION) && !recAlone) {
			addButton("Limit Dim", Resources.ICON_LIMIT_DATA_DIM);
		}
		final boolean dimAlone = node.isAlone(EDimension.DIMENSION);
		if (node.has(EDimension.RECORD) && !dimAlone) {
			addButton("Limit Rec", Resources.ICON_LIMIT_DATA_REC);
		}
		if (recAlone && dimAlone) {
			addButton("Transpose", Resources.ICON_TRANSPOSE);
		}

		addMultiNodes(Collections.singleton(node));

		addButton("Open Details", Resources.ICON_FOCUS);
	}

	/**
	 *
	 */
	public void triggerDelete() {
		Set<String> toTest = ImmutableSet.of("Remove Node", "Remove Nodes", "Remove Block", "Remove Blocks");
		for (GLButton b : Iterables.filter(this, GLButton.class)) {
			if (toTest.contains(b.getTooltip())) {
				b.setSelected(true);
				break;
			}
		}
	}
	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		NodeGroup node = selection.iterator().next();
		if (!node.isValid())
			return;
		EDimension dim = EDimension.get(button.getTooltip().contains("Dim"));
		switch (button.getTooltip()) {
		case "Sort Dim":
		case "Sort Rec":
			undo.push(new SortByNodesCmd(node.getNode(), dim, false));
			break;
		case "Sort Dims":
		case "Sort Recs":
			undo.push(SortByNodesCmd.multi(NodeSelections.getFullNodes(selection), dim, false));
			break;
		case "Stratify Dim":
		case "Stratify Rec":
			undo.push(new SortByNodesCmd(node.getNode(), dim, true));
			break;
		case "Stratify Dims":
		case "Stratify Recs":
			undo.push(SortByNodesCmd.multi(NodeSelections.getFullNodes(selection), dim, true));
			break;
		case "Limit Dim":
		case "Limit Rec":
			undo.push(new LimitToNodeCmd(node.getNode(), dim));
			break;
		case "Explode Dim":
		case "Explode Rec":
			undo.push(ExplodeSlicesCmd.multi(NodeSelections.getFullBlocks(selection), dim));
			break;
		case "Remove Node":
			undo.push(new RemoveNodeCmd(node.getNode()));
			break;
		case "Remove Nodes":
			undo.push(RemoveNodeCmd.multi(NodeSelections.getFullNodes(selection)));
			break;
		case "Remove Rec Slice":
		case "Remove Dim Slice":
			undo.push(new RemoveSliceCmd(dim, node));
			break;
		case "Remove Group":
			undo.push(new RemoveNodeGroupCmd(node));
			break;
		case "Merge Groups":
			undo.push(new MergeGroupsCmd(node.getNode(), selection));
			break;
		case "Remove Block":
			undo.push(new RemoveBlockCmd(node.getNode().getBlock()));
			break;
		case "Remove Blocks":
			undo.push(RemoveBlockCmd.multi(NodeSelections.getFullBlocks(selection)));
			break;
		case "Select All In Node":
			node.getNode().selectAll();
			break;
		case "Select All In Block":
			node.getNode().getBlock().selectAll();
			break;
		case "Select Hor":
			node.select(EDirection.WEST);
			node.select(EDirection.EAST);
			break;
		case "Select Ver":
			node.select(EDirection.NORTH);
			node.select(EDirection.SOUTH);
			break;
		case "Transpose":
			undo.push(new TransposeBlocksCmd(Collections.singleton(node.getNode().getBlock())));
			break;
		case "Transpose Blocks":
			undo.push(new TransposeBlocksCmd(NodeSelections.getFullBlocks(selection)));
			break;
		case "Transpose View":
			undo.push(new TransposeNodeViewCmd(node.getNode()));
			break;
		case "Open Details":
			node.getNode().showInFocus();
		}
	}

	private class ChangeVisTypeTo implements ISelectionCallback {
		private final Collection<Node> nodes;

		/**
		 * @param singleton
		 */
		public ChangeVisTypeTo(Collection<Node> nodes) {
			this.nodes = nodes;
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			final String id = button.getLayoutDataAs(GLElementSupplier.class, null).getId();
			undo.push(new ChangeVisTypeToCmd(nodes, id));
		}

	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.contextmenu.AContextMenuItem;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.contextmenu.item.SeparatorMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;
import org.caleydo.view.domino.internal.NodeSelections;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.undo.ChangeVisTypeToCmd;
import org.caleydo.view.domino.internal.undo.LimitToNodeCmd;
import org.caleydo.view.domino.internal.undo.MergeGroupsCmd;
import org.caleydo.view.domino.internal.undo.RemoveBlockCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeGroupCmd;
import org.caleydo.view.domino.internal.undo.RemoveSliceCmd;
import org.caleydo.view.domino.internal.undo.SortByNodesCmd;
import org.caleydo.view.domino.internal.undo.TransposeBlocksCmd;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeTools extends GLElementContainer implements GLButton.ISelectionCallback, IGLLayout2 {

	private final Set<NodeGroup> selection;
	private final UndoStack undo;

	public NodeTools(UndoStack undo, NodeGroup group) {
		this(undo, Collections.singleton(group));
	}

	/**
	 * @param selection
	 */
	public NodeTools(UndoStack undo, Set<NodeGroup> selection) {
		this.undo = undo;
		setLayout(this);
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
			EDimension dim = node.getSingleGroupingDimension();
			addSingleNode(node);

			if (dim != null)
				addButton("Merge Groups", dim.select(Resources.ICON_MERGE_DIM, Resources.ICON_MERGE_REC));
			if (node.size() == selection.size()) {
				addButton("Remove Node", Resources.ICON_DELETE_ALL);
			}
		} else if (!nodes.isEmpty() && blocks.isEmpty()) {
			addMultiNodes(nodes);
			addButton("Remove Nodes", Resources.ICON_DELETE_ALL);
		} else if (!blocks.isEmpty()) {
			if (areAllSingleBlocks(blocks)) {
				outer: for (EDimension dim : EDimension.values()) {
					for (Block block : blocks)
						if (!((Node) block.get(0)).has(dim))
							continue outer;
					if (dim.isHorizontal()) {
						addButton("Sort Dims", Resources.ICON_SORT_DIM);
						addButton("Stratify Dims", Resources.ICON_SORT_DIM);
					} else {
						addButton("Sort Recs", Resources.ICON_SORT_REC);
						addButton("Stratify Recs", Resources.ICON_SORT_REC);
					}
				}
			}
			addButton("Transpose Blocks", Resources.ICON_TRANSPOSE);
			addMultiNodes(nodes);
			if (blocks.size() == 1)
				addButton("Remove Block", Resources.ICON_DELETE);
			else
				addButton("Remove Blocks", Resources.ICON_DELETE_ALL);
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

		if (group.canBeRemoved())
			addButton("Remove Group", Resources.ICON_DELETE);
		else
			addButton("Remove Node", Resources.ICON_DELETE_ALL);

		if (node.groupCount() > 1) {
			addButton("Select All In Node", Resources.ICON_SELECT_ALL);
		}
		if (node.getBlock().size() > 1)
			addButton("Select All In Block", Resources.ICON_SELECT_ALL);

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
			addButton("Stratify Dim", Resources.ICON_STRATIFY_DIM);
		}
		if (node.has(EDimension.RECORD)) {
			addButton("Sort Rec", Resources.ICON_SORT_REC);
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

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		float x = 0;
		for (IGLLayoutElement child : children) {
			float wi = GLLayouts.defaultValue(child.getSetWidth(), h);
			child.setBounds(x, 0, wi, h);
			x += wi + 3;
		}
		return false;
	}

	public float getWidth(float h) {
		float r = 0;
		for (GLElement elem : this) {
			float w = elem.getSize().x();
			if (Float.isNaN(w))
				w = h;
			r += w;
		}
		return r + 3 * (size() - 1);
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
		case "Remove Node":
			undo.push(new RemoveNodeCmd(node.getNode()));
			break;
		case "Remove Nodes":
			undo.push(RemoveNodeCmd.multi(NodeSelections.getFullNodes(selection)));
			break;
		case "Remove Slice":
			undo.push(new RemoveSliceCmd(node.getNode(), selection));
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
		case "Open Details":
			node.getNode().showInFocus();
		}
	}

	/**
	 * @param string
	 * @param iconSortDim
	 */
	private void addButton(String string, URL iconSortDim) {
		GLButton b = new GLButton();
		b.setCallback(this);
		b.setRenderer(new ImageRenderer(iconSortDim));
		b.setTooltip(string);
		this.add(b);
	}

	/**
	 * tries to convert the contained buttons to context menu items, that trigger {@link TriggerButtonEvent} events
	 *
	 * @param receiver
	 *            event receiver
	 * @param locator
	 *            loader to load the image for a button
	 * @return
	 */
	public List<AContextMenuItem> asContextMenu() {
		List<AContextMenuItem> items = new ArrayList<>(size());
		for (GLElement elem : this) {
			if (elem instanceof GLButton) {
				items.add(asItem((GLButton) elem));
			} else {
				items.add(SeparatorMenuItem.INSTANCE);
			}
		}
		return items;
	}

	private AContextMenuItem asItem(GLButton elem) {
		String label = Objects.toString(elem.getTooltip(), elem.toString());
		ADirectedEvent event = new TriggerButtonEvent(elem).to(this);
		AContextMenuItem item = new GenericContextMenuItem(label, event);
		// if (elem.getMode() == EButtonMode.CHECKBOX) {
		// item.setType(EContextMenuType.CHECK);
		// item.setState(elem.isSelected());
		// }
		URL imagePath = toImagePath(elem.isSelected() ? elem.getSelectedRenderer() : elem.getRenderer());
		item.setImageURL(imagePath);
		return item;
	}

	@ListenTo(sendToMe = true)
	private void onTriggerButton(TriggerButtonEvent event) {
		GLButton b = event.getButton();
		b.setSelected(!b.isSelected());
	}

	private URL toImagePath(IGLRenderer renderer) {
		if (renderer instanceof ImageRenderer) {
			return ((ImageRenderer) renderer).image;
		}
		return null;
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

	private static class ImageRenderer implements IGLRenderer {
		private final URL image;

		public ImageRenderer(URL image) {
			this.image = image;
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			g.fillImage(image, 0, 0, w, h);
		}
	}
}

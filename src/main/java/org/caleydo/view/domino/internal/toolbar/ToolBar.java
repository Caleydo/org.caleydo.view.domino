/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;
import org.caleydo.view.domino.internal.NodeSelections;
import org.caleydo.view.domino.internal.Resources;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class ToolBar extends GLElementContainer implements ICallback<SelectionType> {

	private final GLElement label;
	private Tools tools;
	private NodeSelections selections;

	/**
	 * @param selections
	 *
	 */
	public ToolBar(NodeSelections selections) {
		super(GLLayouts.flowHorizontal(2));
		this.selections = selections;
		this.selections.onSelectionChanges(this);
		this.label = new GLElement();
		this.add(label);
		setRenderer(GLRenderers.fillRect(Color.LIGHT_BLUE));
	}

	@Override
	public void on(SelectionType type) {
		Set<NodeGroup> s = selections.getSelection(type);
		if (type == SelectionType.MOUSE_OVER) {
			if (s.isEmpty())
				s = selections.getSelection(SelectionType.SELECTION);
			updateLabel(s);
		} else if (type == SelectionType.SELECTION) {
			updateTools(s);
		}
	}

	/**
	 * @param selection
	 */
	private void updateTools(Set<NodeGroup> selection) {
		updateLabel(selection);
		this.remove(tools);
		tools = null;
		if (selection.isEmpty()) {
			return;
		}
		this.tools = new Tools(selection);
		this.add(tools);
	}

	/**
	 * @param selection
	 */
	private void updateLabel(Set<NodeGroup> selection) {
		label.setRenderer(GLRenderers.drawText(StringUtils.join(Collections2.transform(selection, Labels.TO_LABEL),
				", "),
				VAlign.LEFT, new GLPadding(2, 4)));
	}

	private static class Tools extends GLElementContainer implements GLButton.ISelectionCallback, IGLLayout2 {

		private final Set<NodeGroup> selection;

		/**
		 * @param selection
		 */
		public Tools(Set<NodeGroup> selection) {
			setLayout(this);
			this.selection = selection;
			if (selection.size() == 1) {
				createSingle(selection.iterator().next());
			} else
				createMulti();
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
					addButton("Transpose Blocks", Resources.ICON_TRANSPOSE);
				}
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
			final GLElementFactorySwitcher switcher = nodes.iterator().next().getRepresentableSwitcher();
			ButtonBarBuilder b = switcher.createButtonBarBuilder();
			b.layoutAs(EButtonBarLayout.SLIDE_DOWN);
			if (nodes.size() > 1) {
				final Set<String> ids = getIds(switcher);
				for (Node node : nodes) {
					ids.retainAll(getIds(node.getRepresentableSwitcher()));
				}
				if (ids.isEmpty())
					return;
				b.filterBy(Predicates.in(ids));
			}
			b.customCallback(new ChangeVisTypeTo(nodes));
			this.add(b.build());
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

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			NodeGroup node = selection.iterator().next();
			EDimension dim = EDimension.get(button.getTooltip().contains("Dim"));
			switch (button.getTooltip()) {
			case "Sort Dim":
			case "Sort Rec":
				node.getNode().sortByMe(dim);
				break;
			case "Sort Dims":
			case "Sort Recs":
				for (Block b : NodeSelections.getFullBlocks(selection))
					((Node) b.get(0)).sortByMe(dim);
				break;
			case "Stratify Dim":
			case "Stratify Rec":
				node.getNode().stratifyByMe(dim);
				break;
			case "Stratify Dims":
			case "Stratify Recs":
				for (Block b : NodeSelections.getFullBlocks(selection))
					((Node) b.get(0)).stratifyByMe(dim);
				break;
			case "Limit Dim":
			case "Limit Rec":
				node.getNode().limitToMe(dim);
				break;
			case "Remove Node":
				node.getNode().removeMe();
				break;
			case "Remove Nodes":
				for (Node n : NodeSelections.getFullNodes(selection))
					n.removeMe();
				break;
			case "Remove Slice":
				node.getNode().removeSlice(selection);
				break;
			case "Remove Group":
				node.removeMe();
				break;
			case "Remove Block":
				node.getNode().getBlock().removeMe();
				break;
			case "Remove Blocks":
				for (Block b : NodeSelections.getFullBlocks(selection))
					b.removeMe();
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
			case "Merge Groups":
				node.getNode().merge(selection);
				break;
			case "Transpose":
				node.getNode().transpose();
				break;
			case "Transpose Blocks":
				for (Block b : NodeSelections.getFullBlocks(selection))
					((Node) b.get(0)).transpose();
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
			b.setRenderer(GLRenderers.fillImage(iconSortDim));
			b.setTooltip(string);
			this.add(b);
		}

	}

	private static class ChangeVisTypeTo implements ISelectionCallback {
		private final Collection<Node> nodes;

		/**
		 * @param singleton
		 */
		public ChangeVisTypeTo(Collection<Node> nodes) {
			this.nodes = nodes;
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			for (Node node : nodes)
				node.setVisualizationType(button.getLayoutDataAs(GLElementSupplier.class, null).getId());
		}

	}
}

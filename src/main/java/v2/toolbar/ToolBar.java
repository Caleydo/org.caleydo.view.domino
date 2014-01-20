/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.toolbar;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
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
import org.caleydo.view.domino.internal.Resources;

import v2.Block;
import v2.Domino;
import v2.Node;
import v2.NodeGroup;
import v2.NodeSelections;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class ToolBar extends GLElementContainer {

	private final GLElement label;
	private Tools tools;

	/**
	 *
	 */
	public ToolBar() {
		super(GLLayouts.flowHorizontal(2));
		this.label = new GLElement();
		this.add(label);
		setRenderer(GLRenderers.fillRect(Color.LIGHT_BLUE));
	}

	/**
	 * @param type
	 */
	public void update(SelectionType type) {
		Domino domino = findParent(Domino.class);
		if (type == SelectionType.MOUSE_OVER) {
			updateLabel(domino.getSelection(type));
		} else if (type == SelectionType.SELECTION) {
			updateTools(domino.getSelection(type));
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
				addMultiNodes(nodes);
				if (areAllSingleBlocks(blocks)) {
					outer: for (EDimension dim : EDimension.values()) {
						for (Block block : blocks)
							if (!((Node) block.get(0)).has(dim))
								continue outer;
						if (dim.isHorizontal())
							addButton("Sort Dims", Resources.ICON_SORT_DIM);
						else
							addButton("Sort Recs", Resources.ICON_SORT_REC);
					}
					addButton("Transpose Blocks", Resources.ICON_TRANSPOSE);
				}
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

			if (node.size() > 1) {
				addButton("Select All In Node", Resources.ICON_SELECT_ALL);
			}
			if (node.getBlock().size() > 1)
				addButton("Select All In Block", Resources.ICON_SELECT_ALL);

			if (group.getNeighbor(EDirection.LEFT_OF) != null || group.getNeighbor(EDirection.RIGHT_OF) != null)
				addButton("Select Hor", Resources.ICON_SELECT_DIM);
			if (group.getNeighbor(EDirection.ABOVE) != null || group.getNeighbor(EDirection.BELOW) != null)
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
			}
			if (node.has(EDimension.RECORD)) {
				addButton("Sort Rec", Resources.ICON_SORT_REC);
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
			switch (button.getTooltip()) {
			case "Sort Dim":
				node.getNode().sortByMe(EDimension.DIMENSION);
				break;
			case "Sort Dims":
				for (Block b : NodeSelections.getFullBlocks(selection)) {
					((Node) b.get(0)).sortByMe(EDimension.DIMENSION);
				}
				break;
			case "Sort Rec":
				node.getNode().sortByMe(EDimension.RECORD);
				break;
			case "Sort Recs":
				for (Block b : NodeSelections.getFullBlocks(selection)) {
					((Node) b.get(0)).sortByMe(EDimension.RECORD);
				}
				break;
			case "Limit Dim":
				node.getNode().limitToMe(EDimension.DIMENSION);
				break;
			case "Limit Rec":
				node.getNode().limitToMe(EDimension.RECORD);
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
				node.select(EDirection.LEFT_OF);
				node.select(EDirection.RIGHT_OF);
				break;
			case "Select Ver":
				node.select(EDirection.ABOVE);
				node.select(EDirection.BELOW);
				break;
			case "Merge Groups":
				node.getNode().merge(selection);
				break;
			case "Transpose":
				node.getNode().transpose();
				break;
			case "Transpose Blocks":
				for (Block b : NodeSelections.getFullBlocks(selection)) {
					((Node) b.get(0)).transposeMe();
				}
				break;
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
		 * @param node
		 */
		public ChangeVisTypeTo(Node node) {
			this(Collections.singleton(node));
		}

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

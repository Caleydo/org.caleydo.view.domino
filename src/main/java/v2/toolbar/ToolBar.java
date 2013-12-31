/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.toolbar;

import java.net.URL;
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
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.Resources;

import v2.Domino;
import v2.Node;
import v2.NodeGroup;

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
			Node node = getSingleNode(selection);
			EDimension dim = node == null ? null : node.getSingleGroupingDimension();
			if (dim != null)
				addButton("Merge Groups", dim.select(Resources.ICON_MERGE_DIM, Resources.ICON_MERGE_REC));
			if (node != null && node.size() == selection.size()) {
				addButton("Remove Node", Resources.ICON_DELETE_ALL);
			}
		}

		private static Node getSingleNode(Set<NodeGroup> selection) {
			Node node = selection.iterator().next().getNode();
			for (NodeGroup group : selection) {
				Node n = group.getNode();
				if (node != n)
					return null;
			}
			return node;
		}

		/**
		 * @param next
		 */
		private void createSingle(NodeGroup group) {
			Node node = group.getNode();
			if (node.has(EDimension.DIMENSION)) {
				addButton("Sort Dim", Resources.ICON_SORT_DIM);
			}
			if (node.has(EDimension.RECORD)) {
				addButton("Sort Rec", Resources.ICON_SORT_REC);
			}
			if (group.canBeRemoved())
				addButton("Remove Group", Resources.ICON_DELETE);
			else
				addButton("Remove Node", Resources.ICON_DELETE_ALL);
		}

		@Override
		public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
				int deltaTimeMs) {
			float x = 0;
			for (IGLLayoutElement child : children) {
				child.setBounds(x, 0, h, h);
				x += h + 3;
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
			case "Sort Rec":
				node.getNode().sortByMe(EDimension.RECORD);
				break;
			case "Remove Node":
				node.getNode().removeMe();
				break;
			case "Remove Group":
				node.removeMe();
				break;
			case "Merge Groups":
				node.getNode().merge(selection);
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
}

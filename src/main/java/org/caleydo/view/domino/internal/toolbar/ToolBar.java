/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.view.contextmenu.AContextMenuItem;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.NodeGroup;
import org.caleydo.view.domino.internal.NodeSelections;
import org.caleydo.view.domino.internal.UndoStack;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class ToolBar extends GLElementContainer implements ICallback<SelectionType> {

	private final GLElement label;
	private NodeTools tools;
	private NodeSelections selections;
	private UndoStack undo;

	/**
	 * @param selections
	 *
	 */
	public ToolBar(UndoStack undo, NodeSelections selections) {
		super(GLLayouts.flowHorizontal(2));
		this.undo = undo;
		this.selections = selections;
		this.selections.onNodeGroupSelectionChanges(this);
		this.label = new GLElement();
		this.add(label);
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
		this.tools = new NodeTools(undo, selection);
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

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(findParent(Domino.class).getTool().getColor()).fillRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}

	public List<AContextMenuItem> asContextMenu() {
		return tools == null ? Collections.<AContextMenuItem> emptyList() : tools.asContextMenu();
	}
}

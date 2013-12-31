/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.toolbar;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;

import v2.Domino;
import v2.NodeGroup;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class ToolBar extends GLElementContainer {

	private final GLElement label;
	private final GLElementContainer tools;

	/**
	 *
	 */
	public ToolBar() {
		super(GLLayouts.flowHorizontal(2));
		this.label = new GLElement();
		this.add(label);
		this.tools = new GLElementContainer(GLLayouts.flowHorizontal(2));
		this.add(this.tools);
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
		// TODO Auto-generated method stub

	}

	/**
	 * @param selection
	 */
	private void updateLabel(Set<NodeGroup> selection) {
		label.setRenderer(GLRenderers.drawText(StringUtils.join(Collections2.transform(selection, Labels.TO_LABEL),
				", "),
				VAlign.LEFT, new GLPadding(2, 4)));
	}

}

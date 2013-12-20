/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.net.URL;
import java.util.List;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.ui.NodeElement.ENodeUIState;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class LeftToolBar extends GLElementContainer implements ISelectionCallback, IGLLayout2 {
	private final DominoGraph graph;
	private final DominoNodeLayer nodes;

	private final RadioController controller = new RadioController(this);

	public LeftToolBar(DominoNodeLayer nodes, DominoGraph graph) {
		setLayout(this);
		this.nodes = nodes;
		this.graph = graph;
		setRenderer(GLRenderers.fillRect(new Color(0.95f)));

		addButton("Move", Resources.ICON_STATE_MOVE);
		addButton("Select", Resources.ICON_STATE_SELECT);
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		float y = 0;
		for (IGLLayoutElement child : children) {
			child.setBounds(0, y, w, w);
			y += w + 3;
		}
		return false;
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		switch (button.getTooltip()) {
		case "Move":
			for (NodeElement elem : Iterables.filter(nodes, NodeElement.class)) {
				elem.setState(ENodeUIState.MOVE);
			}
			break;
		case "Select":
			for (NodeElement elem : Iterables.filter(nodes, NodeElement.class)) {
				elem.setState(ENodeUIState.SELECT);
			}
			break;
		}
	}

	/**
	 * @param label
	 * @param icon
	 */
	private void addButton(String label, URL icon) {
		GLButton b = new GLButton();
		final IGLRenderer r = GLRenderers.fillImage(icon);
		b.setRenderer(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				r.render(g, w, h, parent);
				g.color(0, 0, 0, .25f).fillRoundedRect(0, 0, w, h, Math.min(w, h) * 0.25f);
			}
		});
		b.setSelectedRenderer(r);
		b.setTooltip(label);
		controller.add(b);
		this.add(b);
	}

}

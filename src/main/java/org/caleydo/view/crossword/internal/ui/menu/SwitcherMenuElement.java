/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.menu;

import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_WIDTH;

import java.net.URL;
import java.util.List;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.IActiveChangedCallback;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.crossword.internal.Resources;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class SwitcherMenuElement extends GLElementContainer implements IGLLayout, IActiveChangedCallback,
		IPickingListener {
	private int active;
	private boolean hovered;

	public SwitcherMenuElement(ISelectionCallback callback) {
		addButton("Close", Resources.deleteIcon(), callback);
		setLayout(this);
		setSize(TOOLBAR_WIDTH, TOOLBAR_WIDTH);
		setVisibility(EVisibility.PICKABLE); // for parent
		this.onPick(this);
	}

	@Override
	protected int getPickingObjectId() {
		return 3; // for parent identification
	}

	public void setVisualizationSwitcher(GLElementFactorySwitcher switcher) {
		List<GLElement> list = this.asList();
		list.subList(1, size()).clear(); // clear previous

		// work on copy as add will remove from the other list
		list.addAll(ImmutableList.copyOf(switcher.createButtonBar().asList()));
		switcher.onActiveChanged(this);
		this.active = switcher.getActive();
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			hovered = true;
			relayout();
			break;
		case MOUSE_OUT:
			hovered = false;
			relayout();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActiveChanged(int active) {
		this.active = active;
		relayout();
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		final int size = children.size();
		assert size <= 8;
		// set all to the same size
		for (IGLLayoutElement child : children)
			child.setSize(w, h);
		switch (size) {
		case 1: // just close center it
			children.get(0).setLocation(0, 0);
			break;
		case 2: // just a single one, just show close
			children.get(0).setLocation(0, 0);
			children.get(1).hide();
			break;
		default:
			if (hovered) { // show as a rect of 3x3
				for (int i = 0; i < Math.min(3, size); ++i)
					children.get(i).setLocation((i - 1) * w, -h);
				if (size > 3)
					children.get(3).setLocation(w, 0);
				for (int i = 4; i < Math.min(4 + 3, size); ++i)
					children.get(i).setLocation((5 - i) * w, h);
				if (size > 7)
					children.get(7).setLocation(-w, 0);
			} else {
				for (IGLLayoutElement child : children)
					child.hide();
			}
			// center active
			final IGLLayoutElement activeChild = children.get(this.active + 1);
			activeChild.setLocation(0, 0);
			activeChild.setSize(w, h);
		}
	}

	private void addButton(String tooltip, URL icon, ISelectionCallback callback) {
		GLButton b = new GLButton();
		b.setTooltip(tooltip);
		b.setHoverEffect(GLRenderers.drawRoundedRect(Color.WHITE));
		b.setRenderer(GLRenderers.fillImage(icon));
		b.setCallback(callback);
		this.add(b);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		boolean fat = hovered && size() > 2;
		if (fat) { // if we are rendering the 3x3 rect
			g.incZ(0.5f);
			g.color(Color.GRAY).fillRect(-w, -h, w * 3, h * 3);
		}
		super.renderImpl(g, w, h);
		if (fat)
			g.incZ(-0.5f);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (hovered && size() > 2)
			g.fillRect(-w, -h, w * 3, h * 3);
		super.renderPickImpl(g, w, h);
	}

}

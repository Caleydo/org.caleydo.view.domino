/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.menu;

import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_WIDTH;
import static org.caleydo.view.crossword.internal.Settings.toolbarBackground;

import java.net.URL;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.view.crossword.api.model.PerspectiveMetaData;
import org.caleydo.view.crossword.internal.Resources;
import org.caleydo.view.crossword.internal.ui.CrosswordLayoutInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class PerspectiveMenuElement extends GLElementContainer {
	private final boolean dim;
	private Perspective perspective;

	public PerspectiveMenuElement(TablePerspective tablePerspective, boolean dim, ISelectionCallback callback,
			PerspectiveMetaData metaData) {
		super(dim ? GLLayouts.flowHorizontal(2) : GLLayouts.flowVertical(2));
		setVisibility(EVisibility.PICKABLE);// pickable for my parent to have common area
		this.dim = dim;
		this.add(new GLElement()); // spacer
		setTablePerspective(tablePerspective, callback, metaData);
	}

	protected final CrosswordLayoutInfo getInfo() {
		return getParent().getLayoutDataAs(CrosswordLayoutInfo.class, null);
	}

	public void setTablePerspective(TablePerspective tablePerspective, ISelectionCallback callback,
			PerspectiveMetaData metaData) {
		this.perspective = dim ? tablePerspective.getDimensionPerspective() : tablePerspective.getRecordPerspective();
		this.asList().subList(1, size()).clear();
		String suffix = (dim ? "X" : "Y");
		if (perspective.getVirtualArray().getGroupList().size() > 1 && !metaData.isSplitted()) {
			addButton("Split " + suffix, Resources.splitPerspective(), callback);
		}
		if (!metaData.isChild() && !metaData.isSplitted())
			addButton("Change Perspective " + suffix, Resources.choosePerspective(), callback);
		repaint();
	}

	private void addButton(String tooltip, URL icon, ISelectionCallback callback) {
		GLButton b = new GLButton();
		b.setTooltip(tooltip);
		b.setHoverEffect(GLRenderers.drawRoundedRect(Color.WHITE));
		b.setRenderer(GLRenderers.fillImage(icon));
		b.setCallback(callback);
		b.setSize(TOOLBAR_WIDTH, TOOLBAR_WIDTH);
		this.add(b);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(toolbarBackground(getInfo().isSelected()));
		RoundedRectRenderer.render(g, 0, 0, w, h, Math.min(w, h) * 0.3f, 3, RoundedRectRenderer.FLAG_FILL
				| (dim ? RoundedRectRenderer.FLAG_TOP_RIGHT : RoundedRectRenderer.FLAG_BOTTOM_LEFT));

		float free = dim ? w : h;
		if (size() > 1)
			free = dim ? get(1).getLocation().x() : get(1).getLocation().y();
		g.textColor(Color.WHITE);
		if (dim) {
			g.drawText(perspective, 1, 1, free, h - 4);
		} else {
			g.save().gl.glRotatef(90, 0, 0, -1);
			g.drawText(perspective.getLabel(), -free, 1, free, w - 4, VAlign.RIGHT);
			g.restore();
		}
		g.textColor(Color.BLACK);
		super.renderImpl(g, w, h);
	}

	@Override
	protected int getPickingObjectId() {
		return dim ? 1 : 2;
	}
}

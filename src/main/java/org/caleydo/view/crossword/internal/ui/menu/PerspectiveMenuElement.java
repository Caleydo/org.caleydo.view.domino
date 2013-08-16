/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.menu;

import static org.caleydo.view.crossword.internal.Settings.toolbarBackground;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.view.crossword.internal.ui.CrosswordLayoutInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class PerspectiveMenuElement extends GLElementContainer {
	private final boolean dim;
	private Perspective perspective;

	public PerspectiveMenuElement(TablePerspective tablePerspective, boolean dim) {
		setVisibility(EVisibility.PICKABLE);// pickable for my parent to have common area
		this.dim = dim;
		setTablePerspective(tablePerspective);
	}

	protected final CrosswordLayoutInfo getInfo() {
		return getParent().getLayoutDataAs(CrosswordLayoutInfo.class, null);
	}

	public void setTablePerspective(TablePerspective tablePerspective) {
		this.perspective = dim ? tablePerspective.getDimensionPerspective() : tablePerspective.getRecordPerspective();
		repaint();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(toolbarBackground(getInfo().isSelected()));
		RoundedRectRenderer.render(g, 0, 0, w, h, Math.min(w, h) * 0.3f, 3, RoundedRectRenderer.FLAG_FILL
				| (dim ? RoundedRectRenderer.FLAG_TOP_RIGHT : RoundedRectRenderer.FLAG_BOTTOM_LEFT));

		g.textColor(Color.WHITE);
		if (dim) {
			g.drawText(perspective, 1, 1, w - 2, h - 4);
		} else {
			g.save().gl.glRotatef(90, 0, 0, -1);
			g.drawText(perspective.getLabel(), -h, 1, h - 2, w - 4, VAlign.RIGHT);
			g.restore();
		}
		g.textColor(Color.BLACK);
		super.renderImpl(g, w, h);
	}

	@Override
	protected int getPickingObjectId() {
		return dim ? 1 : 2;
	}

	// @Override
	// protected void renderImpl(GLGraphics g, float w, float h) {
	// super.renderImpl(g, w, h);
	// float start;
	// if (size() > 1) {
	// start = get(1).getLocation().x();
	// } else
	// start = w;
	// g.textColor(TOOLBAR_TEXT_COLOR()).drawText(label, 2, 1, start - 2 - 4, TOOLBAR_TEXT_HEIGHT)
	// .textColor(Color.BLACK);
	// }
	// }
	//
	// private static class VerticalToolBar extends AToolBar {
	// public VerticalToolBar(GLButton.ISelectionCallback callback, TablePerspective data) {
	// super(RoundedRectRenderer.FLAG_LEFT);
	// setLayout(new GLFlowLayout(false, 2, GLPadding.ONE));
	// addButton("Split X", Resources.cutDimension(), callback, data.getDimensionPerspective().getVirtualArray()
	// .getGroupList().size() > 1);
	// addButton("Split Y", Resources.cutRecord(), callback, data.getRecordPerspective().getVirtualArray()
	// .getGroupList().size() > 1);
	// setSize(-1, TOOLBAR_WIDTH);
	// }
	//
	// private void addButton(String tooltip, URL icon, ISelectionCallback callback, boolean enabled) {
	// GLButton b = new GLButton();
	// b.setTooltip(tooltip);
	// b.setHoverEffect(GLRenderers.drawRoundedRect(Color.WHITE));
	// b.setRenderer(enabled ? GLRenderers.fillImage(icon) : new DisabledImageRenderer(icon));
	// b.setCallback(callback);
	// b.setSize(TOOLBAR_WIDTH, TOOLBAR_WIDTH);
	// this.add(b);
	// }
	// }
	//
	// private static class DisabledImageRenderer implements IGLRenderer {
	// private final URL url;
	//
	// public DisabledImageRenderer(URL url) {
	// this.url = url;
	// }
	//
	// @Override
	// public void render(GLGraphics g, float w, float h, GLElement parent) {
	// g.fillImage(g.getTexture(url), 0, 0, w, h, Color.GRAY);
	// }
	// }
}

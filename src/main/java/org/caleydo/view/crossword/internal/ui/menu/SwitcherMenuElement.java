/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.menu;

import java.net.URL;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.crossword.internal.Resources;
import org.caleydo.view.crossword.internal.Settings;
import org.caleydo.view.crossword.spi.config.ElementConfig;

/**
 * @author Samuel Gratzl
 *
 */
public class SwitcherMenuElement extends GLElementDecorator {
	private final ElementConfig config;
	private final GLButton close;

	public SwitcherMenuElement(ISelectionCallback callback, ElementConfig config) {
		this.close = config.canClose() ? createCloseButton(callback) : null;
		setContent(close);
		this.config = config;
		setVisibility(EVisibility.PICKABLE);
	}

	private GLButton createCloseButton(ISelectionCallback callback) {
		return addButton("Close", Resources.deleteIcon(), callback);
	}

	@Override
	protected int getPickingObjectId() {
		return 3;
	}

	public void setSwitcher(GLElementFactorySwitcher switcher) {
		if (!config.canChangeVis())
			return;

		ButtonBarBuilder builder = switcher.createButtonBarBuilder();
		if (close != null)
			builder.prepend(close);
		GLElement r = builder.size(Settings.TOOLBAR_WIDTH).layoutAs(EButtonBarLayout.HOVER_BLOCK_3x3)
				.build();
		setContent(r);
	}


	private GLButton addButton(String tooltip, URL icon, ISelectionCallback callback) {
		GLButton b = new GLButton();
		b.setTooltip(tooltip);
		b.setHoverEffect(GLRenderers.drawRoundedRect(Color.WHITE));
		b.setRenderer(GLRenderers.fillImage(icon));
		b.setCallback(callback);
		return b;
	}

}

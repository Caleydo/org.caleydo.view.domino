/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.api;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.crossword.internal.Activator;
import org.caleydo.view.crossword.internal.ui.CrosswordLayout;

/**
 * the root element of this view holding a {@link TablePerspective}
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordRootElement extends GLElementContainer {
	public CrosswordRootElement() {
		super(new CrosswordLayout());
	}
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.pushResourceLocator(Activator.getResourceLocator());
		super.renderImpl(g, w, h);
		g.popResourceLocator();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.pushResourceLocator(Activator.getResourceLocator());
		super.renderPickImpl(g, w, h);
		g.popResourceLocator();
	}

}

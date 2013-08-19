/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.model;

import gleem.linalg.Vec2f;

import java.util.List;
import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;

/**
 * @author Samuel Gratzl
 *
 */
public class BandRoute implements IGLRenderer {
	private final List<Vec2f> path;
	private final Color color;

	private final Set<Integer> ids;

	public BandRoute(List<Vec2f> path, Color color, Set<Integer> ids) {
		this.path = path;
		this.color = color;
		this.ids = ids;
	}

	@Override
	public void render(GLGraphics g, float w, float h, GLElement parent) {
		g.color(color);
		g.drawPath(path, false);
	}

}

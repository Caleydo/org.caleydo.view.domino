/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.model;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

/**
 * this class represents a small fragment of a band representation including a path color and a set of represented ids
 *
 * @author Samuel Gratzl
 *
 */
public class BandRoute {
	private final List<Vec2f> path;
	private final Color color;

	private final TypedSet ids;

	public BandRoute(List<Vec2f> path, Color color, TypedSet ids) {
		this.path = path;
		this.color = color;
		this.ids = ids;
	}

	public void render(GLGraphics g, float w, float h, GLElement parent) {
		g.color(color);
		g.drawPath(path, false);
	}

	public void renderPick(GLGraphics g, float w, float h, GLElement parent) {
		render(g, w, h, parent);
	}

}

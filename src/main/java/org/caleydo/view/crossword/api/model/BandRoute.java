/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.model;

import gleem.linalg.Vec2f;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;

/**
 * this class represents a small fragment of a band representation including a path color and a set of represented ids
 *
 * @author Samuel Gratzl
 *
 */
public class BandRoute implements ILabeled {
	private final List<Vec2f> path;
	private final Color color;

	private final TypedSet ids;

	public BandRoute(List<Vec2f> path, Color color, TypedSet ids) {
		this.path = path;
		this.color = color;
		this.ids = ids;
	}

	public void render(GLGraphics g, float w, float h, IBandHost host) {
		g.color(color);
		g.drawPath(path, false);
		int selected = host.getSelected(ids, SelectionType.SELECTION);
		int hovered = host.getSelected(ids, SelectionType.MOUSE_OVER);
		// TODO
	}

	@Override
	public String getLabel() {
		return ids.getIdType().getTypeName() + ": " + StringUtils.join(ids, ",");
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	public TypedSet getIds() {
		return ids;
	}

	public IDType getIdType() {
		return ids.getIdType();
	}

	public void renderPick(GLGraphics g, float w, float h, IBandHost host) {
		g.drawPath(path, false);
	}

	public interface IBandHost {
		int getSelected(TypedSet ids, SelectionType type);

		IGLElementContext getContext();
	}
}

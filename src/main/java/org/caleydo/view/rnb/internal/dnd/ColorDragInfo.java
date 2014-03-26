/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.dnd;

import java.io.Serializable;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IUIDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;

/**
 * @author Samuel Gratzl
 *
 */
public class ColorDragInfo implements IUIDragInfo, Serializable {
	private static final long serialVersionUID = -4641764175334903179L;

	private Color color;

	public ColorDragInfo() {

	}
	public ColorDragInfo(Color color) {
		this.color = color;
	}

	@Override
	public String getLabel() {
		return "#" + color.getHEX();
	}

	@Override
	public GLElement createUI() {
		return new GLElement(GLRenderers.fillRect(color)).setSize(16, 16);
	}

}

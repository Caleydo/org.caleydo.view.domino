/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.net.URL;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.util.gleem.IColored;

/**
 * @author Samuel Gratzl
 *
 */
public enum EToolState implements ILabeled, IColored {
	MOVE, SELECT, BANDS;

	public URL toIcon() {
		switch (this) {
		case BANDS:
			return Resources.ICON_STATE_BANDS;
		case MOVE:
			return Resources.ICON_STATE_MOVE;
		case SELECT:
			return Resources.ICON_STATE_SELECT;
		}
		throw new IllegalStateException();
	}

	@Override
	public String getLabel() {
		return WordUtils.capitalizeFully(name());
	}

	@Override
	public Color getColor() {
		switch (this) {
		case MOVE:
			return Color.LIGHT_BLUE;
		case BANDS:
			return Color.LIGHT_RED;
		case SELECT:
			return new Color(144, 238, 144);
		}
		throw new IllegalStateException();
	}
}

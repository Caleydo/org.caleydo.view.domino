/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.net.URL;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.util.base.ILabeled;

/**
 * @author Samuel Gratzl
 *
 */
public enum EToolState implements ILabeled {
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
}

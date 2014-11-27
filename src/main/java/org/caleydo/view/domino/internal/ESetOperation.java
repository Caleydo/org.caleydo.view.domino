/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.net.URL;

/**
 * @author Samuel Gratzl
 *
 */
public enum ESetOperation {
	INTERSECTION, UNION, DIFFERENCE;

	public URL toIcon() {
		switch (this) {
		case INTERSECTION:
			return Resources.ICON_SET_INTERSECT;
		case UNION:
			return Resources.ICON_SET_UNION;
		case DIFFERENCE:
			return Resources.ICON_SET_DIFFERENCE;
		}
		throw new IllegalStateException();
	}
}

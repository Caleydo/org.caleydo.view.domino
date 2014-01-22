/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.plugin;

import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class Settings {

	public static final int TOOLBAR_WIDTH = 16;
	public static final int TOOLBAR_TEXT_HEIGHT = 14;

	public static Color TOOLBAR_TEXT_COLOR() {
		return Color.WHITE;
	}

	public static Color toolbarBackground(boolean isSelected) {
		return !isSelected ? Color.BLACK : Color.DARK_BLUE;
	}

	public static final float SCROLLBAR_WIDTH = 10;
}

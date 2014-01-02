/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;

/**
 * special decorator, which acts as a picking barrier
 *
 * @author Samuel Gratzl
 *
 */
public class PickingBarrier extends GLElementDecorator {
	public PickingBarrier(GLElement wrappee) {
		super(wrappee);
		setPicker(null);
	}

	public PickingBarrier() {
		setPicker(null);
	}

	@Override
	protected boolean hasPickAbles() {
		return getVisibility() == EVisibility.PICKABLE;
	}

	public boolean isPickable() {
		return getVisibility() == EVisibility.PICKABLE;
	}

	public void setPickable(boolean pickable) {
		setVisibility(pickable ? EVisibility.PICKABLE : EVisibility.VISIBLE);
	}
}

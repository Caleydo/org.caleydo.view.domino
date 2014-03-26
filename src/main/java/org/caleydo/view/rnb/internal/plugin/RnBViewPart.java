/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.rnb.internal.plugin;

import org.caleydo.core.view.ARcpGLElementViewPart;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.view.rnb.internal.serial.SerializedDominoView;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class RnBViewPart extends ARcpGLElementViewPart {

	/**
	 * Constructor.
	 */
	public RnBViewPart() {
		super(SerializedDominoView.class);
	}

	@Override
	protected AGLElementView createView(IGLCanvas canvas) {
		return new RnBView(canvas);
	}

	@Override
	public RnBView getView() {
		return (RnBView) super.getView();
	}
}
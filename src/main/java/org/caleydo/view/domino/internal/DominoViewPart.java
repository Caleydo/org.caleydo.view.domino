/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal;

import org.caleydo.core.view.ARcpGLElementViewPart;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.view.domino.internal.action.SettingsAction;
import org.caleydo.view.domino.internal.serial.SerializedDominoView;
import org.eclipse.jface.action.IToolBarManager;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class DominoViewPart extends ARcpGLElementViewPart {

	/**
	 * Constructor.
	 */
	public DominoViewPart() {
		super(SerializedDominoView.class);
	}

	@Override
	protected AGLElementView createView(IGLCanvas canvas) {
		return new DominoView(canvas);
	}

	@Override
	public DominoView getView() {
		return (DominoView) super.getView();
	}

	@Override
	protected void addToolBarContent(IToolBarManager toolBarManager) {
		toolBarManager.add(new SettingsAction(view));
		super.addToolBarContent(toolBarManager);
	}
}
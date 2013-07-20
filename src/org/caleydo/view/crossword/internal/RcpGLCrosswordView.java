/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal;

import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.view.crossword.internal.serial.SerializedCrosswordView;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Samuel Gratzl
 * 
 */
public class RcpGLCrosswordView extends ARcpGLViewPart {

	/**
	 * Constructor.
	 */
	public RcpGLCrosswordView() {
		super(SerializedCrosswordView.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		view = new GLCrosswordView(glCanvas);
		initializeView();
		createPartControlGL();
	}

	@Override
	public void createDefaultSerializedView() {
		serializedView = new SerializedCrosswordView();
		determineDataConfiguration(serializedView);
	}

	@Override
	public String getViewGUIID() {
		return GLCrosswordView.VIEW_TYPE;
	}

}
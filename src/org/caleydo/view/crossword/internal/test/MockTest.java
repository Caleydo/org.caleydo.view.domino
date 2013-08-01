/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.test;

import java.awt.Dimension;
import java.util.Random;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.datadomain.mock.MockDataDomain;
import org.caleydo.view.crossword.internal.ui.CrosswordElement;
import org.caleydo.view.crossword.internal.ui.CrosswordMultiElement;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Samuel Gratzl
 *
 */
public class MockTest extends GLSandBox {

	public MockTest(Shell parentShell) {
		super(parentShell, "Crossword", new GLElementDecorator(), GLPadding.ZERO, new Dimension(800, 600));

		((GLElementDecorator) getRoot()).setContent(createScene());
	}


	/**
	 * @return
	 */
	private GLElement createScene() {
		CrosswordMultiElement m = new CrosswordMultiElement();
		Random r = new Random();
		MockDataDomain numerical = MockDataDomain.createNumerical(100, 100, r);
		m.add(new CrosswordElement(numerical.getDefaultTablePerspective()));
		return m;
	}

	public static void main(String[] args) {
		GLSandBox.main(args, MockTest.class);
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public interface INodeUI {
	GLElement asGLElement();

	boolean setData(EDimension dim, TypedList data);

	/**
	 * @return
	 */
	INode asNode();

	/**
	 * @param dimension
	 * @return
	 */
	double getSize(EDimension dim);

	GLElement getToolBar();
}

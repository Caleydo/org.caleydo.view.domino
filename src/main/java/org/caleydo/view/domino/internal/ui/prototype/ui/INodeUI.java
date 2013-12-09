/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.domino.api.model.typed.TypedList;

/**
 * @author Samuel Gratzl
 *
 */
public interface INodeUI {
	GLElement asGLElement();

	void setData(EDimension dim, TypedList data);
}

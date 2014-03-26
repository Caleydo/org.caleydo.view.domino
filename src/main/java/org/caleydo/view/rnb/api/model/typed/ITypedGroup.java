/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.util.gleem.IColored;

/**
 * a {@link ITypedCollection} with name and color
 *
 * @author Samuel Gratzl
 *
 */
public interface ITypedGroup extends ILabeled, ITypedCollection, IColored {
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public interface INodeCreator extends Function<EDnDType, INode>, ILabeled {

}

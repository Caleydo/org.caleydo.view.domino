/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.Collection;

/**
 * an inhomogenous typed id collection, which is internally based on a single id type
 * 
 * @author Samuel Gratzl
 * 
 */
public interface ISingleTypedIDCollection extends IHasIDType, Collection<TypedID> {
	ITypedCollection getData();
}


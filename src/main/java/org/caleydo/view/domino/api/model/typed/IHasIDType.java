/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import org.caleydo.core.id.IDType;

/**
 * an item which has an {@link IDType}
 * 
 * @author Samuel Gratzl
 * 
 */
public interface IHasIDType {
	IDType getIdType();
}

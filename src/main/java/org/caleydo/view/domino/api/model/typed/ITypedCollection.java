/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.Collection;

import org.caleydo.core.id.IDType;

/**
 * a {@link ITypedCollection} is a collection of integer combined with their {@link IDType}
 * 
 * @author Samuel Gratzl
 * 
 */
public interface ITypedCollection extends Collection<Integer>, IHasIDType {

	TypedList asList();

	TypedSet asSet();
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import java.util.Collection;

import org.caleydo.core.id.IDType;

/**
 * a multi typed collection is a collection, which multiple types per id exists stored in a set of arrays
 * 
 * @author Samuel Gratzl
 * 
 */
public interface IMultiTypedCollection extends Collection<int[]> {

	IDType[] getIDTypes();

	MultiTypedList asList();

	Collection<TypedID> asInhomogenous();
}

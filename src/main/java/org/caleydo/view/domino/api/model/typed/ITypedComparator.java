/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.Comparator;

import org.caleydo.core.id.IDType;

/**
 * a special {@link Comparator} combined with an {@link IDType}
 * 
 * @author Samuel Gratzl
 * 
 */
public interface ITypedComparator extends Comparator<Integer>, IHasIDType {
}

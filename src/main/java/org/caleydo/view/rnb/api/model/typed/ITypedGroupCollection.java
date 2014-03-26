/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import java.util.List;

import org.caleydo.core.id.IDType;

/**
 * a {@link ITypedGroupCollection} is a collection of integer combined with their {@link IDType}
 *
 * @author Samuel Gratzl
 *
 */
public interface ITypedGroupCollection extends ITypedCollection {

	@Override
	TypedGroupList asList();

	@Override
	TypedGroupSet asSet();

	List<? extends ITypedGroup> getGroups();
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.util.Utils;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationDataValue implements IDataValues {
	private final String label;
	private final EDimension main;

	private final TypedGroupSet singleGroup;
	private final TypedGroupSet groups;


	public StratificationDataValue(Perspective data, EDimension dim, Integer referenceId) {
		this.main = dim;
		this.label = data.getLabel();
		this.singleGroup = new TypedGroupSet(TypedGroupList.createUngroupedGroup(new TypedSet(Collections
				.singleton(TypedCollections.INVALID_ID), TypedCollections.INVALID_IDTYPE)));
		this.groups = new TypedGroupSet(Utils.extractSetGroups(data, referenceId, dim));
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? groups : singleGroup;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		return a - b;
	}

}

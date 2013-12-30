/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class A1DDataDomainValues extends ADataDomainDataValues {
	protected final EDimension main;

	protected final TypedID id;
	protected final TypedGroupSet singleGroup;

	/**
	 * @param t
	 */
	public A1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data);
		Perspective o = main.opposite().select(data.getDimensionPerspective(), data.getRecordPerspective());
		assert o.getVirtualArray().size() == 1;
		this.id = new TypedID(o.getVirtualArray().get(0), o.getIdType());
		this.main = main;

		this.singleGroup = new TypedGroupSet(TypedGroupList.createUngroupedGroup(new TypedSet(Collections
				.singleton(id.getId()), TypedCollections.INVALID_IDTYPE)));
	}

	protected abstract TypedGroupSet getGroups();

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? getGroups() : singleGroup;
	}

	public float getNormalized(int id) {
		return getNormalized(main.select(id, this.id.getId()), main.select(this.id.getId(), id));
	}

}
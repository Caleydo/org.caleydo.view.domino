/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposedDataValues implements IDataValues {
	private final IDataValues wrappee;

	private TransposedDataValues(IDataValues wrappee) {
		this.wrappee = wrappee;
	}

	public static IDataValues transpose(IDataValues v) {
		if (v instanceof TransposedDataValues)
			return ((TransposedDataValues) v).wrappee;
		return new TransposedDataValues(v);
	}

	@Override
	public String getLabel() {
		return wrappee.getLabel();
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return wrappee.getDefaultGroups(dim.opposite());
	}

	@Override
	public int compare(EDimension dim, int a, int b, ITypedCollection otherData) {
		return wrappee.compare(dim.opposite(), a, b, otherData);
	}

	@Override
	public String getExtensionID() {
		return wrappee.getExtensionID();
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor, boolean mediumTranspose) {
		wrappee.fill(b, dimData, recData, existNeigbhor, mediumTranspose);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return wrappee.getDefaultVisualization();
	}

	@Override
	public Color getColor() {
		return wrappee.getColor();
	}

	@Override
	public boolean apply(String input) {
		return wrappee.apply(input);
	}

	@Override
	public void onSelectionChanged(boolean selected) {
		wrappee.onSelectionChanged(selected);
	}
}

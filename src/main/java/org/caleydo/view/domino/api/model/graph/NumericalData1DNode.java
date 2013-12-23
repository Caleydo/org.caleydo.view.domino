/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;
import org.caleydo.view.domino.internal.util.BitSetSet;
import org.caleydo.view.domino.internal.util.Utils;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class NumericalData1DNode extends AData1DNode implements IStratisfyingableNode {
	private final List<? extends ITypedGroup> groups;

	/**
	 * @param data
	 */
	public NumericalData1DNode(TablePerspective data, EDimension main) {
		super(data, main);
		assert DataSupportDefinitions.dataClass(EDataClass.REAL_NUMBER, EDataClass.NATURAL_NUMBER).apply(data);
		this.groups = extractGroups();
	}

	/**
	 * @param data
	 * @return
	 */
	private List<? extends ITypedGroup> extractGroups() {
		Set<Integer> invalid = new BitSetSet();
		final TypedSet d = getData(getDimension());
		for (Integer id : d) {
			float v = getNormalized(id);
			if (Float.isInfinite(v) || Float.isNaN(v))
				invalid.add(id);
		}
		if (invalid.isEmpty())
			return Collections.singletonList(TypedGroupList.createUngroupedGroup(d));

		TypedSetGroup normal = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(Sets.difference(d, invalid)),
				d.getIdType()), "Normal", data.getDataDomain().getColor());
		TypedSetGroup invalidG = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(invalid), d.getIdType()), "NaN",
				Color.NOT_A_NUMBER_COLOR);
		return Arrays.asList(normal,invalidG);
	}

	public NumericalData1DNode(NumericalData1DNode parent, String label, TypedSet ids) {
		super(parent, label, ids);
		this.groups = Utils.subGroups(ids, parent.groups);
	}

	public NumericalData1DNode(NumericalData1DNode clone) {
		super(clone);
		this.groups = clone.groups;
	}

	@Override
	public NumericalData1DNode clone() {
		return new NumericalData1DNode(this);
	}

	@Override
	public INode extract(String label, ITypedCollection dim, ITypedCollection rec) {
		return new NumericalData1DNode(this, label, (isRightDimension(EDimension.DIMENSION) ? dim : rec).asSet());
	}

	@Override
	public List<? extends ITypedGroup> getGroups(EDimension dim) {
		return isRightDimension(dim) ? groups : Collections.<ITypedGroup> emptyList();
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	private static class UI extends ANodeUI<NumericalData1DNode> {

		public UI(NumericalData1DNode node) {
			super(node);
		}

		@Override
		protected String getExtensionID() {
			return "1d.numerical";
		}

		@Override
		protected void fill(Builder b, TypedList dim, TypedList rec) {
			b.put(EDimension.class, node.getDimension());
			final TypedList data = node.getDimension().select(dim, rec);
			TablePerspective t = node.asTablePerspective(data);
			b.withData(t);
		}
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		float r1 = getNormalized(o1);
		float r2 = getNormalized(o2);
		return NumberUtils.compare(r1, r2);
	}
}

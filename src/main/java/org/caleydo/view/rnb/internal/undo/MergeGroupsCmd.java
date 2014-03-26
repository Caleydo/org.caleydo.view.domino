/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.rnb.api.model.typed.TypedGroupList;
import org.caleydo.view.rnb.api.model.typed.TypedGroupSet;
import org.caleydo.view.rnb.api.model.typed.TypedListGroup;
import org.caleydo.view.rnb.api.model.typed.TypedSet;
import org.caleydo.view.rnb.api.model.typed.TypedSetGroup;
import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.NodeGroup;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class MergeGroupsCmd implements ICmd {
	private final Node node;
	private final Collection<NodeGroup> groups;

	public MergeGroupsCmd(Node node, Collection<NodeGroup> groups) {
		this.node = node;
		this.groups = groups;
	}

	@Override
	public String getLabel() {
		return "Merge Groups";
	}

	@Override
	public ICmd run(RnB domino) {
		EDimension dim = node.getSingleGroupingDimension();
		TypedGroupSet bak = node.getUnderlyingData(dim);
		List<TypedListGroup> d = new ArrayList<>(node.getData(dim).getGroups());
		List<TypedSetGroup> r = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		for (NodeGroup g : new ArrayList<>(groups)) {
			final TypedListGroup gd = g.getData(dim);
			r.add(gd.asSet());
			int index = d.indexOf(gd);
			indices.add(index);
			d.remove(index);
			g.prepareRemoveal();
		}
		TypedSetGroup mg = new TypedSetGroup(TypedSet.union(r), StringUtils.join(
				Collections2.transform(r, Labels.TO_LABEL), ", "), mixColors(r));
		d.add(mg.asList());
		TypedGroupList l = new TypedGroupList(d);
		node.setUnderlyingData(dim, l.asSet());

		return new UndoMergeGroupsCmd(dim, bak);
	}

	/**
	 * @param r
	 * @return
	 */
	private static Color mixColors(List<TypedSetGroup> data) {
		float r = 0;
		float g = 0;
		float b = 0;
		float a = 0;
		for (TypedSetGroup group : data) {
			Color c = group.getColor();
			r += c.r;
			g += c.g;
			b += c.b;
			a += c.a;
		}
		float f = 1.f / data.size();
		return new Color(r * f, g * f, b * f, a * f);
	}

	private class UndoMergeGroupsCmd implements ICmd {
		private final EDimension dim;
		private final TypedGroupSet ori;

		public UndoMergeGroupsCmd(EDimension dim, TypedGroupSet bak) {
			this.dim = dim;
			this.ori = bak;
		}

		@Override
		public ICmd run(RnB domino) {
			node.setUnderlyingData(dim, ori);
			return MergeGroupsCmd.this;
		}

		@Override
		public String getLabel() {
			return "Undo Merge Groups";
		}

	}

}

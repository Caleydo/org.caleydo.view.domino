/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.view.domino.api.model.typed.MappingCaches;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeOperations {
	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static TypedGroupSet intersect(TypedGroupList a, TypedGroupList b) {
		IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(b.getIdType(), a.getIdType());
		if (mapper == null)
			return TypedGroupSet.createUngrouped(TypedCollections.empty(a.getIdType()));
		Set<Integer> others = mapper.apply(b);

		List<TypedSetGroup> groups = new ArrayList<>();
		for (TypedSetGroup g : a.asSet().getGroups()) {
			Set<Integer> ids = ImmutableSet.copyOf(Sets.intersection(g, others));
			if (ids.isEmpty())
				continue;
			groups.add(new TypedSetGroup(ids, a.getIdType(), g.getLabel(), g.getColor()));
		}
		return asSet(groups, a.getIdType());
	}

	/**
	 * @param groups
	 * @return
	 */
	private static TypedGroupSet asSet(List<TypedSetGroup> groups, IDType idType) {
		if (groups.isEmpty())
			return TypedGroupSet.createUngrouped(TypedCollections.empty(idType));
		return new TypedGroupSet(groups);
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static TypedGroupSet union(TypedGroupList a, TypedGroupList b) {
		TypedGroupSet base = a.asSet();
		IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(b.getIdType(), a.getIdType());
		if (mapper == null)
			return base;

		Set<Integer> acc = new BitSetSet();
		acc.addAll(base);
		List<TypedSetGroup> groups = new ArrayList<>(base.getGroups());

		for (TypedListGroup g : b.getGroups()) {
			Set<Integer> ids = mapper.apply(g);
			ids = ImmutableSet.copyOf(Sets.difference(ids, acc));
			if (ids.isEmpty())
				continue;
			acc.addAll(ids);
			groups.add(new TypedSetGroup(ids, a.getIdType(), g.getLabel(),g.getColor()));
		}
		return asSet(groups, a.getIdType());
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static TypedGroupSet difference(TypedGroupList a, TypedGroupList b) {
		TypedGroupSet base = a.asSet();
		IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(b.getIdType(), a.getIdType());
		if (mapper == null)
			return base;

		Set<Integer> others = mapper.apply(b);
		List<TypedSetGroup> groups = new ArrayList<>(base.getGroups());

		for (TypedSetGroup g : base.getGroups()) {
			Set<Integer> ids = ImmutableSet.copyOf(Sets.difference(g, others));
			if (ids.isEmpty())
				continue;
			groups.add(new TypedSetGroup(ids, a.getIdType(), g.getLabel(), g.getColor()));
		}
		return asSet(groups, a.getIdType());
	}
}

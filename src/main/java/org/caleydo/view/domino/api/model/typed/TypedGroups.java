/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;
import org.caleydo.view.domino.api.model.typed.util.RepeatingList;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public final class TypedGroups {
	private static final String UNMAPPED = "Unmapped";
	private static final String UNGROUPED = "Ungrouped";
	private static final Color COLOr = Color.NEUTRAL_GREY;

	private TypedGroups() {

	}

	public static TypedListGroup createUnmappedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(RepeatingList.repeat(TypedCollections.INVALID_ID, size), idType),
				UNMAPPED, COLOr);
	}

	public static TypedListGroup createUngroupedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(RepeatingList.repeat(TypedCollections.INVALID_ID, size), idType),
				UNGROUPED, COLOr);
	}

	public static TypedListGroup createUngroupedGroup(TypedList list) {
		return new TypedListGroup(list, UNGROUPED, COLOr);
	}

	public static TypedSetGroup createUngroupedGroup(TypedSet set) {
		return new TypedSetGroup(set, UNGROUPED, COLOr);
	}

	public static boolean isUngrouped(ITypedGroup group) {
		return group != null && group.getLabel() == UNGROUPED;
	}

	public static boolean isUnmapped(ITypedGroup group) {
		return group != null && group.getLabel() == UNMAPPED;
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static TypedGroupSet intersect(ITypedGroupCollection a, ITypedGroupCollection b) {
		IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(b.getIdType(), a.getIdType());
		if (mapper == null)
			return TypedGroupSet.createUngrouped(TypedCollections.empty(a.getIdType()));
		Set<Integer> others = mapper.apply(b);

		List<TypedSetGroup> groups = new ArrayList<>();
		for (ITypedGroup g : a.getGroups()) {
			Set<Integer> ids = ImmutableSet.copyOf(Sets.intersection(g.asSet(), others));
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
	public static TypedGroupSet union(ITypedGroupCollection a, ITypedGroupCollection b) {
		if (a.isEmpty())
			return b.asSet();
		if (b.isEmpty())
			return a.asSet();
		if (a.getIdType() == b.getIdType()) {
			TypedGroupSet base = a.asSet();
			Set<Integer> acc = new BitSetSet();
			acc.addAll(base);
			List<TypedSetGroup> groups = new ArrayList<>(base.getGroups());

			for (ITypedGroup g : b.getGroups()) {
				Set<Integer> ids = g.asSet();
				ids = ImmutableSet.copyOf(Sets.difference(ids, acc));
				if (ids.isEmpty())
					continue;
				acc.addAll(ids);
				groups.add(new TypedSetGroup(ids, a.getIdType(), g.getLabel(), g.getColor()));
			}
			return asSet(groups, a.getIdType());
		} else {
			// map both to primary to ensure both can be represented
			final IDType target = a.getIdType().getIDCategory().getPrimaryMappingType();

			Set<Integer> acc = new BitSetSet();
			List<TypedSetGroup> groups = new ArrayList<>();

			IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(a.getIdType(), target);
			for (ITypedGroup g : a.getGroups()) {
				Set<Integer> ids = mapper.apply(g);
				ids = acc.isEmpty() ? ids : ImmutableSet.copyOf(Sets.difference(ids, acc));
				if (ids.isEmpty())
					continue;
				acc.addAll(ids);
				groups.add(new TypedSetGroup(ids, target, g.getLabel(), g.getColor()));
			}
			mapper = MappingCaches.findMapper(b.getIdType(), target);
			for (ITypedGroup g : b.getGroups()) {
				Set<Integer> ids = mapper.apply(g);
				ids = acc.isEmpty() ? ids : ImmutableSet.copyOf(Sets.difference(ids, acc));
				if (ids.isEmpty())
					continue;
				acc.addAll(ids);
				groups.add(new TypedSetGroup(ids, target, g.getLabel(), g.getColor()));
			}
			return asSet(groups, target);
		}
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static TypedGroupSet difference(ITypedGroupCollection a, TypedGroupList b) {
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

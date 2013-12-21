/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupList extends TypedList implements ITypedCollection {

	private final List<TypedListGroup> groups;

	public TypedGroupList(List<TypedListGroup> groups) {
		super(new ConcatedList(groups), groups.get(0).getIdType());
		this.groups = groups;
	}

	private TypedGroupList(TypedList list, List<? extends ITypedGroup> groups) {
		super(list, list.getIdType());
		this.groups = toGroups(list, groups);
	}

	/**
	 * @param list
	 * @param groups2
	 * @return
	 */
	private static List<TypedListGroup> toGroups(TypedList list, List<? extends ITypedGroup> groups) {
		List<TypedListGroup> r = new ArrayList<>(groups.size());
		int i = 0;
		for(ITypedGroup g : groups) {
			r.add(new TypedListGroup(list.subList(i, i + g.size()), g.getLabel(), g.getColor()));
			i+=g.size();
		}
		return ImmutableList.copyOf(r);
	}

	public static TypedGroupList createUngrouped(TypedList list) {
		return create(list, Collections.singletonList(new TypedListGroup(list, "Ungrouped", Color.NEUTRAL_GREY)));
	}

	public static TypedListGroup createUngrouped(IDType idType, int size) {
		return new TypedListGroup(new TypedList(new RepeatingList<>(TypedCollections.INVALID_ID, size), idType),
				"Ungrouped", Color.NEUTRAL_GREY);
	}

	public static TypedGroupList create(TypedList list, List<? extends ITypedGroup> groups) {
		return new TypedGroupList(list, groups);
	}

	/**
	 * @return the groups, see {@link #groups}
	 */
	public List<TypedListGroup> getGroups() {
		return groups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedGroupList other = (TypedGroupList) obj;
		return Objects.equals(groups, other.groups);
	}

	/**
	 * @author Samuel Gratzl
	 *
	 */
	private static final class ConcatedList extends AbstractList<Integer> {
		private final int[] ends;
		private final List<TypedListGroup> groups;

		public ConcatedList(List<TypedListGroup> groups) {
			ends = new int[groups.size()];
			this.groups = groups;
			int c = 0;
			for (int i = 0; i < ends.length; ++i) {
				final TypedListGroup group = groups.get(i);
				c += group.size();
				ends[i] = c;
			}
		}

		@Override
		public Iterator<Integer> iterator() {
			return Iterables.concat(groups).iterator();
		}

		@Override
		public Integer get(int index) {
			for(int i = 0; i < ends.length; ++i) {
				if (index < ends[i]) {
					final TypedList l = groups.get(i);
					return l.get(index - ends[i] - l.size());
				}
			}
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int size() {
			return ends[ends.length - 1];
		}
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.GroupInfo;

import com.google.common.collect.ImmutableSet;

/**
 * a scorerow for the default table perspective
 *
 * @author Samuel Gratzl
 *
 */
public class LabelScoreRow extends AScoreRow {
	private final IDCategory category;
	private final Set<Integer> ids;
	private final Group group;

	public LabelScoreRow(IDCategory category) {
		this.category = category;
		@SuppressWarnings("unchecked")
		Set<Integer> ids = (Set<Integer>) IDMappingManagerRegistry.get().getIDMappingManager(category)
				.getAllMappedIDs(category.getPrimaryMappingType());
		this.ids = ImmutableSet.copyOf(ids);
		this.group = new Group(ids.size(), 0);
		this.group.setLabel(category.getCategoryName(), false);
		this.group.setStartIndex(0);
		this.group.setGroupIndex(0);
	}

	/**
	 * @return the category, see {@link #category}
	 */
	public IDCategory getCategory() {
		return category;
	}

	@Override
	public String getLabel() {
		return category.getCategoryName() + " Labels";
	}

	@Override
	public IDataDomain getDataDomain() {
		return null;
	}

	@Override
	public String getPersistentID() {
		return category.getCategoryName();
	}

	@Override
	public IDType getIdType() {
		return category.getHumanReadableIDType();
	}

	@Override
	public IDType getDimensionIdType() {
		return TypedCollections.INVALID_IDTYPE;
	}

	@Override
	public Collection<Group> getGroups() {
		return Collections.singleton(group);
	}

	@Override
	public int getGroupSize() {
		return 1;
	}

	@Override
	public Collection<Integer> of(Group group) {
		return ids;
	}

	@Override
	public int size() {
		return ids.size();
	}

	@Override
	public Iterable<Integer> getDimensionIDs() {
		return Collections.emptySet();
	}

	@Override
	public Iterator<Integer> iterator() {
		return ids.iterator();
	}

	@Override
	public boolean is(TablePerspective tablePerspective) {
		return false;
	}

	@Override
	public boolean is(Perspective p) {
		return false;
	}

	@Override
	public Collection<GroupInfo> getGroupInfos() {
		return Collections.singleton(new GroupInfo(group.getLabel(), group.getSize(), null));
	}

}

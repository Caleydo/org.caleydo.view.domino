/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

import com.google.common.collect.Iterables;

/**
 * layout implementation
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordMultiElement extends GLElementContainer implements IGLLayout, IHasMinSize {

	private boolean alwaysShowHeader;

	/**
	 *
	 */
	public CrosswordMultiElement() {
		setLayout(this);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		float acc = 10;
		float x_shift = 0;
		float y_shift = 0;

		for(IGLLayoutElement elem : children) {
			CrosswordLayoutInfo helper = elem.getLayoutDataAs(CrosswordLayoutInfo.class, null);
			assert helper != null;
			Vec2f loc = helper.getLocation(elem);
			loc.setX(Float.isNaN(loc.x()) ? acc : loc.x());
			loc.setY(Float.isNaN(loc.y()) ? acc : loc.y());
			Vec2f msize = helper.getMinSize(elem);
			helper.scale(msize);
			helper.setBounds(elem, loc,msize);
			// new loc
			loc = elem.getLocation();
			x_shift = Math.min(loc.x(), x_shift);
			y_shift = Math.min(loc.y(), y_shift);

			// FIXME
			acc += msize.x() + 10;
		}

		if (x_shift < 0 || y_shift < 0) {
			// shift all
			for (IGLLayoutElement elem : children) {
				Vec2f location = elem.getLocation();
				elem.setLocation(location.x() - x_shift, location.y() - y_shift);
			}
		}
		relayoutParent(); // trigger update of the parent
	}

	@Override
	public Vec2f getMinSize() {
		float x = 0;
		float y = 0;
		for (GLElement child : this) {
			Vec4f bounds = child.getBounds();
			x = Math.max(x, bounds.x() + bounds.z());
			y = Math.max(y, bounds.y() + bounds.w());
		}
		return new Vec2f(x, y);
	}

	/**
	 * @return the alwaysShowHeader, see {@link #alwaysShowHeader}
	 */
	public boolean isAlwaysShowHeader() {
		return alwaysShowHeader;
	}

	/**
	 *
	 */
	public void toggleAlwaysShowHeader() {
		this.alwaysShowHeader = !this.alwaysShowHeader;
		for (CrosswordElement elem : Iterables.filter(this, CrosswordElement.class))
			elem.relayout();
	}

	/**
	 * @param dimensionSubTablePerspectives
	 */
	public void add(TablePerspective tablePerspective) {
		this.add(new CrosswordElement(tablePerspective));
	}

	public void addAll(Iterable<TablePerspective> tablePerspectives) {
		for (TablePerspective tablePerspective : tablePerspectives)
			add(tablePerspective);
	}

	/**
	 * @param removed
	 */
	public void removeAll(Collection<TablePerspective> removed) {
		if (removed.isEmpty())
			return;
		List<CrosswordElement> toRemove = new ArrayList<>();
		for (CrosswordElement elem : Iterables.filter(this, CrosswordElement.class)) {
			if (removed.contains(elem.getTablePerspective()))
				toRemove.add(elem);
		}
		for (CrosswordElement r : toRemove)
			remove(r);
	}
}

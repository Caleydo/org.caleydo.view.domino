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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IGLElementParent;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * layout implementation
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordMultiElement extends GLElement implements IHasMinSize, IGLElementParent,
		Iterable<CrosswordElement> {

	private UndirectedGraph<CrosswordElement, BandEdge> graph = new Pseudograph<>(BandEdge.class);
	private boolean alwaysShowHeader;

	/**
	 *
	 */
	public CrosswordMultiElement() {

	}

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
		add(new CrosswordElement(tablePerspective));
	}

	@Override
	public void layout(int deltaTimeMs) {
		super.layout(deltaTimeMs);
		for (GLElement child : this)
			child.layout(deltaTimeMs);
	}

	@Override
	protected final void layoutImpl() {
		super.layoutImpl();
		List<IGLLayoutElement> l = asLayoutElements();
		Vec2f size = getSize();
		doLayout(l, size.x(), size.y());
	}

	private List<IGLLayoutElement> asLayoutElements() {
		Set<CrosswordElement> s = graph.vertexSet();
		List<IGLLayoutElement> l = new ArrayList<>(s.size());
		for (CrosswordElement child : s)
			if (child.getVisibility() != EVisibility.NONE)
				l.add(GLElementAccessor.asLayoutElement(child));
		return Collections.unmodifiableList(l);
	}

	@Override
	protected void takeDown() {
		for (GLElement elem : this)
			GLElementAccessor.takeDown(elem);
		super.takeDown();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		for (GLElement child : this)
			GLElementAccessor.init(child, context);
	}

	private void setup(CrosswordElement child) {
		IGLElementParent ex = child.getParent();
		boolean doInit = ex == null;
		if (ex == this) {
			// internal move
			graph.removeVertex(child);
		} else if (ex != null) {
			doInit = ex.moved(child);
		}
		GLElementAccessor.setParent(child, this);
		if (doInit && context != null)
			GLElementAccessor.init(child, context);
	}

	/**
	 * @param crosswordElement
	 */
	private void add(CrosswordElement child) {
		setup(child);
		graph.addVertex(child);

	}

	@Override
	public boolean moved(GLElement child) {
		if (child instanceof CrosswordElement)
			graph.removeVertex((CrosswordElement) child);
		relayout();
		return context != null;
	}

	@Override
	public Iterator<CrosswordElement> iterator() {
		return Iterators.unmodifiableIterator(graph.vertexSet().iterator());
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		g.incZ();
		for (GLElement child : graph.vertexSet())
			child.render(g);
		g.decZ();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
		g.incZ();
		for (GLElement child : graph.vertexSet())
			child.renderPick(g);
		g.decZ();
	}

	@Override
	protected boolean hasPickAbles() {
		return super.hasPickAbles() || !graph.vertexSet().isEmpty();
	}
	/**
	 * @param r
	 */
	void remove(CrosswordElement child) {
		if (graph.removeVertex(child)) {
			GLElementAccessor.takeDown(child);
			relayout();
		}
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


	private static class BandEdge extends DefaultEdge {

	}
}

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
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.BitField;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IGLElementParent;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.crossword.internal.ui.band.ABandEdge;
import org.caleydo.view.crossword.internal.ui.band.ParentChildBandEdge;
import org.caleydo.view.crossword.internal.ui.band.SharedBandEdge;
import org.caleydo.view.crossword.internal.ui.band.SisterBandEdge;
import org.jgrapht.UndirectedGraph;
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

	private final UndirectedGraph<CrosswordElement, ABandEdge> graph = new Pseudograph<>(ABandEdge.class);
	private final CrosswordBandLayer bands = new CrosswordBandLayer();
	private boolean alwaysShowHeader;

	/**
	 *
	 */
	public CrosswordMultiElement() {
		GLElementAccessor.setParent(bands, this);
	}

	public void doLayout(List<? extends IGLLayoutElement> children) {
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
		bands.layout(deltaTimeMs);
		for (GLElement child : this)
			child.layout(deltaTimeMs);
	}

	@Override
	public void relayout() {
		super.relayout();
		GLElementAccessor.relayoutDown(bands);
	}

	@Override
	protected final void layoutImpl() {
		super.layoutImpl();
		Vec2f size = getSize();
		GLElementAccessor.asLayoutElement(bands).setBounds(0, 0, size.x(), size.y());
		List<IGLLayoutElement> l = asLayoutElements();
		doLayout(l);
		for (ABandEdge edge : graph.edgeSet()) {
			edge.relayout();
		}
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
		GLElementAccessor.takeDown(bands);
		for (GLElement elem : this)
			GLElementAccessor.takeDown(elem);
		super.takeDown();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		GLElementAccessor.init(bands, context);
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

	private static final int FLAG_PARENT = 1;
	private static final int FLAG_SISTER = 2;

	/**
	 * @param crosswordElement
	 */
	private void add(CrosswordElement child) {
		setup(child);
		graph.addVertex(child);

		final TablePerspective tablePerspective = child.getTablePerspective();
		final IDType recIDType = tablePerspective.getRecordPerspective().getIdType();
		final IDType dimIDType = tablePerspective.getDimensionPerspective().getIdType();
		final TablePerspective parent = tablePerspective.getParentTablePerspective();

		for (CrosswordElement other : graph.vertexSet()) {
			if (other == child)
				continue;
			final TablePerspective tablePerspective2 = child.getTablePerspective();
			final IDType recIDType2 = tablePerspective2.getRecordPerspective().getIdType();
			final IDType dimIDType2 = tablePerspective2.getDimensionPerspective().getIdType();
			int flags = 0;
			boolean isParent = tablePerspective2.equals(parent);
			boolean isSister = Objects.equals(tablePerspective2.getParentTablePerspective(), parent) && parent != null;
			flags |= (isParent ? FLAG_PARENT : 0);
			flags |= (isSister ? FLAG_SISTER : 0);
			if (recIDType.resolvesTo(recIDType2))
				graph.addEdge(child, other, createEdge(true, true, flags));
			else if (dimIDType.resolvesTo(recIDType2))
				graph.addEdge(child, other, createEdge(false, true, flags));
			if (recIDType.resolvesTo(dimIDType2))
				graph.addEdge(child, other, createEdge(true, false, flags));
			else if (dimIDType.resolvesTo(dimIDType2))
				graph.addEdge(child, other, createEdge(false, false, flags));
		}
		relayout();
	}

	private static ABandEdge createEdge(boolean hor1, boolean hor2, int flags) {
		BitField mask = new BitField(flags);
		if (mask.isSet(FLAG_PARENT))
			return new ParentChildBandEdge(hor1, hor2);
		if (mask.isSet(FLAG_SISTER))
			return new SisterBandEdge(hor1, hor2);
		return new SharedBandEdge(hor1, hor2);
	}

	/**
	 * @param crosswordElement
	 * @param dimensionSubTablePerspectives
	 */
	public void split(CrosswordElement child, List<TablePerspective> subElements) {
		for (TablePerspective t : subElements)
			add(t);
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
		bands.render(g);
		g.incZ();
		for (GLElement child : graph.vertexSet())
			child.render(g);
		g.decZ();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
		bands.renderPick(g);
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

	public Iterable<ABandEdge> getBands() {
		return Iterables.unmodifiableIterable(graph.edgeSet());
	}
	/**
	 * @param crosswordElement
	 */
	public void onConnectionsChanged(CrosswordElement child) {
		for (ABandEdge edge : graph.edgesOf(child))
			edge.update();
	}

}

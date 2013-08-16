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
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IGLElementParent;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.crossword.internal.model.PerspectiveMetaData;
import org.caleydo.view.crossword.internal.model.TablePerspectiveMetaData;
import org.caleydo.view.crossword.internal.ui.band.ABandEdge;
import org.caleydo.view.crossword.internal.ui.band.ParentChildBandEdge;
import org.caleydo.view.crossword.internal.ui.band.SharedBandEdge;
import org.caleydo.view.crossword.internal.ui.band.SiblingBandEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;

import com.google.common.collect.ImmutableSet;
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

		for (IGLLayoutElement elem : children) {
			CrosswordLayoutInfo helper = elem.getLayoutDataAs(CrosswordLayoutInfo.class, null);
			assert helper != null;
			Vec2f loc = helper.getLocation(elem);
			loc.setX(Float.isNaN(loc.x()) ? acc : loc.x());
			loc.setY(Float.isNaN(loc.y()) ? acc : loc.y());
			Vec2f msize = helper.getMinSize(elem);
			helper.scale(msize);
			helper.setBounds(elem, loc, msize);
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
		relayoutParent(); // trigger update of the parent for min size changes
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
		add(new CrosswordElement(tablePerspective, new TablePerspectiveMetaData(0, 0)));
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

	/**
	 * @param crosswordElement
	 */
	private void add(CrosswordElement child) {
		addImpl(child);
		createBands(child, ImmutableSet.of(child));
	}

	private void addImpl(CrosswordElement child) {
		setup(child);
		graph.addVertex(child);
		relayout();
	}

	private void createBands(CrosswordElement child, Set<CrosswordElement> ignores) {
		final TablePerspective tablePerspective = child.getTablePerspective();
		final IDType recIDType = tablePerspective.getRecordPerspective().getIdType();
		final IDType dimIDType = tablePerspective.getDimensionPerspective().getIdType();

		for (CrosswordElement other : graph.vertexSet()) {
			if (ignores.contains(other))
				continue;
			final TablePerspective tablePerspective2 = child.getTablePerspective();
			final IDType recIDType2 = tablePerspective2.getRecordPerspective().getIdType();
			final IDType dimIDType2 = tablePerspective2.getDimensionPerspective().getIdType();
			if (recIDType.resolvesTo(recIDType2))
				addEdgeImpl(child, other, new SharedBandEdge(false, false));
			if (dimIDType.resolvesTo(recIDType2))
				addEdgeImpl(child, other, new SharedBandEdge(true, false));
			if (recIDType.resolvesTo(dimIDType2))
				addEdgeImpl(child, other, new SharedBandEdge(false, true));
			if (dimIDType.resolvesTo(dimIDType2))
				addEdgeImpl(child, other, new SharedBandEdge(true, true));
		}
	}

	private void addEdgeImpl(CrosswordElement child, CrosswordElement other, final ABandEdge edge) {
		graph.addEdge(child, other, edge);
		edge.update();
	}

	/**
	 * @param crosswordElement
	 * @param dimensionSubTablePerspectives
	 */
	private void split(CrosswordElement base, boolean inDim) {
		TablePerspective table = base.getTablePerspective();
		boolean hor = inDim;
		final GroupList groups = (inDim ? table.getDimensionPerspective() : table.getRecordPerspective())
				.getVirtualArray().getGroupList();
		assert groups.size() > 1;
		final List<TablePerspective> datas = inDim ? table.getDimensionSubTablePerspectives() : table
				.getRecordSubTablePerspectives();
		List<CrosswordElement> children = new ArrayList<>(datas.size());
		{
			TablePerspectiveMetaData metaData = new TablePerspectiveMetaData(
					inDim ? 0 : PerspectiveMetaData.FLAG_CHILD, inDim ? PerspectiveMetaData.FLAG_CHILD : 0);
			for (TablePerspective t : datas) {
				children.add(new CrosswordElement(t, metaData));
			}
		}

		// combine the elements that should be ignored
		ImmutableSet<CrosswordElement> ignore = ImmutableSet.<CrosswordElement> builder().addAll(children).add(base)
				.build();

		for (int i = 0; i < children.size(); ++i) {
			CrosswordElement child = children.get(i);
			Group group = groups.get(i);
			addImpl(child);
			createBands(child, ignore);
			addEdgeImpl(base, child, new ParentChildBandEdge(hor, group.getStartIndex(), hor)); // add parent edge
			for (int j = 0; j < i; ++j) {
				CrosswordElement child2 = children.get(j);
				addEdgeImpl(child, child2, new SiblingBandEdge(hor, hor)); // add sibling edge
			}
		}

		// update metadata flags
		TablePerspectiveMetaData metaData = base.getMetaData();
		(inDim ? metaData.getDimension() : metaData.getRecord()).setSplitted();
	}

	/**
	 * @param crosswordElement
	 */
	public void splitDim(CrosswordElement child) {
		split(child, true);
	}

	public void splitRec(CrosswordElement child) {
		split(child, false);
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

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.BandRoute;
import org.caleydo.view.domino.api.model.graph.BandEdge;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.Edges;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.NodeUIState;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.spi.model.IBandRenderer;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;
import org.caleydo.view.domino.spi.model.graph.IDominoGraphListener;
import org.caleydo.view.domino.spi.model.graph.IEdge;
import org.caleydo.view.domino.spi.model.graph.INode;
import org.caleydo.vis.lineup.ui.GLPropertyChangeListeners;

import com.google.common.collect.Iterables;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class DominoBandLayer extends DominoBackgroundLayer implements
		MultiSelectionManagerMixin.ISelectionMixinCallback,
		IBandHost, IPickingListener, IPickingLabelProvider, IDominoGraphListener {
	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);


	private final List<IBandRenderer> routes = new ArrayList<>();
	private final PropertyChangeListener relayout = GLPropertyChangeListeners.relayoutOnEvent(this);

	private PickingPool pickingPool;


	public DominoBandLayer(DominoGraph graph, DominoNodeLayer nodes) {
		super(nodes, graph);
		this.graph.addGraphListener(this);
	}

	@Override
	public void vertexAdded(INode vertex, Collection<IEdge> edges) {
		relayout();
		vertex.getUIState().addPropertyChangeListener(NodeUIState.PROP_SIZE_CHANGE, relayout);
	}

	@Override
	public void vertexRemoved(INode vertex, Collection<IEdge> edges) {
		relayout();
		vertex.getUIState().removePropertyChangeListener(NodeUIState.PROP_SIZE_CHANGE, relayout);
	}

	@Override
	public void vertexSortingChanged(ISortableNode vertex, EDimension dim, boolean stratisfy) {
		// check if any visible bands if so we have to update them
		relayout();
	}

	public void update() {
		routes.clear();

		for (BandEdge edge : Iterables.filter(graph.edgeSet(), BandEdge.class)) {
			INode s = edge.getSource();
			INode t = edge.getTarget();

			Rect sourceB = nodes.getBounds(graph.walkAlong(edge.getDimension(s), s, Edges.SAME_STRATIFICATION));
			Rect targetB = nodes.getBounds(graph.walkAlong(edge.getDimension(t), t, Edges.SAME_STRATIFICATION));

			if (sourceB.x() < targetB.x())
				createBand(edge, s, sourceB, t, targetB);
			else
				createBand(edge, t, targetB, s, sourceB);
		}
	}

	private void createBand(BandEdge edge, INode s, Rect sourceB, INode t, Rect targetB) {
		final EDimension sDim = edge.getDimension(s);
		final EDimension tDim = edge.getDimension(t);

		final EDimension dim = sDim; // FIXME cross stuff

		List<Vec2f> curve;
		if (dim == EDimension.RECORD) {
			curve = rot90(createCurve(rot90(sourceB), rot90(targetB), dim.opposite()));
		} else
			curve = createCurve(sourceB, targetB, dim);
		if (curve.isEmpty())
			return;

		Color color = new Color(180, 212, 231, 32);
		// if (curve.size() > 2)
		// curve = TesselatedPolygons.spline(curve, 5);

		TypedSet sData = s.getData(sDim.opposite());
		TypedSet tData = t.getData(tDim.opposite());
		MultiTypedSet shared = TypedSets.intersect(sData, tData);

		if (shared.isEmpty())
			return;

		final float sTotal = dim.opposite().select(sourceB.width(), sourceB.height());
		final float tTotal = dim.opposite().select(targetB.width(), targetB.height());
		routes.add(new BandRoute(new Route(curve), color, shared, sData, tData, sTotal * 0.5f, tTotal * 0.5f));
	}

	@Override
	protected void layoutImpl(int deltaTimeMs) {
		super.layoutImpl(deltaTimeMs);
		update();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		pickingPool = new PickingPool(context, PickingListenerComposite.concat(this, context.getSWTLayer()
				.createTooltip(this)));
	}

	@Override
	protected void takeDown() {
		pickingPool.clear();
		pickingPool = null;
		super.takeDown();
	}

	@Override
	public String getLabel(Pick pick) {
		IBandRenderer route = getRoute(pick.getObjectID());
		if (route == null)
			return "";
		return route.getLabel();
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			select(getRoute(pick.getObjectID()), SelectionType.SELECTION, !((IMouseEvent) pick).isCtrlDown());
			break;
		case MOUSE_OVER:
			select(getRoute(pick.getObjectID()), SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			clear(getRoute(pick.getObjectID()), SelectionType.MOUSE_OVER);
			break;
		default:
			break;
		}
	}

	/**
	 * @param route
	 * @param type
	 */
	private void clear(IBandRenderer route, SelectionType type) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = selections.getSelectionManager(route.getIds(st).getIdType());
			if (manager == null)
				return;
			manager.clearSelection(type);
			selections.fireSelectionDelta(manager);
		}
	}

	/**
	 * @param route
	 * @param selection
	 * @param clear
	 *            whether to clear before
	 */
	private void select(IBandRenderer route, SelectionType type, boolean clear) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = getOrCreate(route.getIdType(st));
			if (clear)
				manager.clearSelection(type);
			manager.addToType(type, route.getIds(st));
			selections.fireSelectionDelta(manager);
		}
	}

	/**
	 * @param objectID
	 * @return
	 */
	private IBandRenderer getRoute(int index) {
		if (index < 0 || index >= routes.size())
			return null;
		return routes.get(index);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		for (IBandRenderer edge : routes) {
			edge.render(g, w, h, this);
		}
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLUE);
		super.renderPickImpl(g, w, h);
		g.color(Color.BLACK);
		if (getVisibility() == EVisibility.PICKABLE) {
			g.incZ(0.05f);
			g.color(Color.RED);
			for (int i = 0; i < routes.size(); i++) {
				g.pushName(pickingPool.get(i));
				routes.get(i).renderPick(g, w, h, this);
				g.popName();
			}
			g.incZ(-0.05f);
			g.color(Color.BLACK);
		}
	}

	@Override
	public IGLElementContext getContext() {
		return context;
	}

	@Override
	public TypedSet getSelected(TypedSet ids, SelectionType type) {
		if (ids.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		return ids.intersect(new TypedSet(active, ids.getIdType()));
	}

	/**
	 * @param idType
	 * @return
	 */
	private SelectionManager getOrCreate(IDType idType) {
		SelectionManager manager = selections.getSelectionManager(idType);
		if (manager == null) {
			manager = new SelectionManager(idType);
			selections.add(manager);
		}
		return manager;
	}

	@Override
	protected boolean hasPickAbles() {
		return true; // routes have pickables
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaint();
	}



	/**
	 * @param sourceB
	 * @param targetB
	 * @param dim
	 * @return
	 */
	private List<Vec2f> createCurve(Rect s, Rect t, EDimension dim) {
		assert dim == EDimension.DIMENSION;
		if (Math.abs(s.x2() - t.x()) < 4)
			return Collections.emptyList();

		Vec2f sv = new Vec2f(s.x2(), s.y() + s.height() * 0.5f);
		Vec2f tv = new Vec2f(t.x(), t.y() + t.height() * 0.5f);
		if (sv.y() == tv.y())
			return Arrays.asList(sv, tv);

		Vec2f shift = new Vec2f(20, 0);
		Vec2f s2 = sv.plus(shift);
		Vec2f t2 = tv.minus(shift);

		return Arrays.asList(sv, s2, t2, tv);
	}

	/**
	 * @param curve
	 * @return
	 */
	private static List<Vec2f> rot90(List<Vec2f> curve) {
		List<Vec2f> r = new ArrayList<>(curve.size());
		for (Vec2f in : curve)
			r.add(new Vec2f(in.y(), in.x()));
		return r;
	}

	/**
	 * @param sourceB
	 * @return
	 */
	private static Rect rot90(Rect a) {
		return new Rect(a.y(), a.x(), a.height(), a.width());
	}
}

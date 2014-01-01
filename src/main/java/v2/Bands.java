/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;

import v2.band.Band;

import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.common.util.IntIntHashMap.Entry;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class Bands extends GLElement implements
		MultiSelectionManagerMixin.ISelectionMixinCallback,
 IBandHost,
		IPickingListener, IPickingLabelProvider {
	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);


	private final List<Band> routes = new ArrayList<>();

	private PickingPool pickingPool;
	private final IntIntHashMap pickingOffsets = new IntIntHashMap();

	public Bands() {
	}

	public void update() {
		routes.clear();
		Domino domino = findParent(Domino.class);
		List<Block> blocks = domino.getBlocks();
		final int length = blocks.size();
		for (int i = 0; i < length; ++i) {
			blocks.get(i).createBandsTo(blocks.subList(i + 1, length), routes);
		}
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
		Band route = getRoute(pick.getObjectID());
		if (route == null)
			return "";
		return route.getLabel();
	}

	@Override
	public void pick(Pick pick) {
		int[] split = split(pick.getObjectID());
		switch (pick.getPickingMode()) {
		case CLICKED:
			select(getRoute(split[0]), split[1], SelectionType.SELECTION, !((IMouseEvent) pick).isCtrlDown());
			break;
		case MOUSE_OVER:
			select(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			clear(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER);
			break;
		default:
			break;
		}
	}

	/**
	 * @param objectID
	 * @return
	 */
	private int[] split(int objectID) {
		Entry last = null;
		for (Entry entry : pickingOffsets) {
			if (entry.value > objectID)
				break;
			last = entry;
		}
		assert last != null;
		return new int[] { last.key, objectID - last.value };
	}

	/**
	 * @param route
	 * @param type
	 */
	private void clear(Band route, int subIndex, SelectionType type) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = selections.getSelectionManager(route.getIds(st, subIndex).getIdType());
			if (manager == null)
				return;
			manager.clearSelection(type);
			selections.fireSelectionDelta(manager);
		}
	}

	/**
	 * @param route
	 * @param subIndex
	 * @param selection
	 * @param clear
	 *            whether to clear before
	 */
	private void select(Band route, int subIndex, SelectionType type, boolean clear) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = getOrCreate(route.getIdType(st, subIndex));
			if (clear)
				manager.clearSelection(type);
			manager.addToType(type, route.getIds(st, subIndex));
			selections.fireSelectionDelta(manager);
		}
		repaint();
	}

	/**
	 * @param objectID
	 * @return
	 */
	private Band getRoute(int index) {
		if (index < 0 || index >= routes.size())
			return null;
		return routes.get(index);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Vec2f loc = getAbsoluteLocation();
		g.save().move(-loc.x(), -loc.y());
		float z = g.z();
		for (Band edge : routes) {
			g.incZ(0.002f);
			edge.render(g, w, h, this);
		}
		g.incZ(z - g.z());
		g.restore();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLUE);
		super.renderPickImpl(g, w, h);
		g.color(Color.BLACK);
		if (getVisibility() == EVisibility.PICKABLE) {
			g.incZ(0.05f);
			g.color(Color.RED);
			Vec2f loc = getAbsoluteLocation();
			g.save().move(-loc.x(), -loc.y());
			int j = 0;
			pickingOffsets.clear();
			for (int i = 0; i < routes.size(); i++) {
				pickingOffsets.put(i, j);
				j = routes.get(i).renderPick(g, w, h, this, pickingPool, j);
			}
			g.restore();
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


}

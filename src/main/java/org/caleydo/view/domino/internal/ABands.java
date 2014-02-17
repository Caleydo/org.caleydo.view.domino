/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.BitSet;
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
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.MiniMapCanvas.IHasMiniMap;
import org.caleydo.view.domino.internal.band.ABand;
import org.caleydo.view.domino.internal.band.IBandHost;

import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.common.util.IntIntHashMap.Entry;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public abstract class ABands extends GLElement implements MultiSelectionManagerMixin.ISelectionMixinCallback, IBandHost,
 IPickingListener, IPickingLabelProvider, IHasMiniMap {
	@DeepScan
	protected final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);


	protected final List<ABand> bands = new ArrayList<>();

	protected PickingPool pickingBandDetailPool;
	protected PickingPool pickingBandPool;
	private final IntIntHashMap pickingOffsets = new IntIntHashMap();

	protected abstract void update();

	/**
	 *
	 */
	public ABands() {
		setVisibility(EVisibility.PICKABLE);
		setPicker(null);
	}

	@Override
	protected void layoutImpl(int deltaTimeMs) {
		super.layoutImpl(deltaTimeMs);
		update();
	}


	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		pickingBandDetailPool = new PickingPool(context, PickingListenerComposite.concat(this, context.getSWTLayer()
				.createTooltip(this)));
		pickingBandPool = new PickingPool(context, new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onBandPick(pick);
			}
		});
	}

	/**
	 * @param pick
	 */
	protected abstract void onBandPick(Pick pick);

	@Override
	protected void takeDown() {
		pickingBandDetailPool.clear();
		pickingBandDetailPool = null;
		pickingBandPool.clear();
		pickingBandPool = null;
		super.takeDown();
	}

	@Override
	public String getLabel(Pick pick) {
		int[] split = split(pick.getObjectID());
		if (split == null)
			return "";
		ABand route = getRoute(split[0]);
		if (route == null)
			return "";
		return route.getLabel(split[1]);
	}

	@Override
	public void pick(Pick pick) {
		int[] split = split(pick.getObjectID());
		if (split == null)
			return;
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
	protected int[] split(int objectID) {
		Entry last = null;
		for (Entry entry : pickingOffsets) {
			if (entry.value > objectID)
				break;
			last = entry;
		}
		if (last == null)
			return null;
		return new int[] { last.key, objectID - last.value };
	}

	/**
	 * @param route
	 * @param type
	 */
	private void clear(ABand route, int subIndex, SelectionType type) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = selections.get(route.getIdType(st));
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
	private void select(ABand route, int subIndex, SelectionType type, boolean clear) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = getOrCreate(route.getIdType(st));
			if (clear) {
				manager.clearSelection(type);
			}
			manager.addToType(type, route.getIds(st, subIndex));
			selections.fireSelectionDelta(manager);
		}
		repaint();
	}

	/**
	 * @param objectID
	 * @return
	 */
	protected ABand getRoute(int index) {
		if (index < 0 || index >= bands.size())
			return null;
		return bands.get(index);
	}

	/**
	 * @param identifier
	 * @return
	 */
	private ABand getRoute(String identifier) {
		for (ABand band : bands)
			if (identifier.equals(band.getIdentifier()))
				return band;
		return null;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Vec2f loc = getShift();
		g.save().move(-loc.x(), -loc.y());
		float z = g.z();
		for (ABand edge : bands) {
			g.incZ(0.002f);
			edge.render(g, w, h, this);
		}
		g.incZ(z - g.z());
		g.restore();
	}

	protected abstract Vec2f getShift();

	@Override
	public void renderMiniMap(GLGraphics g) {
		Vec2f loc = getShift();
		g.save().move(-loc.x(), -loc.y());
		for (ABand edge : bands) {
			edge.renderMiniMap(g);
		}
		g.restore();
	}

	@Override
	public Rect getBoundingBox() {
		Rect r = null;
		Vec2f loc = getShift();
		for (ABand edge : bands) {
			Rect bounds = edge.getBoundingBox();
			if (r == null)
				r = bounds;
			else
				r = Rect.union(r, bounds);
		}
		if (r != null) {
			r.x(r.x() - loc.x());
			r.y(r.y() - loc.y());
		}
		return r;
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLUE);
		super.renderPickImpl(g, w, h);
		g.color(Color.BLACK);
		if (getVisibility() == EVisibility.PICKABLE) {
			g.incZ(0.05f);
			g.color(Color.RED);
			Vec2f loc = getShift();
			g.save().move(-loc.x(), -loc.y());
			int j = 0;
			pickingOffsets.clear();
			for (int i = 0; i < bands.size(); i++) {
				pickingOffsets.put(i, j);
				final ABand band = bands.get(i);
				g.pushName(pickingBandPool.get(i));
				g.incZ(-0.01f);
				band.renderMiniMap(g);
				g.incZ(0.01f);
				g.popName();

				// pop the self id but the band id
				g.popName();
				g.pushName(pickingBandPool.get(i));
				j = band.renderPick(g, w, h, this, pickingBandDetailPool, j);
				g.popName();
				g.pushName(pickingID); // FIXME HACK
			}
			g.restore();
			g.incZ(-0.05f);
			g.color(Color.BLACK);
		}
	}


	@Override
	public TypedSet getSelected(TypedSet ids, SelectionType type) {
		if (ids.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		TypedSet r = ids.intersect(new TypedSet(active, ids.getIdType()));
		return r;
	}

	@Override
	public boolean isSelected(TypedID id, SelectionType type) {
		SelectionManager manager = getOrCreate(id.getIdType());
		return manager.checkStatus(type, id.getId());
	}

	@Override
	public BitSet isSelected(TypedList ids, SelectionType type) {
		if (ids.isEmpty())
			return new BitSet(0);
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return new BitSet(0);

		BitSet r = new BitSet(ids.size());
		for (int i = 0; i < ids.size(); ++i)
			r.set(i, active.contains(ids.get(i)));
		return r;
	}

	/**
	 * @param idType
	 * @return
	 */
	protected SelectionManager getOrCreate(IDType idType) {
		SelectionManager manager = selections.get(idType);
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
	 * @param bandIdentifier
	 * @param increase
	 */
	public void changeLevel(String identifier, boolean increase) {
		ABand band = getRoute(identifier);
		if (band != null)
			band.changeLevel(increase);

	}

}

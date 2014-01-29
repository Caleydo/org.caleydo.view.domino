/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.band.ABand;
import org.caleydo.view.domino.internal.band.IBandHost;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.SetDragInfo;
import org.caleydo.view.domino.internal.undo.ChangeBandLevelCmd;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.common.util.IntIntHashMap.Entry;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class Bands extends GLElement implements MultiSelectionManagerMixin.ISelectionMixinCallback, IBandHost,
		IPickingListener, IPickingLabelProvider, IDragGLSource, ICallback<SelectionType> {
	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);


	private final List<ABand> bands = new ArrayList<>();

	private PickingPool pickingBandDetailPool;
	private PickingPool pickingBandPool;
	private final IntIntHashMap pickingOffsets = new IntIntHashMap();

	private int currentDragPicking;

	public Bands(NodeSelections selections) {
		selections.onBlockSelectionChanges(this);
	}

	@Override
	public void on(SelectionType data) {
		if (data == SelectionType.SELECTION && findParent(Domino.class).getTool() == EToolState.BANDS)
			relayout();

	}

	public void update() {
		Map<String, ABand> bak = Maps.uniqueIndex(bands, new Function<ABand, String>() {
			@Override
			public String apply(ABand input) {
				return input.getIdentifier();
			}
		});
		bands.clear();
		Domino domino = findParent(Domino.class);
		EToolState tool = domino.getTool();
		List<Block> blocks;
		if (tool != EToolState.BANDS)
			blocks = domino.getBlocks();
		else {
			blocks = ImmutableList.copyOf(domino.getSelections().getBlockSelection(SelectionType.SELECTION));
			if (blocks.size() < 2) // less than 2 -> show all bands
				blocks = domino.getBlocks();
		}

		final int length = blocks.size();

		// create bands
		Collection<Rectangle2D> bounds = new ArrayList<>();
		for (int i = 0; i < length; ++i) {
			final Block block = blocks.get(i);
			block.createBandsTo(blocks.subList(i + 1, length), bands);
			for (LinearBlock b : block.getLinearBlocks())
				bounds.add(shrink(block.getAbsoluteBounds(b).asRectangle2D()));
		}

		// collected the bounds check what we have to stubify
		if (tool != EToolState.BANDS) {
			outer: for (Iterator<ABand> it = bands.iterator(); it.hasNext();) {
				ABand band = it.next();
				for (Rectangle2D bound : bounds) {
					if (band.intersects(bound)) {
						// if (!band.stubify())
						it.remove();
						continue outer;
					}
				}
				if (bak.containsKey(band.getIdentifier())) {
					band.initFrom(bak.get(band.getIdentifier()));
				}
			}
		}
		pickingBandPool.ensure(0, bands.size());

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
	protected void onBandPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case RIGHT_CLICKED:
			UndoStack undo = findParent(Domino.class).getUndo();
			ABand band = getRoute(pick.getObjectID());
			undo.push(new ChangeBandLevelCmd(band.getIdentifier(), !((IMouseEvent) pick).isCtrlDown()));
			repaint();
			break;
		default:
			break;
		}
	}

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
			context.getMouseLayer().addDragSource(this);
			this.currentDragPicking = pick.getObjectID();
			select(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			clear(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER);
			break;
		default:
			break;
		}
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof ADragInfo)
			return ((ADragInfo) info).createUI(findParent(Domino.class));
		return null;
	}

	@Override
	public void onDropped(IDnDItem info) {
		// nothing to do
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		int[] split = split(currentDragPicking);
		if (split == null)
			return null;
		ABand route = getRoute(split[0]);
		TypedSet ids = route.getIds(SourceTarget.SOURCE, split[1]);
		return new SetDragInfo(route.getLabel(), ids, route.getDimension(SourceTarget.SOURCE));
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
	private ABand getRoute(int index) {
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
		Vec2f loc = getAbsoluteLocation();
		g.save().move(-loc.x(), -loc.y());
		float z = g.z();
		for (ABand edge : bands) {
			g.incZ(0.002f);
			edge.render(g, w, h, this);
		}
		g.incZ(z - g.z());
		g.restore();
	}

	public void renderMiniMap(GLGraphics g) {
		Vec2f loc = getAbsoluteLocation();
		g.save().move(-loc.x(), -loc.y());
		for (ABand edge : bands) {
			edge.renderMiniMap(g);
		}
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
			for (int i = 0; i < bands.size(); i++) {
				pickingOffsets.put(i, j);
				final ABand band = bands.get(i);
				g.pushName(pickingBandPool.get(i));
				g.incZ(-0.01f);
				band.renderMiniMap(g);
				g.incZ(0.01f);
				j = band.renderPick(g, w, h, this, pickingBandDetailPool, j);
				g.popName();
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
	private SelectionManager getOrCreate(IDType idType) {
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
	 * @param clear
	 * @param r
	 */
	public void selectBandsByBounds(boolean clear, Rectangle2D r) {
		if (clear) {
			for (SelectionManager m : selections) {
				m.clearSelection(SelectionType.SELECTION);
			}
		}

		r = shrink(r);

		for(ABand band : bands) {
			Pair<TypedSet, TypedSet> intersects = band.intersectingIds(r);
			TypedSet intersectsS = intersects.getFirst();
			TypedSet intersectsT = intersects.getSecond();
			for (TypedSet s : Arrays.asList(intersectsS, intersectsT)) {
				if (s.isEmpty())
					continue;
				SelectionManager manager = getOrCreate(s.getIdType());
				manager.addToType(SelectionType.SELECTION, s);
			}
		}

		for (SelectionManager m : selections) {
			selections.fireSelectionDelta(m);
		}
		repaint();
	}

	private static Rectangle2D shrink(Rectangle2D r) {
		return new Rectangle2D.Double(r.getX() + 2, r.getY() + 2, r.getWidth() - 4, r.getHeight() - 4);
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

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.band.ABand;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.SetDragInfo;
import org.caleydo.view.domino.internal.undo.ChangeBandLevelCmd;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class Bands extends ABands implements IDragGLSource, ICallback<SelectionType> {
	private int currentDragPicking;

	public Bands(NodeSelections selections) {
		selections.onBlockSelectionChanges(this);
	}

	@Override
	public void on(SelectionType data) {
		if (data == SelectionType.SELECTION && findParent(Domino.class).getTool() == EToolState.BANDS)
			relayout();

	}

	@Override
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
		pickingBandDetailPool.ensure(0, bands.size());

	}

	/**
	 * @param pick
	 */
	@Override
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
	public void pick(Pick pick) {
		super.pick(pick);
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(this);
			this.currentDragPicking = pick.getObjectID();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
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


	@Override
	protected Vec2f getShift() {
		return getAbsoluteLocation();
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
}

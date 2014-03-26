/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import gleem.linalg.Vec2f;

import java.util.Collection;
import java.util.Map;

import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.rnb.internal.band.ABand;
import org.caleydo.view.rnb.internal.band.ABandIdentifier;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class BlockBands extends ABands {

	@Override
	protected void update() {
		Map<ABandIdentifier, ABand> bak = Maps.uniqueIndex(bands, new Function<ABand, ABandIdentifier>() {
			@Override
			public ABandIdentifier apply(ABand input) {
				return input.getId();
			}
		});
		bands.clear();

		Block block = findParent(Block.class);
		block.createOffsetBands(bands);

		for (ABand band : bands) {
			if (bak.containsKey(band.getId())) {
				band.initFrom(bak.get(band.getId()));
			}
		}
	}


	@Override
	protected void onBandPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case RIGHT_CLICKED:
			ABand band = getRoute(pick.getObjectID());
			band.changeLevel(!((IMouseEvent) pick).isCtrlDown());
			repaint();
			break;
		default:
			break;
		}
	}

	@Override
	protected Vec2f getShift() {
		return new Vec2f(0, 0);
	}

	public void select(SelectionType type, IDType idType, Collection<Integer> ids, boolean additional) {
		SelectionManager manager = getOrCreate(idType);
		if (!additional)
			manager.clearSelection(type);
		manager.addToType(type, ids);
		selections.fireSelectionDelta(manager);
	}

	public void clear(SelectionType type, IDType idType, Collection<Integer> ids) {
		SelectionManager manager = getOrCreate(idType);
		if (ids == null)
			manager.clearSelection(type);
		else
			manager.removeFromType(type, ids);
		selections.fireSelectionDelta(manager);
	}

}

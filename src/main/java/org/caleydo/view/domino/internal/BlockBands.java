/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.util.Map;

import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.band.ABand;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class BlockBands extends ABands {

	@Override
	protected void update() {
		Map<String, ABand> bak = Maps.uniqueIndex(bands, new Function<ABand, String>() {
			@Override
			public String apply(ABand input) {
				return input.getIdentifier();
			}
		});
		bands.clear();

		Block block = findParent(Block.class);
		block.createOffsetBands(bands);

		for (ABand band : bands) {
			if (bak.containsKey(band.getIdentifier())) {
				band.initFrom(bak.get(band.getIdentifier()));
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

}

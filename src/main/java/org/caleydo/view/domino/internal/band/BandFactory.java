/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.INodeLocator;

/**
 * @author Samuel Gratzl
 *
 */
public class BandFactory {

	public static ABand create(String label, TypedGroupList sData, TypedGroupList tData, Rect ra, Rect rb,
			final INodeLocator sNodeLocator, final INodeLocator tNodeLocator, final EDimension sDim,
			final EDimension tDim) {
		MultiTypedSet shared = TypedSets.intersect(sData.asSet(), tData.asSet());
		if (shared.isEmpty())
			return null;
		if (sDim == tDim) {
			BandLine line = BandLines.create(ra, sDim.opposite(), rb, tDim.opposite());
			if (line == null)
				return null;
			return new Band(line, label, shared, sData, tData, sNodeLocator, tNodeLocator, sDim, tDim);
		} else {
			// cross
			if (sDim == EDimension.RECORD) {
				Vec2f s = ra.x2y();
				Vec2f t = rb.xy();
				return new CrossBand(label, shared, sData, tData, sNodeLocator, tNodeLocator, s, t);
			}
		}
		return null;
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.INodeLocator;

/**
 * @author Samuel Gratzl
 *
 */
public class BandFactory {

	public static ABand create(Pair<String, String> labels, TypedGroupList sData, TypedGroupList tData, ShearedRect ra,
			ShearedRect rb,
			final INodeLocator sNodeLocator, final INodeLocator tNodeLocator, final EDimension sDim,
			final EDimension tDim, BandIdentifier id) {
		MultiTypedSet shared = TypedSets.intersect(sData.asSet(), tData.asSet());
		if (shared.isEmpty())
			return null;
		if (sDim == tDim) {
			final EDirection primary = EDirection.getPrimary(sDim.opposite());
			final float minDistance = ABand.SHIFT * 2 + 5;
			if (primary.isHorizontal()) {
				if (ra.x2() < rb.x() - minDistance) {
					Vec2f sLoc = ra.x2y();
					Vec2f tLoc = rb.xy();
					id = id.with(true, false);
					return new ParaBand(labels, shared, sData, tData, sLoc, tLoc, sNodeLocator, tNodeLocator,
							primary.opposite(), primary, id);
				} else if (rb.x2() < ra.x() - minDistance) {
					Vec2f sLoc = ra.xy();
					Vec2f tLoc = rb.x2y();
					id = id.with(false, true);
					return new ParaBand(labels, shared, tData, sData, tLoc, sLoc, tNodeLocator, sNodeLocator,
							primary.opposite(), primary, id);
				}
				return null;
			} else {
				id = id.swap();
				if (ra.y2() < rb.y() - minDistance) {
					Vec2f sLoc = ra.xy2();
					Vec2f tLoc = rb.xy();
					id = id.with(true, false);
					return new ParaBand(labels, shared, sData, tData, sLoc, tLoc, sNodeLocator, tNodeLocator,
							primary.opposite(), primary, id);
				} else if (rb.y2() < ra.y() - minDistance) {
					Vec2f sLoc = ra.xy();
					Vec2f tLoc = rb.xy2();
					id = id.with(false, true);
					return new ParaBand(labels, shared, tData, sData, tLoc, sLoc, tNodeLocator, sNodeLocator,
							primary.opposite(), primary, id);
				}
				return null;
			}
		} else {
			// cross
			if (sDim == EDimension.RECORD) {
				Vec2f s = ra.x2y();
				Vec2f t = rb.xy();
				EDirection sDir = EDirection.getPrimary(sDim.opposite());
				if (s.x() > t.x())
					sDir = sDir.opposite();
				EDirection tDir = EDirection.getPrimary(tDim.opposite());
				if (s.y() < t.y())
					tDir = tDir.opposite();
				if (sDir == EDirection.EAST)
					s = ra.xy();
				if (tDir == EDirection.NORTH)
					t = rb.xy2();
				id = id.with(sDir != EDirection.EAST, tDir != EDirection.NORTH);
				return new CrossBand(labels, shared, sData, tData, sNodeLocator, tNodeLocator, s, t, sDir, tDir,
						id);
			} else {
				id = id.swap();
				// swap
				Vec2f t = ra.xy();
				Vec2f s = rb.x2y();
				EDirection sDir = EDirection.getPrimary(tDim.opposite());
				if (s.x() > t.x())
					sDir = sDir.opposite();
				EDirection tDir = EDirection.getPrimary(sDim.opposite());
				if (s.y() < t.y())
					tDir = tDir.opposite();
				if (sDir == EDirection.EAST)
					s = rb.xy();
				if (tDir == EDirection.NORTH)
					t = ra.xy2();
				id.with(sDir != EDirection.EAST, tDir != EDirection.NORTH);
				return new CrossBand(labels, shared, tData, sData, tNodeLocator, sNodeLocator, s, t, sDir, tDir,
						id);
			}
		}
	}
}

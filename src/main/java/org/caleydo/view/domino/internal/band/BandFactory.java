/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
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

	public static ABand create(String label, TypedGroupList sData, TypedGroupList tData, ShearedRect ra,
			ShearedRect rb,
			final INodeLocator sNodeLocator, final INodeLocator tNodeLocator, final EDimension sDim,
			final EDimension tDim, String identifier) {
		MultiTypedSet shared = TypedSets.intersect(sData.asSet(), tData.asSet());
		if (shared.isEmpty())
			return null;
		if (sDim == tDim) {
			BandLine line = BandLines.create(ra, sDim.opposite(), rb, tDim.opposite());
			if (line == null)
				return null;
			if (line.isInvalid())
				return null;
			Vec2f sPoint = line.getPoint(true, true);
			Vec2f tPoint = line.getPoint(false, true);
			EDirection sDir = EDirection.getPrimary(sDim);
			if (sDim.opposite().select(sPoint) > sDim.opposite().select(ra.xy()))
				sDir = sDir.opposite();
			EDirection tDir = EDirection.getPrimary(tDim);
			if (tDim.opposite().select(tPoint) > tDim.opposite().select(rb.xy()))
				tDir = tDir.opposite();
			return new Band(line, label, shared, sData, tData, sNodeLocator, tNodeLocator, sDir, tDir, identifier);
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
				return new CrossBand(label, shared, sData, tData, sNodeLocator, tNodeLocator, s, t, sDir, tDir,
						identifier);
			} else {
				Vec2f s = ra.xy();
				Vec2f t = rb.x2y();
				EDirection sDir = EDirection.getPrimary(tDim.opposite());
				if (t.y() >= s.y())
					sDir = sDir.opposite();
				EDirection tDir = EDirection.getPrimary(sDim.opposite());
				if (t.x() >= s.x())
					tDir = tDir.opposite();
				return new CrossBand(label, shared, tData, sData, tNodeLocator, sNodeLocator, t, s, sDir, tDir,
						identifier);
			}
		}
	}
}

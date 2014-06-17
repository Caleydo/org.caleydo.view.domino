/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.List;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.SelectionInfo;

import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomSelectionInfosCmd implements IMergeAbleCmd {

	private final List<SelectionInfo> items;
	private float factor;

	public ZoomSelectionInfosCmd(Iterable<SelectionInfo> l, float factor) {
		this.items = Lists.newArrayList(l);
		this.factor = factor;
	}

	@Override
	public String getLabel() {
		return "Zoom Selection Info";
	}

	@Override
	public ICmd run(Domino domino) {
		float bak = items.get(0).getScaleFactor();
		for (SelectionInfo item : items)
			item.setScaleFactor(factor);
		domino.getBands().relayout();
		return new ZoomSelectionInfosCmd(items, bak);
	}

	/**
	 * @param undo
	 * @return
	 */
	@Override
	public boolean merge(ICmd cmd) {
		if (!(cmd instanceof ZoomSelectionInfosCmd))
			return false;
		ZoomSelectionInfosCmd undo = (ZoomSelectionInfosCmd) cmd;
		this.factor = undo.factor;
		return true;
	}

}


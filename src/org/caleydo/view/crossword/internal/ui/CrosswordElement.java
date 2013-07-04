/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.layout.CompositeGLLayoutData;

/**
 * the root element of this view holding a {@link TablePerspective}
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordElement extends PickableGLElement implements
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback {

	private final TablePerspective tablePerspective;

	@DeepScan
	private final TablePerspectiveSelectionMixin selection;

	@DeepScan
	private final CrosswordLayoutInfo info;

	public CrosswordElement(TablePerspective tablePerspective) {
		this.tablePerspective = tablePerspective;
		this.selection = new TablePerspectiveSelectionMixin(tablePerspective, this);
		this.info = new CrosswordLayoutInfo();
		this.onPick(this.info);
		setLayoutData(CompositeGLLayoutData.combine(tablePerspective, info));
	}

	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return tablePerspective;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(tablePerspective.getDataDomain().getColor()).fillRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaintAll();
	}

	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {
		relayout();
	}
}

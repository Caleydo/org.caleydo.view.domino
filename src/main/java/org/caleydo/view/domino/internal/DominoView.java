/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal;
import java.util.List;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.view.domino.internal.event.ToggleHeaderAlwaysEvent;
import org.caleydo.view.domino.internal.serial.SerializedDominoView;

import v2.Domino;

/**
 * basic view based on {@link GLElement} with a {@link AMultiTablePerspectiveElementView}
 *
 * @author Samuel Gratzl
 *
 */
public class DominoView extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.domino";
	public static final String VIEW_NAME = "Domino";

	private final Domino domino;

	public DominoView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		domino = new Domino();
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		return new SerializedDominoView(this);
	}

	@Override
	protected GLElement createContent() {
		return ScrollingDecorator.wrap(domino, Settings.SCROLLBAR_WIDTH);
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.all;
	}

	@Override
	protected void applyTablePerspectives(GLElementDecorator root, List<TablePerspective> all,
			List<TablePerspective> added, List<TablePerspective> removed) {
		// domino.removeAll(removed);
		// domino.addAll(added);
	}

	@ListenTo(sendToMe = true)
	private void onToggleHeaderAlwaysHeader(ToggleHeaderAlwaysEvent event) {
		domino.setShowDebugInfos(!domino.isShowDebugInfos());
	}

	public void replaceTablePerspectiveInternally(TablePerspective from, TablePerspective to) {
		int fromIndex = this.tablePerspectives.indexOf(from);
		if (fromIndex < 0)
			return;
		this.tablePerspectives.set(fromIndex, to);
		fireTablePerspectiveChanged();
	}
}

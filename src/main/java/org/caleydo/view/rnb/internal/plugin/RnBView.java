/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.rnb.internal.plugin;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.canvas.GLThreadListenerWrapper;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.serial.SerializedDominoView;

/**
 * basic view based on {@link GLElement} with a {@link AMultiTablePerspectiveElementView}
 *
 * @author Samuel Gratzl
 *
 */
public class RnBView extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.domino";
	public static final String VIEW_NAME = "Domino";

	private final RnB domino;

	@DeepScan
	private final IGLKeyListener keyAdapter;

	public RnBView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		domino = new RnB();

		keyAdapter = GLThreadListenerWrapper.wrap(domino);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		canvas.addKeyListener(keyAdapter);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		super.dispose(drawable);
		canvas.removeKeyListener(keyAdapter);
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		return new SerializedDominoView(this);
	}

	@Override
	protected GLElement createContent() {
		return domino;
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

	public void replaceTablePerspectiveInternally(TablePerspective from, TablePerspective to) {
		int fromIndex = this.tablePerspectives.indexOf(from);
		if (fromIndex < 0)
			return;
		this.tablePerspectives.set(fromIndex, to);
		fireTablePerspectiveChanged();
	}
}

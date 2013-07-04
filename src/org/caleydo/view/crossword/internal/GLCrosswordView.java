/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal;

import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.view.crossword.api.CrosswordRootElement;
import org.caleydo.view.crossword.internal.ui.CrosswordElement;

import com.google.common.collect.Iterators;

/**
 * basic view based on {@link GLElement} with a {@link AMultiTablePerspectiveElementView}
 *
 * @author Samuel Gratzl
 *
 */
public class GLCrosswordView extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.crossword";
	public static final String VIEW_NAME = "CrossWord";

	private final CrosswordRootElement crossword;

	public GLCrosswordView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		this.crossword = new CrosswordRootElement();
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedCrosswordView serializedForm = new SerializedCrosswordView(this);
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.all;
	}

	@Override
	protected void applyTablePerspectives(AGLElementDecorator root, List<TablePerspective> all,
			List<TablePerspective> added, List<TablePerspective> removed) {
		if (root.getContent() == null)
			root.setContent(crossword);
		if (!removed.isEmpty()) {
			for (Iterator<CrosswordElement> it = Iterators.filter(crossword.iterator(), CrosswordElement.class); it
					.hasNext();) {
				if (removed.contains(it.next().getTablePerspective()))
					it.remove();
			}
		}
		for (TablePerspective t : added) {
			crossword.add(new CrosswordElement(t));
			for (TablePerspective rt : t.getRecordSubTablePerspectives())
				crossword.add(new CrosswordElement(rt));
		}

	}
}

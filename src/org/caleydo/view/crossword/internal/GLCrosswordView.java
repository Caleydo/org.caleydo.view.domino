/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander Lex, Christian Partl, Johannes Kepler
 * University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 *******************************************************************************/
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
import org.caleydo.view.crossword.CrosswordElement;
import org.caleydo.view.crossword.CrosswordRootElement;

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
		for (Iterator<CrosswordElement> it = Iterators.filter(crossword.iterator(), CrosswordElement.class); it
				.hasNext();) {
			if (removed.contains(it.next().getTablePerspective()))
				it.remove();
		}
		for (TablePerspective t : added)
			crossword.add(new CrosswordElement(t));
	}
}

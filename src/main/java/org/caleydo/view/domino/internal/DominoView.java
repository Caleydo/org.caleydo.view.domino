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
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.domino.internal.event.ToggleHeaderAlwaysEvent;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/**
 * basic view based on {@link GLElement} with a {@link AMultiTablePerspectiveElementView}
 *
 * @author Samuel Gratzl
 *
 */
public class DominoView extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.domino";
	public static final String VIEW_NAME = "Domino";

	private final GLElement domino;

	public DominoView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		// this.domino = new DominoMultiElement(new MultiConfig());
		GLElementFactoryContext.Builder builder = GLElementFactoryContext.builder();
		builder.put(PathwayGraph.class, PathwayManager.get().getAllItems().iterator().next());
		ImmutableList<GLElementSupplier> extension = GLElementFactories.getExtensions(builder.build(), "domino", Predicates.alwaysTrue());
		domino = new GLElementFactorySwitcher(extension, ELazyiness.DESTROY);
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		//SerializedDominoView serializedForm = new SerializedDominoView(this);
		return null;
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
		// domino.toggleAlwaysShowHeader();
	}

	public void replaceTablePerspectiveInternally(TablePerspective from, TablePerspective to) {
		int fromIndex = this.tablePerspectives.indexOf(from);
		if (fromIndex < 0)
			return;
		this.tablePerspectives.set(fromIndex, to);
		fireTablePerspectiveChanged();
	}
}

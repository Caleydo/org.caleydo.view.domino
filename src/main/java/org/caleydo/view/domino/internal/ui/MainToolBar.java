/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.ui.model.IDominoGraphListener;
import org.caleydo.view.domino.internal.ui.model.IEdge;
import org.caleydo.view.domino.internal.ui.model.NodeUIState;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;

/**
 * @author Samuel Gratzl
 *
 */
public class MainToolBar extends GLElementContainer implements PropertyChangeListener, IDominoGraphListener {

	private INode active;

	public MainToolBar() {
		super(GLLayouts.flowHorizontal(3));
		setRenderer(GLRenderers.fillRect(new Color(0.95f)));
	}

	@Override
	public void vertexAdded(INode vertex, Collection<IEdge> edges) {
		vertex.addPropertyChangeListener(this);
	}

	@Override
	public void vertexRemoved(INode vertex, Collection<IEdge> edges) {
		vertex.removePropertyChangeListener(this);
	}

	@Override
	public void vertexSortingChanged(ISortableNode vertex, EDimension dim) {

	}

	public void addSeparator() {
		this.add(new Separator());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		INode source = (INode) evt.getSource();
		switch (evt.getPropertyName()) {
		case NodeUIState.PROP_PROXIMITY_MODE:

			break;
		case NodeUIState.PROP_STATE:
			if (source.getUIState().isSelected()) {
				setActive(source);
			} else if (!source.getUIState().isSelected() && active == source)
				setActive(null);
			break;
		}
	}

	/**
	 * @param source
	 */
	private void setActive(INode node) {
		if (this.active == node)
			return;
		this.active = node;

		if (this.active == null)
			this.clear();
		else
			this.add(new GLElement(GLRenderers.drawText(node.getLabel())));
	}

	private static class Separator extends GLElement {
		public Separator() {
			setSize(5, -1);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.DARK_GRAY);
			g.drawLine(w * 0.5f, 0, w * 0.5f, h);
			super.renderImpl(g, w, h);
		}
	}
}

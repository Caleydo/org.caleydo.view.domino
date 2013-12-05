/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANodeUI<T extends INode> extends GLElementDecorator implements PropertyChangeListener {
	protected final T node;

	public ANodeUI(T node) {
		this.node = node;
		setLayoutData(node);
	}

	@Override
	protected void init(IGLElementContext context) {
		rebuild();
		node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);
		super.init(context);
	}

	@Override
	protected void takeDown() {
		node.removePropertyChangeListener(INode.PROP_TRANSPOSE, this);
		super.takeDown();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case INode.PROP_TRANSPOSE:
			rebuild();
			break;
		}
	}

	private void rebuild() {
		List<GLElementSupplier> children = createVis();
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(children, ELazyiness.DESTROY);
		setContent(s);
	}

	protected abstract List<GLElementSupplier> createVis();

}

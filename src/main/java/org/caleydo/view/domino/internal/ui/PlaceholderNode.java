/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.domino.api.model.graph.ADataNode;
import org.caleydo.view.domino.api.model.graph.ANode;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceholderNode extends ADataNode {

	public PlaceholderNode(INode node, boolean transposed) {
		super("", node.getData(EDimension.DIMENSION), node.getData(EDimension.RECORD));
		this.transposed = transposed;
		setLayoutData(node.getLayoutDataAs(Object.class, null));
	}

	public PlaceholderNode(PlaceholderNode parent, String label, TypedSet dim, TypedSet rec) {
		super(label, dim, rec);
		this.transposed = parent.transposed;
	}

	public PlaceholderNode(PlaceholderNode clone) {
		super(clone);
	}

	@Override
	public INode extract(String label, ITypedCollection dim, ITypedCollection rec) {
		return new PlaceholderNode(this, label, dim.asSet(), rec.asSet());
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	@Override
	public ANode clone() {
		return new PlaceholderNode(this);
	}

	static class UI extends GLElement implements INodeUI {
		private final PlaceholderNode node;
		public UI(PlaceholderNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		public INode asNode() {
			return node;
		}


		@Override
		public GLElement getToolBar() {
			return null;
		}

		@Override
		public GLElement asGLElement() {
			return this;
		}

		@Override
		public boolean setData(EDimension dim, TypedList data) {
			return false;
		}

		@Override
		public double getSize(EDimension dim) {
			return 80;
		}
	}
}

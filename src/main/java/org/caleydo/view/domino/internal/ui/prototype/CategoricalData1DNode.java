/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

/**
 * @author Samuel Gratzl
 *
 */
public class CategoricalData1DNode extends AData1DNode {

	/**
	 * @param data
	 */
	public CategoricalData1DNode(TablePerspective data) {
		super(data);
		assert DataSupportDefinitions.categoricalColumns.apply(data);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final CategoricalData1DNode node;

		public UI(CategoricalData1DNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(node.getDataDomain().getColor()).fillRect(0, 0, w, h);
			super.renderImpl(g, w, h);
		}
	}
}

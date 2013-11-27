/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

/**
 * @author Samuel Gratzl
 *
 */
public class NumericalData2DNode extends AData2DNode {

	/**
	 * @param data
	 */
	public NumericalData2DNode(ATableBasedDataDomain data) {
		super(data);
		assert DataSupportDefinitions.numericalTables.apply(data);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final NumericalData2DNode node;

		public UI(NumericalData2DNode node) {
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

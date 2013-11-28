/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.Objects;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

/**
 * @author Samuel Gratzl
 *
 */
public class NumericalData1DNode extends AData1DNode {

	/**
	 * @param data
	 */
	public NumericalData1DNode(TablePerspective data) {
		super(data);
		assert DataSupportDefinitions.numericalTables.apply(data);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final NumericalData1DNode node;

		public UI(NumericalData1DNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(node.getDataDomain().getColor()).fillRect(0, 0, w, h);
			super.renderImpl(g, w, h);
		}
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		Object r1 = getRaw(o1);
		Object r2 = getRaw(o2);
		if (Objects.equals(r1, r2))
			return 0;
		if (r1 == null)
			return 1;
		if (r2 == null)
			return -1;
		assert r1 instanceof Number && r2 instanceof Number;
		if (r1 instanceof Float && r2 instanceof Float)
			return ((Float) r1).compareTo((Float) r2);
		if (r1 instanceof Integer && r2 instanceof Integer)
			return ((Integer) r1).compareTo((Integer) r2);
		return Double.compare(((Number) r1).doubleValue(), ((Number) r2).doubleValue());
	}
}

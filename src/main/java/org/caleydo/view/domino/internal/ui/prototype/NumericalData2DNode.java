/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

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

	public NumericalData2DNode(NumericalData2DNode clone) {
		super(clone);
	}

	@Override
	public NumericalData2DNode clone() {
		return new NumericalData2DNode(this);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElementDecorator {
		private final NumericalData2DNode node;

		public UI(NumericalData2DNode node) {
			this.node = node;
			setLayoutData(node);
			GLElementFactoryContext context = GLElementFactoryContext.builder().withData(
node.getDataDomain().getDefaultTablePerspective()).build();
			ImmutableList<GLElementSupplier> children = GLElementFactories.getExtensions(context,
					"domino.2d.numerical", Predicates.alwaysTrue());
			GLElementFactorySwitcher s = new GLElementFactorySwitcher(children, ELazyiness.DESTROY);
			setContent(s);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			// g.color(node.getDataDomain().getColor()).fillRect(0, 0, w, h);
			super.renderImpl(g, w, h);
		}
	}
}

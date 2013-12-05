/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.internal.ui.prototype.ui.ANodeUI;

import com.google.common.base.Predicates;

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

	private static class UI extends ANodeUI<NumericalData2DNode> {

		public UI(NumericalData2DNode node) {
			super(node);
		}

		@Override
		protected List<GLElementSupplier> createVis() {
			Builder b = GLElementFactoryContext.builder();
			b.withData(node.getDataDomain().getDefaultTablePerspective());
			return GLElementFactories.getExtensions(b.build(), "domino.2d.numerical", Predicates.alwaysTrue());
		}
	}
}

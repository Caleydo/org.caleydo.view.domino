/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
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
public class NumericalData1DNode extends AData1DNode {

	/**
	 * @param data
	 */
	public NumericalData1DNode(TablePerspective data, EDimension main) {
		super(data, main);
		assert DataSupportDefinitions.dataClass(EDataClass.REAL_NUMBER, EDataClass.NATURAL_NUMBER).apply(data);
	}

	public NumericalData1DNode(NumericalData1DNode clone) {
		super(clone);
	}

	@Override
	public NumericalData1DNode clone() {
		return new NumericalData1DNode(this);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends ANodeUI<NumericalData1DNode> {

		public UI(NumericalData1DNode node) {
			super(node);
		}

		@Override
		protected List<GLElementSupplier> createVis() {
			Builder b = GLElementFactoryContext.builder();
			// b.withData(node.getData());
			b.put(EDimension.class, node.getDimension());
			return GLElementFactories.getExtensions(b.build(),
					"domino.1d.numerical", Predicates.alwaysTrue());
		}
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		float r1 = getNormalized(o1);
		float r2 = getNormalized(o2);
		return NumberUtils.compare(r1, r2);
	}
}

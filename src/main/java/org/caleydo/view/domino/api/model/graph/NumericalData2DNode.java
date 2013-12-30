/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;
import org.caleydo.view.domino.spi.model.graph.INode;

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

	public NumericalData2DNode(NumericalData2DNode parent, String label, TypedSet dim, TypedSet rec) {
		super(parent, label, dim, rec);
	}

	public NumericalData2DNode(NumericalData2DNode clone) {
		super(clone);
	}

	@Override
	public NumericalData2DNode clone() {
		return new NumericalData2DNode(this);
	}

	@Override
	public INode extract(String label, ITypedCollection dim, ITypedCollection rec) {
		return new NumericalData2DNode(this, label, dim.asSet(), rec.asSet());
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	private static class UI extends ANodeUI<NumericalData2DNode> {

		public UI(NumericalData2DNode node) {
			super(node);
		}

		@Override
		protected String getExtensionID() {
			return "2d.numerical";
		}

		@Override
		protected void fill(Builder b, TypedList dim, TypedList rec) {
			TablePerspective t = node.asTablePerspective(dim, rec);
			b.withData(t);
		}
	}
}
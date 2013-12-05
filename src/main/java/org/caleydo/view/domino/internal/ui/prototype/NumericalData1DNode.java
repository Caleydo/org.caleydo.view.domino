/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.beans.PropertyChangeListener;
import java.util.Objects;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TableDoubleLists;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.view.domino.internal.ui.prototype.ui.ATextured1DUI;
import org.caleydo.vis.lineup.ui.GLPropertyChangeListeners;

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

	public IDoubleList getNormalizedData() {
		return TableDoubleLists.asNormalizedList(data);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends ATextured1DUI {
		private final NumericalData1DNode node;
		private final PropertyChangeListener repaint = GLPropertyChangeListeners.repaintOnEvent(this);

		public UI(NumericalData1DNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void init(IGLElementContext context) {
			super.init(context);
			update(node.getNormalizedData().iterator());
			node.addPropertyChangeListener(PROP_TRANSPOSE, repaint);
		}

		@Override
		protected void takeDown() {
			node.removePropertyChangeListener(PROP_TRANSPOSE, repaint);
			super.takeDown();
		}

		@Override
		protected EDimension getDimension() {
			return node.getDimension();
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

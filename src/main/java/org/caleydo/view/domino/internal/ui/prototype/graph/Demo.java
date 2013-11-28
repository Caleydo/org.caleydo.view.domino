/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import static org.caleydo.view.domino.internal.ui.prototype.EDirection.ABOVE;
import static org.caleydo.view.domino.internal.ui.prototype.EDirection.BELOW;
import static org.caleydo.view.domino.internal.ui.prototype.EDirection.LEFT_OF;
import static org.caleydo.view.domino.internal.ui.prototype.EDirection.RIGHT_OF;

import java.util.Random;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.datadomain.mock.AValueFactory;
import org.caleydo.datadomain.mock.MockDataDomain;
import org.caleydo.view.domino.internal.ui.prototype.CategoricalData2DNode;
import org.caleydo.view.domino.internal.ui.prototype.NumericalData1DNode;
import org.caleydo.view.domino.internal.ui.prototype.NumericalData2DNode;
import org.caleydo.view.domino.internal.ui.prototype.StratificationNode;

/**
 * @author Samuel Gratzl
 *
 */
public class Demo {

	public static void fill(DominoGraph graph) {
		MockDataDomain d_ab = MockDataDomain.createNumerical(10, 10, new ValueFac("a", "a", "b", "b"));
		Perspective s_b = MockDataDomain.addRecGrouping(d_ab, 3, 4, 2).getRecordPerspective();
		Perspective s_b2 = MockDataDomain.addRecGrouping(d_ab, 1, 1).getRecordPerspective();
		Perspective s_a = MockDataDomain.addDimGrouping(d_ab, 1, 1, 1, 2).getDimensionPerspective();
		MockDataDomain d_cb = MockDataDomain.createCategorical(10, 10, new ValueFac("c", "c", "b", "b"), "A", "B", "C",
				"D");
		MockDataDomain d_ac = MockDataDomain.createNumerical(10, 10, new ValueFac("a", "a", "c", "c"));
		MockDataDomain d_b = MockDataDomain.createNumerical(1, 10, new ValueFac("IN", "IN", "b", "b"));
		TablePerspective d1_b = d_b.getDefaultTablePerspective();
		Perspective s_c = MockDataDomain.addRecGrouping(d_ab, 3, 3).getRecordPerspective();

		NumericalData2DNode n2d_ab = new NumericalData2DNode(d_ab);
		NumericalData2DNode n2d_ac = new NumericalData2DNode(d_ac);
		CategoricalData2DNode c2d_cb = new CategoricalData2DNode(d_cb);

		NumericalData1DNode n1d_b = new NumericalData1DNode(d1_b, EDimension.RECORD);

		StratificationNode ns_b = new StratificationNode(s_b, EDimension.RECORD);
		StratificationNode ns_b2 = new StratificationNode(s_b2, EDimension.RECORD);
		StratificationNode ns_a = new StratificationNode(s_a, EDimension.DIMENSION);
		StratificationNode ns_c = new StratificationNode(s_c, EDimension.RECORD);

		graph.addVertex(n2d_ab);
		graph.addVertex(n2d_ac);
		graph.addVertex(c2d_cb);
		graph.addVertex(n1d_b);

		graph.addVertex(ns_b);
		graph.addVertex(ns_b2);
		graph.addVertex(ns_a);
		graph.addVertex(ns_c);

		graph.magnetic(ns_b, LEFT_OF, n2d_ab);
		graph.magnetic(ns_a, BELOW, n2d_ab);
		graph.magnetic(ns_a, ABOVE, n2d_ac);
		graph.magnetic(n2d_ac, LEFT_OF, ns_c);

		graph.magnetic(ns_b2, LEFT_OF, c2d_cb);
		graph.magnetic(c2d_cb, LEFT_OF, n1d_b);

		graph.band(ns_b2, RIGHT_OF, n2d_ab);
	}

	private static class ValueFac extends AValueFactory {
		private final Random r = new Random();
		private final String dimCat, dimID, recCat, recID;

		public ValueFac(String dimCat, String dimID, String recCat, String recID) {
			this.dimCat = dimCat;
			this.dimID = dimID;
			this.recCat = recCat;
			this.recID = recID;
		}

		@Override
		public int nextInt(int n) {
			return r.nextInt(n);
		}

		@Override
		public float nextFloat() {
			return r.nextFloat();
		}

		@Override
		public String getIDCategory(EDimension dim) {
			return dim.select(dimCat, recCat);
		}

		@Override
		public String getIDType(EDimension dim) {
			return dim.select(dimID, recID);
		}
	}


}

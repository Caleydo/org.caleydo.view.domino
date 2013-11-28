/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.ui.prototype.BandEdge;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.jgrapht.DirectedGraph;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
class LayoutBlock implements Runnable {
	private final NodeLayoutElement[][] arr;
	private final float[] cols;
	private final float[] rows;

	private float x = 0;
	private float y = 0;

	public LayoutBlock(NodeLayoutElement[][] arr, float[] cols, float[] rows) {
		this.arr = arr;
		this.cols = cols;
		this.rows = rows;
	}

	public Vec2f getSize() {
		return new Vec2f(cols[cols.length - 1], rows[rows.length - 1]);
	}

	public void shift(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public void run() {
		for (int i = 0; i < arr.length; ++i) {
			NodeLayoutElement[] line = arr[i];
			for (int j = 0; j < line.length; ++j) {
				NodeLayoutElement v = line[j];
				if (v == null)
					continue;
				Vec2f size = v.getSize();
				float xi = (j == 0 ? 0 : cols[j - 1]);
				float yi = (i == 0 ? 0 : rows[i - 1]);
				float wi = cols[j] - xi;
				float hi = rows[i] - yi;
				xi += x + (wi - size.x()) * 0.5f; // shift and center
				yi += y + (hi - size.y()) * 0.5f; // shift and center
				v.setBounds(xi, yi, size.x(), size.y());
			}
		}
	}

	public static LayoutBlock create(INode root, DirectedGraph<INode, IEdge> graph,
			Function<INode, NodeLayoutElement> lookup) {
		Map<INode, Point> grid = Maps.newIdentityHashMap();
		placeNode(root, 0, 0, grid, graph);
		Rectangle r = boundingBoxOf(grid.values());

		// shift grid such that grid starts at 0,0
		grid = shiftGrid(grid, -r.x, -r.y);
		r.x = r.y = 0;

		NodeLayoutElement[][] arr = transformGrid(grid, r.width, r.height, lookup);
		float[] cols = new float[r.width];
		float[] rows = new float[r.height];

		for (int i = 0; i < arr.length; ++i) {
			NodeLayoutElement[] line = arr[i];
			for (int j = 0; j < line.length; ++j) {
				NodeLayoutElement v = line[j];
				if (v == null)
					continue;

				Vec2f size = v.getSize();
				float wShift = 0;
				float hShift = 0;
				if (j > 0 && line[j - 1] != null && line[j - 1].getSize().y() < size.y())
					wShift += 10;
				if (j < line.length - 1 && line[j + 1] != null && line[j + 1].getSize().y() < size.y())
					wShift += 10;
				if (i > 0 && arr[i - 1][j] != null && arr[i - 1][j].getSize().x() < size.x())
					hShift += 10;
				if (i < arr.length - 1 && arr[i + 1][j] != null && arr[i + 1][j].getSize().x() < size.x())
					hShift += 10;
				rows[i] = Math.max(rows[i], size.y() + hShift);
				cols[j] = Math.max(cols[j], size.x() + wShift);
			}
		}

		// postsum for faster access: 1,2,3 will be 1,3,6 -> delta = x[i]-i==0?:0:-x[i-1]
		cols = postsum(cols);
		rows = postsum(rows);

		return new LayoutBlock(arr, cols, rows);
	}

	/**
	 * @param cols
	 * @return
	 */
	private static float[] postsum(float[] cols) {
		for (int i = 1; i < cols.length; ++i) {
			cols[i] += cols[i - 1];
		}
		return cols;
	}

	/**
	 * @param grid
	 * @param lookup
	 * @return
	 */
	private static NodeLayoutElement[][] transformGrid(Map<INode, Point> grid, int cols, int rows,
			Function<INode, NodeLayoutElement> lookup) {
		NodeLayoutElement[][] r = new NodeLayoutElement[rows][cols];
		for (Entry<INode, Point> entry : grid.entrySet()) {
			Point p = entry.getValue();
			r[p.y][p.x] = lookup.apply(entry.getKey());
		}
		return r;
	}

	private static Map<INode, Point> shiftGrid(Map<INode, Point> grid, int x_shift, int y_shift) {
		if (x_shift == 0 && y_shift == 0)
			return grid;
		for (Entry<INode, Point> entry : grid.entrySet()) {
			Point p = entry.getValue();
			p.x += x_shift;
			p.y += y_shift;
		}
		return grid;
	}

	/**
	 * @param grid
	 * @return
	 */
	private static Rectangle boundingBoxOf(Iterable<Point> points) {
		int x = 0;
		int y = 0;
		int x_max = 0;
		int y_max = 0;
		for (Point p : points) {
			if (p.x < x)
				x = p.x;
			else if (p.x > x_max)
				x_max = p.x;
			if (p.y < y)
				y = p.y;
			else if (p.y > y_max)
				y_max = p.y;
		}
		return new Rectangle(x, y, x_max - x + 1, y_max - y + 1);
	}

	private static void placeNode(INode node, int x, int y, Map<INode, Point> grid, DirectedGraph<INode, IEdge> graph) {
		if (grid.containsKey(node))
			return;
		grid.put(node, new Point(x, y));
		for (IEdge edge : graph.outgoingEdgesOf(node)) {
			INode target = graph.getEdgeTarget(edge);
			EDirection dir = edge.getDirection();
			int f = (edge instanceof BandEdge) ? 2 : 1;
			placeNode(target, x + f * dir.asInt(EDimension.DIMENSION), y + f * dir.asInt(EDimension.RECORD), grid,
					graph);
		}
	}

}
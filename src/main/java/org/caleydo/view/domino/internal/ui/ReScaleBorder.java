/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.eclipse.swt.SWT;

/**
 * a special border renderer that let the user resize this element
 *
 * @author Samuel Gratzl
 *
 */
public final class ReScaleBorder extends GLElement implements IPickingListener {
	private static final int NORTH = 0;
	private static final int NORTHEAST = 1;
	private static final int EAST = 2;
	private static final int SOUTHEAST = 3;
	private static final int SOUTH = 4;
	private static final int SOUTHWEST = 5;
	private static final int WEST = 6;
	private static final int NORTHWEST = 7;

	private final DominoLayoutInfo info;

	private int[] pickingIds;

	public ReScaleBorder(DominoLayoutInfo info) {
		setPicker(null); // custom picking
		this.info = info;
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		pickingIds = new int[8];
		for (int i = 0; i < pickingIds.length; ++i)
			pickingIds[i] = context.registerPickingListener(this, i);
	}

	@Override
	protected void takeDown() {
		for (int pickingId : pickingIds)
			context.unregisterPickingListener(pickingId);
		pickingIds = null;
		super.takeDown();
	}

	@Override
	public void pick(Pick pick) {
		int direction = pick.getObjectID();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getSWTLayer().setCursor(toCursor(direction));
			break;
		case MOUSE_OUT:
			context.getSWTLayer().setCursor(-1);
			break;
		case DRAG_DETECTED:
			if (!pick.isAnyDragging())
				pick.setDoDragging(true);
			break;
		case DRAGGED:
			if (!pick.isDoDragging())
				return;
			info.enlarge(pick.getDx(), toXEnlarge(direction), pick.getDy(), toYEnlarge(direction));
			break;
		default:
			break;
		}
	}

	/**
	 * @param direction
	 * @return
	 */
	private int toYEnlarge(int direction) {
		switch (direction) {
		case NORTH:
		case NORTHEAST:
		case NORTHWEST:
			return -1;
		case SOUTH:
		case SOUTHEAST:
		case SOUTHWEST:
			return +1;
		default:
			return 0;
		}
	}

	/**
	 * @param direction
	 * @return
	 */
	private int toXEnlarge(int direction) {
		switch (direction) {
		case EAST:
		case NORTHEAST:
		case SOUTHEAST:
			return 1;
		case WEST:
		case NORTHWEST:
		case SOUTHWEST:
			return -1;
		default:
			return 0;
		}
	}

	private int toCursor(int direction) {
		switch (direction) {
		case NORTH:
			return SWT.CURSOR_SIZEN;
		case NORTHEAST:
			return SWT.CURSOR_SIZENE;
		case EAST:
			return SWT.CURSOR_SIZEE;
		case SOUTHEAST:
			return SWT.CURSOR_SIZESE;
		case SOUTH:
			return SWT.CURSOR_SIZES;
		case SOUTHWEST:
			return SWT.CURSOR_SIZESW;
		case WEST:
			return SWT.CURSOR_SIZEW;
		case NORTHWEST:
			return SWT.CURSOR_SIZENW;
		}
		return SWT.CURSOR_HELP;
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
		g.lineWidth(3);
		final float t = Math.min(w, h) * 0.05f;
		final float t2 = t*2;
		final float x = -1;
		final float y = -1;
		w+=2;
		h+=2;
		g.pushName(pickingIds[NORTH]).drawLine(x+t, y, w-t2, y).popName();
		g.pushName(pickingIds[EAST]).drawLine(w, y+t, w, h-t2).popName();
		g.pushName(pickingIds[SOUTH]).drawLine(x+t, h, w-t2, h).popName();
		g.pushName(pickingIds[WEST]).drawLine(x, y+t, x, h-t2).popName();

		g.pushName(pickingIds[NORTHEAST]).drawLine(w - t, y, t, y).drawLine(w, y, w, t).popName();
		g.pushName(pickingIds[SOUTHEAST]).drawLine(w, h - t, w, t).drawLine(w - t, h, t, h).popName();
		g.pushName(pickingIds[SOUTHWEST]).drawLine(x, h - t, x, t).drawLine(x, h, t, h).popName();
		g.pushName(pickingIds[NORTHWEST]).drawLine(x, y, t, y).drawLine(x, y, x, t).popName();

		g.lineWidth(1);
	}

	@Override
	protected boolean hasPickAbles() {
		return info.getConfig().canScale();
	}

}

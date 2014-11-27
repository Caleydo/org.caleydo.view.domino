/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.util.Arrays;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.ui.Ruler;
import org.caleydo.view.domino.internal.undo.RemoveRulerCmd;
import org.caleydo.view.domino.internal.undo.SetRulerMaxCmd;
import org.caleydo.view.domino.internal.undo.TransposeRulerCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class RulerTools extends AItemTools implements ISelectionCallback<Integer> {

	private final Ruler ruler;

	/**
	 * @param undo
	 */
	public RulerTools(UndoStack undo, Ruler ruler) {
		super(undo);
		this.ruler = ruler;

		GLComboBox<Integer> items = new GLComboBox<Integer>(Arrays.asList(0, 5, 10, 50, 100, 250, 500, 1000, 5000,
				10000,
				Integer.MAX_VALUE),
				new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				Integer r = parent.getLayoutDataAs(Integer.class, null);
				if (r == null)
					return;
				String t;
				switch (r.intValue()) {
						case 0:
					t = "visible";
					break;
				case Integer.MAX_VALUE:
					t = "all";
					break;
				default:
					t = r.toString();
				}
						g.drawText(t, 2, 2 + (h - 12) * 0.5f, w - 2, 11);
			}
		}, GLRenderers.fillRect(Color.WHITE));
		items.setTooltip("Set the ruler size");
		if (ruler.getMaxElements() == 100)
			items.setSelected(4);
		else
			items.setSelected(6);
		items.setCallback(this);
		this.add(items.setSize(50, -1));
		addButton("Transpose", Resources.ICON_TRANSPOSE);
		addButton("Remove", Resources.ICON_DELETE);

		setzDelta(2.f);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(Color.LIGHT_BLUE);
		RoundedRectRenderer.render(g, 0, 0, w, h, 5, 3, RoundedRectRenderer.FLAG_TOP | RoundedRectRenderer.FLAG_FILL);
		super.renderImpl(g, w, h);
	}

	@Override
	public void onSelectionChanged(GLComboBox<? extends Integer> widget, Integer item) {
		int size = toSize(item);
		undo.push(new SetRulerMaxCmd(ruler, size));
	}

	/**
	 * @param item
	 * @return
	 */
	private int toSize(Integer item) {
		switch (item.intValue()) {
		case 0:
			return findParent(Domino.class).getVisibleItemCount(ruler.getIDCategory());
		case Integer.MAX_VALUE:
			return Ruler.getTotalMax(ruler.getIDCategory());
		default:
			return item.intValue();
		}
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		switch (button.getTooltip()) {
		case "Transpose":
			undo.push(new TransposeRulerCmd(ruler));
			break;
		case "Remove":
			undo.push(new RemoveRulerCmd(ruler));
			break;
		default:
			break;
		}
	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.ui;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.gui.util.RenameNameDialog;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.ISWTLayer.ISWTLayerRunnable;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.text.ETextStyle;
import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.UndoStack;
import org.caleydo.view.rnb.internal.undo.SetAnnotationCmd;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author Samuel Gratzl
 *
 */
public class AnnotationItem extends AItem {
	private String text = "<DoubleClick to Edit>";

	public AnnotationItem(UndoStack undo) {
		super(undo);
		dim = EDimension.DIMENSION;
		if (dim.isHorizontal())
			setSize(200, 20);
		else
			setSize(20, 200);
	}

	/**
	 * @return the text, see {@link #text}
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            setter, see {@link text}
	 */
	public void setText(String text) {
		this.text = text;
		repaint();
	}
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (dim.isHorizontal())
			g.drawText(text, 1, 1, w - 2, 12, VAlign.LEFT);
		else {
			g.drawRotatedText(text, 1, +h - 1, h - 2, 12, VAlign.RIGHT, ETextStyle.PLAIN, -90);
		}
		if (hovered)
			g.color(Color.BLACK).drawRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}



	@Override
	public void pick(Pick pick) {
		super.pick(pick);
		switch (pick.getPickingMode()) {
		case DOUBLE_CLICKED:
			editText();
			break;
		default:
			break;
		}
	}

	private void editText() {
		context.getSWTLayer().run(new ISWTLayerRunnable() {
			@Override
			public void run(Display display, Composite canvas) {
				String label = text;
				String newlabel = RenameNameDialog.show(null, "Edit Annotation: " + label, label);
				if (newlabel != null && !label.equals(newlabel))
					EventPublisher.trigger(new SetTextEvent(newlabel).to(AnnotationItem.this));
			}
		});
	}

	@ListenTo(sendToMe = true)
	private void onSetTextEvent(SetTextEvent event) {
		findParent(RnB.class).getUndo().push(new SetAnnotationCmd(this, event.getText()));
	}

	public static class SetTextEvent extends ADirectedEvent {
		private final String text;

		public SetTextEvent(String text) {
			this.text = text;
		}

		/**
		 * @return the text, see {@link #text}
		 */
		public String getText() {
			return text;
		}
	}
}

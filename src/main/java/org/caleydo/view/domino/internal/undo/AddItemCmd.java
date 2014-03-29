package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.AItem;


public class AddItemCmd implements ICmd {
	private final AItem item;

	public AddItemCmd(AItem item) {
		this.item = item;
	}

	@Override
	public String getLabel() {
		return "Add " + item.getClass().getSimpleName();
	}

	@Override
	public ICmd run(Domino rnb) {
		rnb.getOutlerBlocks().addItem(item);
		rnb.getBands().relayout();
		return new RemoveItemCmd(item);
	}
}
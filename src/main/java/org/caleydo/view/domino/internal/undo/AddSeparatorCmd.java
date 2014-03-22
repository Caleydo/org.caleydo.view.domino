package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Separator;


public class AddSeparatorCmd implements ICmd {
	private final Separator separator;

	public AddSeparatorCmd(Separator ruler) {
		this.separator = ruler;
	}

	@Override
	public String getLabel() {
		return "Add Separator";
	}

	@Override
	public ICmd run(Domino domino) {
		domino.getOutlerBlocks().addSeparator(separator);
		return new RemoveSeparatorCmd(separator);
	}
}
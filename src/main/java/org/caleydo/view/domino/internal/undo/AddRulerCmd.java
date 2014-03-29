package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Ruler;


public class AddRulerCmd implements ICmd {
	private final Ruler ruler;

	public AddRulerCmd(Ruler ruler) {
		this.ruler = ruler;
	}

	@Override
	public String getLabel() {
		return "Add Ruler: " + ruler;
	}

	@Override
	public ICmd run(Domino rnb) {
		rnb.addRuler(ruler);
		return new RemoveRulerCmd(ruler);
	}
}
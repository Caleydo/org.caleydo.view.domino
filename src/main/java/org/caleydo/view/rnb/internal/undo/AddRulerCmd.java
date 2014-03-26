package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.ui.Ruler;


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
	public ICmd run(RnB domino) {
		domino.addRuler(ruler);
		return new RemoveRulerCmd(ruler);
	}
}
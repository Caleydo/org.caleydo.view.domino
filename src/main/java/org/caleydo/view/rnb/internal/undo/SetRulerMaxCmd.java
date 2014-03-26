package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.ui.Ruler;


public class SetRulerMaxCmd implements ICmd {
	private final Ruler ruler;
	private int max;

	public SetRulerMaxCmd(Ruler ruler, int max) {
		this.ruler = ruler;
		this.max = max;
	}

	@Override
	public String getLabel() {
		return "Set Ruler Max: " + ruler;
	}

	@Override
	public ICmd run(RnB rnb) {
		int bak = ruler.getMaxElements();
		ruler.setMaxElements(max);
		return new SetRulerMaxCmd(ruler, bak);
	}
}
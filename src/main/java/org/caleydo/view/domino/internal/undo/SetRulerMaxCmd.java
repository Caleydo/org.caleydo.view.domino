package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Ruler;


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
	public ICmd run(Domino domino) {
		int bak = ruler.getMaxElements();
		ruler.setMaxElements(max);
		return new SetRulerMaxCmd(ruler, bak);
	}
}
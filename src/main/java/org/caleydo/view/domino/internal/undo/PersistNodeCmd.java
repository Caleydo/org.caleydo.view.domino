/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class PersistNodeCmd implements ICmd {

	private final Node preview;
	private final Node toRemove;

	public PersistNodeCmd(Node preview, Node toRemove) {
		this.preview = preview;
		this.toRemove = toRemove;
	}

	@Override
	public String getLabel() {
		return "Persist Placeholder";
	}

	@Override
	public ICmd run(Domino domino) {
		domino.persistPreview(preview);

		ICmd readd = null;
		if (toRemove != null && domino.containsNode(toRemove))
			readd = new RemoveNodeCmd(toRemove).run(domino);

		return new UndoPersistNodeCmd(readd);
	}

	private class UndoPersistNodeCmd implements ICmd {
		private ICmd readd;

		/**
		 * @param readd
		 */
		public UndoPersistNodeCmd(ICmd readd) {
			this.readd = readd;
		}

		@Override
		public ICmd run(Domino domino) {
			ICmd del = null;
			if (readd != null)
				del = readd.run(domino);
			ICmd add = new RemoveNodeCmd(preview).run(domino);
			if (del == null)
				return add;
			else
				return CmdComposite.chain(add, del);
		}

		@Override
		public String getLabel() {
			return "Remove Node and Placeholder";
		}

	}

}

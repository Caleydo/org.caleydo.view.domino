/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.ArrayDeque;
import java.util.Deque;

import org.caleydo.core.util.base.ICallback;
import org.caleydo.view.domino.internal.undo.ICmd;
import org.caleydo.view.domino.internal.undo.ZoomCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class UndoStack {
	private final Deque<ICmd> undo = new ArrayDeque<>();
	private final Deque<ICmd> redo = new ArrayDeque<>();
	private final Domino domino;
	private ICallback<UndoStack> onChanged;

	public UndoStack(Domino domino) {
		this.domino = domino;
	}

	public void onChanged(ICallback<UndoStack> onChanged) {
		this.onChanged = onChanged;
	}

	public void push(ICmd cmd) {
		ICmd undo = cmd.run(domino);
		if (undo != null) {
			// merge same zoom stuff
			if (undo instanceof ZoomCmd && this.undo.peekLast() instanceof ZoomCmd) {
				ZoomCmd peek = (ZoomCmd) this.undo.peekLast();
				if (!peek.merge((ZoomCmd) undo))
					this.undo.add(undo);
			} else
				this.undo.add(undo);
		}
		this.redo.clear();
		fire();
	}

	/**
	 *
	 */
	private void fire() {
		if (onChanged != null)
			onChanged.on(this);
	}

	public boolean undo() {
		if (this.undo.isEmpty())
			return false;
		ICmd cmd = this.undo.pollLast();
		ICmd redo = cmd.run(domino);
		if (redo != null)
			this.redo.add(redo);
		fire();
		return true;
	}

	public void clearUndo() {
		this.undo.clear();
	}

	public void undoAll() {
		while (!this.undo.isEmpty())
			undo();
	}

	public boolean redo() {
		if (this.redo.isEmpty())
			return false;
		ICmd cmd = this.redo.pollLast();
		ICmd redo = cmd.run(domino);
		if (redo != null)
			this.undo.add(redo);
		fire();
		return true;
	}

	/**
	 * @return
	 */
	public boolean isUndoEmpty() {
		return undo.isEmpty();
	}

	public boolean isRedoEmpty() {
		return this.redo.isEmpty();
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.model;


/**
 * meta data about a perspective
 *
 * @author Samuel Gratzl
 *
 */
public class PerspectiveMetaData {
	public static final int FLAG_CHILD = 1 << 1;
	public static final int FLAG_SPLITTED = 1 << 2;

	private int flags;
	/**
	 *
	 */
	public PerspectiveMetaData(int flags) {
		this.flags = flags;
	}

	private boolean isSet(int flag) {
		return (flags & flag) != 0;
	}

	private void set(int flag) {
		flags |= flag;
	}

	public boolean isChild() {
		return isSet(FLAG_CHILD);
	}

	public void setSplitted() {
		set(FLAG_SPLITTED);
	}

	public boolean isSplitted() {
		return isSet(FLAG_SPLITTED);
	}
}

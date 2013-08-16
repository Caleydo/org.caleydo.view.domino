/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.model;


/**
 * @author Samuel Gratzl
 *
 */
public class TablePerspectiveMetaData {
	private final PerspectiveMetaData record;
	private final PerspectiveMetaData dimension;

	/**
	 * @param mask
	 */
	public TablePerspectiveMetaData(int recordFlags, int dimensionFlags) {
		this.record = new PerspectiveMetaData(recordFlags);
		this.dimension = new PerspectiveMetaData(dimensionFlags);
	}

	/**
	 * @return the record, see {@link #record}
	 */
	public PerspectiveMetaData getRecord() {
		return record;
	}

	/**
	 * @return the dimension, see {@link #dimension}
	 */
	public PerspectiveMetaData getDimension() {
		return dimension;
	}

	public boolean isChild() {
		return record.isChild() || dimension.isChild();
	}
}

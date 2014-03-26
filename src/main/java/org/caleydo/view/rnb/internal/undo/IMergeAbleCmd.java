/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

/**
 * @author Samuel Gratzl
 *
 */
public interface IMergeAbleCmd extends ICmd {
	boolean merge(ICmd cmd);
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public interface ICmd extends ILabeled {
	/**
	 * run the given command return a command to undo the operation
	 *
	 * @return
	 */
	ICmd run(Domino rnb);
}

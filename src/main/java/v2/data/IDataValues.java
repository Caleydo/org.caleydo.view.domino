/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public interface IDataValues extends ILabeled {

	TypedGroupSet getDefaultGroups(EDimension dim);

	int compare(EDimension dim, int a, int b, TypedSet otherData);

}

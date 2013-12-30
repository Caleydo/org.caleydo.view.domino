/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.internal.util.Utils;

/**
 * @author Samuel Gratzl
 *
 */
public class Categorical1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;

	/**
	 * @param t
	 */
	public Categorical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = new TypedGroupSet(Utils.extractSetGroups(p, id.getId(), main));
	}

	@Override
	protected TypedGroupSet getGroups() {
		return groups;
	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public interface IDataValues extends ILabeled {

	TypedGroupSet getDefaultGroups(EDimension dim);

	int compare(EDimension dim, int a, int b, TypedSet otherData);

	/**
	 * @return
	 */
	String getExtensionID();

	/**
	 * @param b
	 * @param dimData
	 * @param recData
	 */
	void fill(Builder b, TypedListGroup dimData, TypedListGroup recData);

	Collection<String> getDefaultVisualization(EProximityMode mode);
}

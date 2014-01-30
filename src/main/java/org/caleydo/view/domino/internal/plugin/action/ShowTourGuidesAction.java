/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.plugin.action;

import org.caleydo.core.gui.SimpleAction;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.tourguide.DataTourGuideAdapter;
import org.caleydo.view.domino.internal.tourguide.StratifiationTourGuideAdapter;

/**
 * @author Samuel Gratzl
 *
 */
public class ShowTourGuidesAction extends SimpleAction {
	/**
	 *
	 */
	public ShowTourGuidesAction() {
		super("Show Tour Guides", "resources/icons/icon_16.png", Resources.getResourceLoader());
	}

	@Override
	public void run() {
		DataTourGuideAdapter.show();
		StratifiationTourGuideAdapter.show();
		// call again to get focus
		DataTourGuideAdapter.show();
	}
}

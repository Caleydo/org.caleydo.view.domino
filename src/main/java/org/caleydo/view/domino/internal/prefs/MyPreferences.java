/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.prefs;

import org.caleydo.view.domino.internal.plugin.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * @author Samuel Gratzl
 *
 */
public class MyPreferences extends AbstractPreferenceInitializer {

	public static IPreferenceStore prefs() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = prefs();
		store.setDefault("autoselect", true);
		store.setDefault("blockLabelInGroup", false);
	}

	public static boolean isAutoSelectItems() {
		return prefs().getBoolean("autoselect");
	}

	/**
	 * @return
	 */
	public static boolean showBlockLabelInGroup() {
		return prefs().getBoolean("blockLabelInGroup");
	}

}

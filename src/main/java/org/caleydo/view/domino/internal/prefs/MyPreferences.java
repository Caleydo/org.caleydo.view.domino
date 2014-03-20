/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.prefs;

import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.internal.plugin.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;


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
		store.setDefault("toolbar.dynamic", true);

		PreferenceConverter.setDefault(store, "numerical.color.min", new RGB(250, 250, 250));
		PreferenceConverter.setDefault(store, "numerical.color.max", new RGB(0, 0, 0));
		store.setDefault("matrix.color.usenumerical", false);
	}

	public static boolean isAutoSelectItems() {
		return prefs().getBoolean("autoselect");
	}

	public static boolean isShowDynamicToolBar() {
		return prefs().getBoolean("toolbar.dynamic");
	}

	public static Color getNumericalMappingMinColor() {
		return getColor("numerical.color.min");
	}

	public static boolean isUseNumericalColorMapping() {
		return prefs().getBoolean("matrix.color.usenumerical");
	}

	public static Color getNumericalMappingMaxColor() {
		return getColor("numerical.color.max");
	}

	private static Color getColor(final String key) {
		RGB color = PreferenceConverter.getColor(prefs(), key);
		return new Color(color.red, color.green, color.blue);
	}

	/**
	 * @return
	 */
	public static boolean showBlockLabelInGroup() {
		return prefs().getBoolean("blockLabelInGroup");
	}

}

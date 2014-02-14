/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.prefs;

import org.caleydo.view.tourguide.api.prefs.MyPreferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
/**
 * @author Samuel Gratzl
 *
 */
public class MyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public MyPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		final Composite parent = getFieldEditorParent();

		addField(new BooleanFieldEditor("autoselect", "Auto Select Items", parent));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MyPreferences.prefs());
		setDescription("Domino settings");
	}
}
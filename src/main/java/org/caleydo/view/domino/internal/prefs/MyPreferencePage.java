/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.prefs;

import java.util.ArrayList;
import java.util.Collection;

import org.caleydo.core.gui.util.FontUtil;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
/**
 * @author Samuel Gratzl
 *
 */
public class MyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private final Collection<Label> labels = new ArrayList<>();

	public MyPreferencePage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		final Composite parent = getFieldEditorParent();

		addField(new BooleanFieldEditor("autoselect", "Auto Select Items", parent));
		addField(new BooleanFieldEditor("blockLabelInGroup", "Show Block Label in Groups", parent));

		addGroup(parent, "Numerical Block");
		addField(new ColorFieldEditor("numerical.color.min", "Color used for encoding 0.f", parent));
		addField(new ColorFieldEditor("numerical.color.max", "Color used for encoding 1.f", parent));

		addGroup(parent, "Matrix Block");
		addField(new BooleanFieldEditor("matrix.color.usenumerical",
				"Use numerical block color mapping instead of inherited one", parent));

	}

	private void addGroup(final Composite parent, String label) {
		Label l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labels.add(l);

		l = new Label(parent, SWT.NONE);
		l.setText(label);
		FontUtil.makeBold(l);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		labels.add(l);
	}

	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		int cols = ((GridLayout) (getFieldEditorParent().getLayout())).numColumns;
		for (Label label : labels)
			((GridData) label.getLayoutData()).horizontalSpan = cols;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MyPreferences.prefs());
		setDescription("Domino settings");
	}
}
/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.util.base.Labels;
import org.caleydo.view.info.dataset.spi.IDataSetItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeDataItem implements IDataSetItem {
	private static volatile NodeDataItem instance;
	private ExpandItem item;
	private StyledText text;

	/**
	 *
	 */
	public NodeDataItem() {
		instance = this;
	}

	@Override
	public ExpandItem create(ExpandBar expandBar) {
		this.item = new ExpandItem(expandBar, SWT.WRAP);
		item.setText("Domino Node Infos");
		item.setExpanded(false);
		Composite c = new Composite(expandBar, SWT.NONE);
		c.setLayout(new GridLayout(1, false));

		text = new StyledText(c, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
		text.setBackgroundMode(SWT.INHERIT_FORCE);
		text.setText("No Selection");
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 60;
		text.setLayoutData(gd);
		text.setEditable(false);
		text.setWordWrap(true);

		// transformationLabel.set
		// transformationLabel.();

		item.setControl(c);
		item.setHeight(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return item;
	}

	/**
	 * @param mouseOvers
	 * @param selections
	 */
	private void updateImpl(String text) {
		this.text.setText(text);
	}

	public static void update(final Set<NodeGroup> mouseOvers, final Set<NodeGroup> selections) {
		final NodeDataItem i = instance;
		if (i == null)
			return;
		Set<NodeGroup> toShow = selections;
		if (!mouseOvers.isEmpty()) {
			toShow = mouseOvers;
		}

		String text;
		if (toShow.isEmpty()) {
			text = "No Selection";
		} else
			text = StringUtils.join(Collections2.transform(toShow, Labels.TO_LABEL), "\n");

		final String t = text;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				i.updateImpl(t);
			}
		});
	}
}

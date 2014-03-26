/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import java.util.Collection;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.Labels;
import org.caleydo.view.info.dataset.spi.IDataSetItem;
import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.api.model.typed.TypedGroupList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;

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
		item.setText("RnB Block Infos");
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
		if (this.text.isDisposed()) {
			instance = null;
			return;
		}
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
		} else {
			Multimap<Node, NodeGroup> nodes = HashMultimap.create();
			for (NodeGroup group : toShow) {
				Node n = group.getNode();
				nodes.put(n, group);
			}
			StringBuilder b = new StringBuilder();
			for (ILabeled node : ImmutableSortedSet.orderedBy(Labels.BY_LABEL).addAll(nodes.keySet()).build()) {
				Node n = (Node) node;
				addNodeInfos(b, n, nodes.get(n));
				b.append('\n');
			}
			b.setLength(b.length() - 1);
			text = b.toString();
		}

		final String t = text;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				i.updateImpl(t);
			}
		});
	}

	/**
	 * @param b
	 * @param n
	 * @param selected
	 */
	private static void addNodeInfos(StringBuilder b, Node n, Collection<NodeGroup> selected) {
		b.append(n.getLabel()).append(" (");
		final TypedGroupList dim = n.getData(EDimension.DIMENSION);
		final TypedGroupList rec = n.getData(EDimension.RECORD);
		if (n.has(EDimension.DIMENSION) && n.has(EDimension.RECORD)) {
			b.append(dim.size()).append('x').append(rec.size()).append(" items)");
			float fD = 100.f / dim.size();
			float fR = 100.f / rec.size();
			if (dim.getGroups().size() > 1 || rec.getGroups().size() > 1) {
				for (NodeGroup g : n.nodeGroups()) {
					int sD = g.getData(EDimension.DIMENSION).size();
					int sR = g.getData(EDimension.RECORD).size();
					b.append("\n\t").append(g.getLabel());
					if (selected.contains(g))
						b.append('*');
					b.append(" (").append(sD).append('x').append(sR);
					b.append(String.format(" %.1f%% x %.1f%%", sD * fD, sR * fR)).append(')');
				}
			}
		} else if (n.has(EDimension.DIMENSION)) {
			b.append(dim.size()).append(" items)");
			addGroupInfos(b, n, selected, dim, EDimension.DIMENSION);
		} else if (n.has(EDimension.RECORD)) {
			b.append(rec.size()).append(" items)");
			addGroupInfos(b, n, selected, rec, EDimension.RECORD);
		}

	}

	static void addGroupInfos(StringBuilder b, Node n, Collection<NodeGroup> selected, final TypedGroupList data,
			EDimension dim) {
		if (data.getGroups().size() <= 1)
			return;
		float f = 100.f / data.size();
		for (NodeGroup g : n.getGroupNeighbors(EDirection.getPrimary(dim.opposite()))) {
			if (g == null)
				continue;
			int s = g.getData(dim).size();
			b.append("\n\t").append(g.getLabel());
			if (selected.contains(g))
				b.append('*');
			b.append(" (").append(s).append(String.format(" %.1f%%", s * f)).append(')');
		}
	}
}

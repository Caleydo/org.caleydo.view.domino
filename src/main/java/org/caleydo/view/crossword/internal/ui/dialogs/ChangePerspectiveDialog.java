/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.view.opengl.layout2.ISWTLayer.ISWTLayerRunnable;
import org.caleydo.view.crossword.internal.event.ChangePerspectiveEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class ChangePerspectiveDialog extends Dialog implements ISWTLayerRunnable, ISelectionChangedListener {

	private final Perspective perspective;
	private final Object receiver;
	private TreeViewer tree;
	/**
	 * @param parentShell
	 */
	public ChangePerspectiveDialog(Perspective perspective, Object receiver) {
		super((Shell) null);
		this.perspective = perspective;
		this.receiver = receiver;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Select a new perspective to show");
		super.configureShell(newShell);
	}

	@Override
	public void run(Display display, Composite canvas) {
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		this.setBlockOnOpen(false);
		this.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		tree = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree.getTree().setLinesVisible(true);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setAutoExpandLevel(2);
		tree.setLabelProvider(Labels.PROVIDER);
		tree.setContentProvider(new ChangePerspectiveContentProvider());
		tree.setInput(resolvePerspectives());
		tree.setSelection(new StructuredSelection(perspective));
		tree.addSelectionChangedListener(this);
		return super.createDialogArea(parent);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ITreeSelection selection = (ITreeSelection)event.getSelection();
		Object selected = selection.getFirstElement();
		if (selected instanceof Perspective) {
			EventPublisher.trigger(new ChangePerspectiveEvent(perspective.getIdType(), (Perspective) selected).to(
					receiver).from(
					this));
		}
	}

	/**
	 * @return
	 */
	private List<DataDomainPerspective> resolvePerspectives() {
		List<DataDomainPerspective> b = new ArrayList<>();
		IDCategory target = perspective.getIdType().getIDCategory();
		IDataDomain original = perspective.getDataDomain();
		for (ATableBasedDataDomain dataDomain : DataDomainManager.get().getDataDomainsByType(
				ATableBasedDataDomain.class)) {
			List<Perspective> ps = new ArrayList<>();
			Table table = dataDomain.getTable();
			if (dataDomain.getDimensionIDCategory().equals(target)) {
				for (String id : table.getDimensionPerspectiveIDs())
					ps.add(table.getDimensionPerspective(id));
			}
			if (dataDomain.getRecordIDCategory().equals(target)) {
				for (String id : table.getRecordPerspectiveIDs())
					ps.add(table.getRecordPerspective(id));
			}
			if (!ps.isEmpty()) {
				Collections.sort(ps, Labels.BY_LABEL);
				DataDomainPerspective d = new DataDomainPerspective(dataDomain, ps);
				if (dataDomain.equals(original))
					b.add(0, d);
				else
					b.add(d);
			}
		}
		return ImmutableList.copyOf(b);
	}

	@Override
	protected void cancelPressed() {
		EventPublisher.trigger(new ChangePerspectiveEvent(perspective.getIdType(), perspective));
		super.cancelPressed();
	}

	private static class ChangePerspectiveContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getElements(Object inputElement) {
			assert inputElement instanceof List<?>;
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof DataDomainPerspective)
				return ((DataDomainPerspective) parentElement).perspectives.toArray();
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Perspective) {
				// TODO
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof DataDomainPerspective)
				return !((DataDomainPerspective) element).isEmpty();
			return false;
		}
	}

	private static class DataDomainPerspective implements ILabeled {
		private final ATableBasedDataDomain dataDomain;
		private final ImmutableList<Perspective> perspectives;

		public DataDomainPerspective(ATableBasedDataDomain dataDomain, List<Perspective> perspectives) {
			this.dataDomain = dataDomain;
			this.perspectives = ImmutableList.copyOf(perspectives);
		}

		public boolean isEmpty() {
			return perspectives.isEmpty();
		}

		@Override
		public String getLabel() {
			return dataDomain.getLabel();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DataDomainPerspective [dataDomain=");
			builder.append(dataDomain);
			builder.append(", perspectives=");
			builder.append(perspectives);
			builder.append("]");
			return builder.toString();
		}

	}
}

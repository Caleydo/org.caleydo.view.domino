/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractList;
import java.util.Objects;

/**
 * a special ro list, which just returns the same value over and over again
 *
 * @author Samuel Gratzl
 *
 */
public class RepeatingList<T> extends AbstractList<T> {
	private final T value;
	private final int size;

	public static <T> RepeatingList<T> repeat(T value, int size) {
		return new RepeatingList<>(value, size);
	}

	public RepeatingList(T value, int size) {
		this.value = value;
		this.size = size;
	}

	@Override
	public T get(int index) {
		return value;
	}

	@Override
	public int indexOf(Object o) {
		if (Objects.equals(o, value))
			return 0;
		return -1;
	}

	@Override
	public boolean contains(Object o) {
		return Objects.equals(o, value);
	}

	@Override
	public int size() {
		return size;
	}

}

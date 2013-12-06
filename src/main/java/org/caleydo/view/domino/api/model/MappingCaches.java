/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author Samuel Gratzl
 *
 */
public class MappingCaches {

	public static LoadingCache<IDType, IIDTypeMapper<Integer, Integer>> create(IDType source, IDType target) {
		return CacheBuilder.newBuilder().build(new MapperCache(source, target));
	}

	public static LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> create() {
		return CacheBuilder.newBuilder().build(new MapperCache2());
	}

	private static final class MapperCache extends CacheLoader<IDType, IIDTypeMapper<Integer, Integer>> {
		private final IDType source;
		private final IDType target;

		private final IDMappingManager manager;

		public MapperCache(IDType source, IDType target) {
			this.source = source;
			this.target = target;
			assert this.source != null ^ this.target != null;
			IDType i = source != null ? source : target;
			manager = IDMappingManagerRegistry.get().getIDMappingManager(i);
		}

		@Override
		public IIDTypeMapper<Integer, Integer> load(IDType key) {
			IDType s = source == null ? key : source;
			IDType t = target == null ? key : target;
			return manager.getIDTypeMapper(s, t);
		}
	}

	private static final class MapperCache2 extends CacheLoader<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> {
		@Override
		public IIDTypeMapper<Integer, Integer> load(Pair<IDType, IDType> key) throws Exception {
			assert key != null;
			IDType source = key.getFirst();
			IDType target = key.getSecond();
			IDMappingManager m = IDMappingManagerRegistry.get().getIDMappingManager(source);
			if (m == null)
				return null;
			return m.getIDTypeMapper(source, target);
		}
	}
}

package org.iungo.context.api;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides a context map between key/value pairs.
 * 
 * The pairs are stored using a ConcurrentMap with the values wrapped in a Value class which allows null values to be stored.
 * 
 * @author dick
 *
 * @param <K>
 * @param <V>
 */
public class Context<K, V> implements Serializable {

	public static class Value<V> {
		
		private final V value;

		public Value(final V value) {
			super();
			this.value = value;
		}
		
		public V getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("%s", (getValue() == null ? null : getValue().getClass() + "\n" + getValue().toString()));
		}
	}
	
	public static class Entry<K ,V> {

		private final K key;
		
		private final V value;

		public Entry(final K key, final V value) {
			super();
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("Key [%s]\nValue [%s]", getKey(), getValue());
		}
	}
	
	public static interface Go<K, V> {
		
		V go(Context<K, V> context);
	}

	public static <K, V> void copy(final Context<K, V> from, final Context<K, V> to) {
		to.entries.putAll(from.entries);
	}
	
	public static <K, V> Context<K, V> mirror(final Context<K, V> original) {
		return new Context<>(original);
	}
	
	private static final long serialVersionUID = 1L;
	
	protected final ConcurrentMap<K, Value<V>> entries;
	
	public Context() {
		this(new ConcurrentHashMap<K, Value<V>>());
	}

	public Context(final Context<K, V> context) {
		this(context.entries);
	}

	public Context(final ConcurrentMap<K, Value<V>> entries) {
		this.entries = entries;
	}
	
	/**
	 * Get the V for the the given K returning null if K is not mapped.
	 * @param k
	 * @return
	 */
	public V get(final K k) {
		final Value<V> v = entries.get(k);
		return (v == null ? null : v.getValue());
	}
	
	public V set(final K k, final V v) {
		return put(k, v);
	}
	
	public V get(K key, Go<K, V> ifNull) {
		final Value<V> value = entries.get(key);
		return (value == null ? ifNull.go(this) : value.getValue());
	}
	
	public V put(final K k, final V v) {
		final Value<V> previous = entries.put(k, new Value<V>(v));
		return (previous == null ? null : previous.getValue());
	}

	public V put(final Entry<K, V> contextEntry) {
		final Value<V> previous = entries.put(contextEntry.getKey(), new Value<V>(contextEntry.getValue()));
		return (previous == null ? null : previous.getValue());
	}
	
	public V putIfAbsent(final K k, final V v) {
		final Value<V> previous = entries.putIfAbsent(k, new Value<V>(v));
		return (previous == null ? null : previous.getValue());
	}
	
	public V remove(final K k) {
		final Value<V> value = entries.remove(k);
		return (value == null ? null : value.getValue());
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(2048);
		result.append(String.format("%s [\n", this.getClass().getName()));
		for (Map.Entry<K, Value<V>> entry : entries.entrySet()) {
			result.append(String.format("Key [%s]\nValue [%s]", entry.getKey(), entry.getValue()));
		}
		result.append("\n]");
		return result.toString();
	}
}

package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A mapping from the cartesian product of a set of arbitrary identifiers to a
 * value.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public class MatrixMapImpl<T> implements MatrixMap<T> {
	List<Map<?, Integer>> maps;
	Object[] matrix;

	public MatrixMapImpl(Collection<? extends Collection<?>> dimensions) {
		if (dimensions.isEmpty()) {
			throw new IllegalArgumentException("Must have at least one dimension");
		}
		if (dimensions.stream().anyMatch(dim -> dim.isEmpty())) {
			throw new IllegalArgumentException("Each dimension must have at least one value");
		}
		maps = dimensions.stream().map(dim -> {
			var map = new LinkedHashMap<Object, Integer>(dim.size());
			int i = 0;
			for (var o : dim) {
				map.put(o, i);
				i++;
			}
			return map;
		}).collect(Collectors.toList());
		var matrixSize = maps.stream().map(Map::size).reduce(1, (x, y) -> x * y);
		matrix = new Object[matrixSize];
	}

	public MatrixMapImpl(Collection<?>... dimensions) {
		this(Arrays.asList(dimensions));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> getM(Object... params) {
		return (Optional<T>) getIndex(params).flatMap(i -> Optional.ofNullable(matrix[i]));
	}

	@Override
	public void putM(T value, Object... params) {
		matrix[getIndex(params).orElseThrow(() -> new IllegalArgumentException())] = value;
	}

	protected Optional<Integer> getIndex(Object... params) {
		if (params.length != maps.size()) {
			throw new IllegalArgumentException("MatrixMap requires parameters to equal the number of dimensions");
		}
		int i = 0;
		int index = 0;
		int step = 1;
		for (var o : params) {
			var dim = maps.get(i);
			Integer dimIndex = dim.get(o);
			if (dimIndex == null) {
				return Optional.empty();
			}
			index += step * dimIndex;

			step *= dim.size();
			i++;
		}
		return Optional.of(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean all(Predicate<T> pred) {
		return Arrays.stream(matrix).allMatch((Predicate<? super Object>) pred);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean any(Predicate<T> pred) {
		return Arrays.stream(matrix).anyMatch((Predicate<? super Object>) pred);
	}

	@Override
	public boolean isFull() {
		return all(x -> x != null);
	}

	@Override
	public boolean isEmpty() {
		return all(x -> x == null);
	}

	@Override
	public List<Set<?>> getDimensions() {
		return maps.stream().map(m -> m.keySet()).collect(Collectors.toList());
	}

	@Override
	public int getNumDimensions() {
		return maps.size();
	}

	@Override
	public void setAll(T value) {
		Arrays.fill(matrix, value);
	}

	@Override
	public void eachKey(Consumer<Object[]> body) {
		var key = new Object[maps.size()];
		eachKey(key, 0, body);
	}

	// Recursively compute the cartesian product
	private void eachKey(Object[] key, int i, Consumer<Object[]> body) {
		if (i < maps.size()) {
			var dim = maps.get(i);
			dim.keySet().forEach(k -> {
				key[i] = k;
				eachKey(key, i + 1, body);
			});
		} else {
			body.accept(Arrays.copyOf(key, i));
		}
	}
}

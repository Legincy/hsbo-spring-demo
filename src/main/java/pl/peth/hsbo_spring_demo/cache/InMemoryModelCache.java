package pl.peth.hsbo_spring_demo.cache;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryModelCache<T> implements ModelCache<T> {
    private final ConcurrentMap<String, T> cache = new ConcurrentHashMap<>();


    @Override
    public void put(String key, T value) {
        cache.put(key, value);
    }

    @Override
    public Optional<T> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Collection<T> getAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    @Override
    public void deleteAll() {
        cache.clear();
    }
}

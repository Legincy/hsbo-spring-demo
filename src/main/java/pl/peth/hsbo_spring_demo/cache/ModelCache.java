package pl.peth.hsbo_spring_demo.cache;

import java.util.Collection;
import java.util.Optional;

public interface ModelCache<T> {
    void put(String key, T value);
    Optional<T> get(String key);
    Collection<T> getAll();
    void delete(String key);
    void deleteAll();
}

package pl.peth.hsbo_spring_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.peth.hsbo_spring_demo.cache.InMemoryModelCache;
import pl.peth.hsbo_spring_demo.cache.ModelCache;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.Wago750Model;

@Configuration
public class CacheConfig {
    @Bean
    public ModelCache<Wago750Model> wago750Cache() {
        return new InMemoryModelCache<>();
    }

    @Bean
    public ModelCache<S7_1500Model> s7_1500ModelModelCache() {
        return new InMemoryModelCache<>();
    }
}

package com.zeeyeh.quyvera.plugin.config;

import com.zeeyeh.quyvera.plugin.sdk.entity.config.PluginMetaLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.dependency.PluginDependencyManager;
import com.zeeyeh.quyvera.plugin.service.plugin.library.MavenLibraryResolver;
import com.zeeyeh.quyvera.plugin.service.plugin.library.PluginLibraryResolver;
import com.zeeyeh.quyvera.plugin.service.plugin.loader.PluginLoader;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.EventManager;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.PluginManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.file.Path;
import java.util.concurrent.Executor;

/**
 * 插件配置
 * @author Qinloren
 * @date 2026/11/15 20:25
 */
@Configuration
public class PluginConfiguration {

    @Bean("pluginAsyncExecutor")
    public Executor pluginAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("plugin-event-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        return executor;
    }

    @Bean
    public EventManager eventManager(@Qualifier("pluginAsyncExecutor") Executor executor) {
        return new EventManager(executor);
    }

    @Bean
    public MavenLibraryResolver libraryResolver(@Value("${app.plugin.maven-repository}") String repository) {
        return new MavenLibraryResolver(Path.of(repository));
    }

    @Bean
    public PluginDependencyManager dependencyManager() {
        return new PluginDependencyManager();
    }

    @Bean
    public PluginLoader pluginLoader(PluginDependencyManager dependencyManager, PluginLibraryResolver pluginLibraryResolver) {
        return new PluginLoader(dependencyManager, pluginLibraryResolver);
    }

    @Bean
    public PluginManager pluginManager(PluginLoader pluginLoader, EventManager eventManager) {
        return new PluginManager(pluginLoader, eventManager);
    }
}

package com.zeeyeh.quyvera.common.factory;

import com.zeeyeh.quyvera.common.config.properties.AppLocaleProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author Qinloren
 * @date 2026/10/20 00:33
 */
@Slf4j
@Component
@Primary
public class LocaleFactory extends AbstractMessageSource implements InitializingBean {
    private final Path localeDir;
    private final String baseName = "locales";
    private volatile Map<Locale, Properties> bundles = new ConcurrentHashMap<>();


    public LocaleFactory(AppLocaleProperties properties) {
        this.localeDir = Paths.get(properties.getDir()).toAbsolutePath().normalize();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Files.createDirectories(localeDir);
        reload();
        startWatch();
    }

    @Override
    protected @Nullable MessageFormat resolveCode(String code, Locale locale) {
        for (Locale item : localeChain(locale)) {
            Properties properties = bundles.get(item);
            if (properties != null && properties.containsKey(code)) {
                return new MessageFormat(properties.getProperty(code), locale);
            }
        }
        return null;
    }

    private List<Locale> localeChain(Locale locale) {
       List<Locale> locales = new ArrayList<>();
       locales.add(locale);
       if (!locale.getLanguage().isEmpty()) {
           locales.add(Locale.of(locale.getLanguage()));
       }
       locales.add(Locale.ROOT);
       return locales;
    }

    public synchronized void reload() {
        Map<Locale, Properties> newBundles = new ConcurrentHashMap<>();
        try(Stream<Path> stream = Files.list(localeDir)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".properties"))
                    .filter(path -> path.getFileName().toString().startsWith(baseName))
                    .forEach(path -> {
                        Locale locale = parseLocale(path.getFileName().toString());
                        Properties properties = new Properties();
                        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                            properties.load(reader);
                            newBundles.put(locale, properties);
                            log.info("加载国际化文件: {} -> {}", path.getFileName(), locale);
                        } catch (IOException e) {
                            log.error("加载国际化文件失败: " + path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("扫描 locales 目录失败: " + localeDir.toFile().getAbsolutePath(), e);
        }
        this.bundles = newBundles;
        log.info("locale 重载完成， 共 {} 个语言版本", newBundles.size());
    }

    private Locale parseLocale(String fileName) {
        String name = fileName.substring(0, fileName.length() - ".properties".length());
        if (name.equals(baseName)) {
            return Locale.ROOT;
        }
        String[] parts = name.substring(baseName.length() + 1).split("_");
        if (parts.length == 1) return Locale.of(parts[0]);
        if (parts.length == 2) return Locale.of(parts[0], parts[1]);
        return Locale.of(parts[0], parts[1], parts[2]);
    }

    private void startWatch() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        localeDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        Thread watcher = new Thread(() -> watchLoop(watchService), "i18n-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private void watchLoop(WatchService watchService) {
        while (true) {
            try {
                WatchKey key = watchService.take();
                boolean changed = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    String name = ((Path) event.context()).getFileName().toString();
                    if (name.startsWith(baseName) && name.endsWith(".properties")) {
                        changed = true;
                    }
                }
                key.reset();
                if (changed) {
                    reload();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

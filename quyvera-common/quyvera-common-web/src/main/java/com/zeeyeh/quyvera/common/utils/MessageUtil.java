package com.zeeyeh.quyvera.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 语言消息工具类
 * @author Qinloren
 * @date 2026/10/20 15:21
 */
public class MessageUtil {

    /**
     * 翻译消息
     * @param messageSource 消息源
     * @param key 消息键
     * @return 翻译后的消息
     */
    public static String translate(MessageSource messageSource, String key) {
        return translate(messageSource, key, new Object[0]);
    }

    /**
     * 翻译消息
     * @param messageSource 消息源
     * @param key 消息键
     * @return 翻译后的消息
     */
    public static String translate(MessageSource messageSource, String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return messageSource.getMessage(key, args, Locale.SIMPLIFIED_CHINESE);
        }
    }
}

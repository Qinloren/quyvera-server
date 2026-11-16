package com.zeeyeh.quyvera.plugin.sdk.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zeeyeh.quyvera.plugin.sdk.exceptions.InvalidPluginException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 插件元数据
 * @author Qinloren
 * @date 2026/10/25 18:25
 */
@Getter
@AllArgsConstructor
public class PluginMeta {
    /**
     * 插件Id
     */
    private String name;
    /**
     * 插件主类路径
     */
    private String main;

    /**
     * 插件版本
     */
    private SemanticVersion version;

    /**
     * 插件官网
     */
    private String website;

    /**
     * 强依赖
     */
    private List<String> depend = new ArrayList<>();

    /**
     * 可选依赖
     */
    private List<String> softDepend = new ArrayList<>();

    /**
     * 作者列表
     */
    private List<String> authors = new ArrayList<>();

    /**
     * 插件的第三方依赖
     */
    private List<String> libs = new ArrayList<>();

    public PluginMeta(byte[] data) {
        load(data);
    }

    public PluginMeta(String json) {
        this(json == null ? null : json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析并校验插件元数据，任何不合法都会直接抛出异常，
     * 不再吞掉异常留下一个字段为 null 的半成品对象。
     * @param data plugin.json 字节内容
     */
    private void load(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidPluginException("插件元数据为空");
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(data);
        } catch (Exception e) {
            throw new InvalidPluginException("插件元数据不是合法的 JSON", e);
        }
        if (jsonObject == null) {
            throw new InvalidPluginException("插件元数据不是合法的 JSON 对象");
        }

        this.name = jsonObject.getString("name");
        this.main = jsonObject.getString("main");
        this.version = SemanticVersion.parse(jsonObject.getString("version"));
        this.website = jsonObject.getString("website");
        this.depend = readStringList(jsonObject, "depend");
        this.softDepend = readStringList(jsonObject, "softDepend");
        this.libs = readStringList(jsonObject, "libs");
        this.authors = readStringList(jsonObject, "authors");

        if (StringUtils.isBlank(this.name)) {
            throw new InvalidPluginException("插件元数据缺少 name 字段");
        }
        if (!PluginNames.isValid(this.name)) {
            throw new InvalidPluginException("[插件系统] 非法插件名: " + this.name + "，要求：" + PluginNames.RULE);
        }
        if (StringUtils.isBlank(this.main)) {
            throw new InvalidPluginException("插件 [" + this.name + "] 元数据缺少 main 字段");
        }
        if (this.version == null) {
            throw new InvalidPluginException("插件 [" + this.name + "] 元数据缺少合法的 version 字段");
        }
    }

    /**
     * 读取字符串数组字段，同时兼容单个字符串写法
     * @param jsonObject 元数据对象
     * @param key 字段名
     * @return 字符串列表，字段不存在时返回空列表
     */
    private static List<String> readStringList(JSONObject jsonObject, String key) {
        if (!jsonObject.containsKey(key)) {
            return new ArrayList<>();
        }
        Object value = jsonObject.get(key);
        switch (value) {
            case null -> {
                return new ArrayList<>();
            }
            case String stringValue -> {
                if (StringUtils.isBlank(stringValue)) {
                    return new ArrayList<>();
                }
                List<String> single = new ArrayList<>(1);
                single.add(stringValue);
                return single;
            }
            case Collection<?> collection -> {
                List<String> result = new ArrayList<>(collection.size());
                for (Object element : collection) {
                    if (element != null) {
                        result.add(String.valueOf(element));
                    }
                }
                return result;
            }
            default -> {
            }
        }
        throw new InvalidPluginException("插件元数据字段 [" + key + "] 必须是字符串或字符串数组");
    }

    public String getFullName() {
        return this.name + "(" + this.version + ")";
    }
}

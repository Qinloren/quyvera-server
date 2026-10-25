package com.zeeyeh.quyvera.plugin.sdk.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件元数据
 * @author Qinloren
 * @date 2026/10/25 18:25
 */
@Getter
@AllArgsConstructor
@Slf4j
public class PluginMeta {
    private String name;
    private String main;
    private SemanticVersion version;
    private String website;
    private List<String> depend = new ArrayList<>();
    private List<String> softDepend =  new ArrayList<>();
    private List<String> authors =  new ArrayList<>();
    private List<String> libs =  new ArrayList<>();

    public PluginMeta(byte[] data) {
        load(data);
    }

    private void load(byte[] data) {
        try {
            JSONObject jsonObject = JSON.parseObject(data);
            this.name = jsonObject.getString("name");
            this.main = jsonObject.getString("main");
            this.version = SemanticVersion.parse(jsonObject.getString("version"));
            this.website = jsonObject.getString("website");
            this.depend = jsonObject.containsKey("depend") ? jsonObject.getJSONArray("depend").toJavaList(String.class) : new ArrayList<>();
            this.softDepend = jsonObject.containsKey("softDepend") ? jsonObject.getJSONArray("softDepend").toJavaList(String.class) : new ArrayList<>();
            this.libs = jsonObject.containsKey("libs") ? jsonObject.getJSONArray("libs").toJavaList(String.class) : new ArrayList<>();
            this.authors = jsonObject.isArray("authors") ? jsonObject.getJSONArray("authors").toJavaList(String.class) : List.of(jsonObject.getString("authors"));
        } catch (Exception e) {
            log.error("插件元数据解析失败: {}", e.getMessage());
        }
    }

    public String getFullName() {
        return this.name + "(" + this.version + ")";
    }
}

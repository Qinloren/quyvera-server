package com.zeeyeh.quyvera.plugin.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 插件表
 */
@TableName(value ="qy_plugin")
@Data
public class Plugin implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 插件Id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 插件编码
     */
    @TableField(value = "plugin_code")
    private String pluginCode;

    /**
     * 插件名称
     */
    @TableField(value = "plugin_name")
    private String pluginName;

    /**
     * 存储文件名称
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private String version;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 作者
     */
    @TableField(value = "author")
    private String author;

    /**
     * 是否启用：0=禁用，1=启用
     */
    @TableField(value = "is_enabled")
    private Boolean isEnabled;

    /**
     * 安装时间
     */
    @TableField(value = "install_time")
    private LocalDateTime installTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Plugin other = (Plugin) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPluginCode() == null ? other.getPluginCode() == null : this.getPluginCode().equals(other.getPluginCode()))
            && (this.getPluginName() == null ? other.getPluginName() == null : this.getPluginName().equals(other.getPluginName()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getAuthor() == null ? other.getAuthor() == null : this.getAuthor().equals(other.getAuthor()))
            && (this.getIsEnabled() == null ? other.getIsEnabled() == null : this.getIsEnabled().equals(other.getIsEnabled()))
            && (this.getInstallTime() == null ? other.getInstallTime() == null : this.getInstallTime().equals(other.getInstallTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPluginCode() == null) ? 0 : getPluginCode().hashCode());
        result = prime * result + ((getPluginName() == null) ? 0 : getPluginName().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getAuthor() == null) ? 0 : getAuthor().hashCode());
        result = prime * result + ((getIsEnabled() == null) ? 0 : getIsEnabled().hashCode());
        result = prime * result + ((getInstallTime() == null) ? 0 : getInstallTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", pluginCode=" + pluginCode +
                ", pluginName=" + pluginName +
                ", version=" + version +
                ", description=" + description +
                ", author=" + author +
                ", isEnabled=" + isEnabled +
                ", installTime=" + installTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", serialVersionUID=" + serialVersionUID +
                "]";
    }
}
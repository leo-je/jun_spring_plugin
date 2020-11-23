package com.diboot.iam.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.diboot.core.entity.BaseExtEntity;
import com.diboot.iam.config.Cons;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 认证用户 Entity定义
 * @author Wujun
 * @version 2.0
 * @date 2019-12-03
 */
@Getter @Setter @Accessors(chain = true)
public class IamAccount extends BaseExtEntity {
    private static final long serialVersionUID = -6825516429612507644L;

    // 用户类型
    @NotNull(message = "用户类型不能为空")
    @Length(max = 100, message = "用户类型长度应小于100")
    @TableField()
    private String userType;

    // 用户ID
    @NotNull(message = "用户ID不能为空")
    @TableField()
    private Long userId;

    // 认证方式
    @NotNull(message = "认证方式不能为空")
    @Length(max = 20, message = "认证方式长度应小于20")
    @TableField()
    private String authType = Cons.DICTCODE_AUTH_TYPE.PWD.name();

    // 用户名
    @NotNull(message = "用户名不能为空")
    @Length(max = 100, message = "用户名长度应小于100")
    @TableField()
    private String authAccount;

    // 密码
    @JSONField(serialize = false)
    @Length(max = 32, message = "密码长度应小于32")
    @TableField()
    private String authSecret;

    // 加密盐
    @JSONField(serialize = false)
    @Length(max = 32, message = "加密盐长度应小于32")
    @TableField()
    private String secretSalt;

    // 加密盐
    @Length(max = 10, message = "状态长度应小于10")
    @TableField()
    private String status;
}

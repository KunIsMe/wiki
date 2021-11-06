package com.zhangkun.wiki.req;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class UserLoginReq {

    @NotEmpty(message = "【用户名】不能为空")
    private String loginName;

    @NotEmpty(message = "【密码】不能为空")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,32}$", message = "【密码】格式错误")
    private String password;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLoginReq{" +
                "loginName='" + loginName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
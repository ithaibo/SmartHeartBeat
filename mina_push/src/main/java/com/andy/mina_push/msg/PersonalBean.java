package com.andy.mina_push.msg;

import java.io.Serializable;

/**
 * Created by Andy on 2017/5/4.
 */

public class PersonalBean implements Serializable {
    private String name;
    private String mobile;

    public PersonalBean(String name, String mobile) {
        this.name = name;
        this.mobile = mobile;
    }

    public PersonalBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "PersonalBean{" +
                "name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}

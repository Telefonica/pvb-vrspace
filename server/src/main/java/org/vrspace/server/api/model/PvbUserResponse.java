package org.vrspace.server.api.model;

import java.io.Serializable;

public class PvbUserResponse implements Serializable {
    private Integer id;

    private String email;

    private String full_name;

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String name) {
        this.full_name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

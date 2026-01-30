package com.example.webapp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Data
public class StudentDTO {

    private String name;

    private String roll;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }
}

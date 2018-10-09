package com.andeqa.andeqa.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryOptions {
    String user_id;
    String option_id;
    String type;
    List<String> one = new ArrayList<>();
    List<String> two = new ArrayList<>();

    public QueryOptions() {
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getOption_id() {
        return option_id;
    }

    public void setOption_id(String option_id) {
        this.option_id = option_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getOne() {
        return one;
    }

    public void setOne(List<String> one) {
        this.one = one;
    }

    public List<String> getTwo() {
        return two;
    }

    public void setTwo(List<String> two) {
        this.two = two;
    }
}

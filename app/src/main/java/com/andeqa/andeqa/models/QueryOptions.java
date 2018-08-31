package com.andeqa.andeqa.models;

public class QueryOptions {
    String user_id;
    String query_option;
    String option_id;

    public QueryOptions() {
    }

    public String getUser_id() {
        return user_id;
    }


    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getQuery_option() {
        return query_option;
    }

    public void setQuery_option(String query_option) {
        this.query_option = query_option;
    }

    public String getOption_id() {
        return option_id;
    }

    public void setOption_id(String option_id) {
        this.option_id = option_id;
    }
}

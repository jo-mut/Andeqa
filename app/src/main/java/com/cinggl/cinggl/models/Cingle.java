package com.cinggl.cinggl.models;

import java.util.ArrayList;

/**
 * Created by J.EL on 4/8/2017.
 */

public class Cingle {

    String title;
    int viewsCount;
    int cingleHashtagWorth;
    String cscs;
    String accountUserName;
    String profileImageUrl;
    String cingleImageUrl;


    int likesCount;
    int commentsCount;
    int cingleWorth;


    String description;
    String slash;
    String tradeMethod;

    public Cingle(String title, int viewsCount, int cingleHashtagWorth,
                  String cscs, String accountUserName, String profileImageUrl,
                  String cingleImageUrl, int likesCount, int commentsCount,
                  int cingleWorth, String description, String slash,
                  String tradeMethod) {
        this.title = title;
        this.viewsCount = viewsCount;
        this.cingleHashtagWorth = cingleHashtagWorth;
        this.cscs = cscs;
        this.accountUserName = accountUserName;
        this.profileImageUrl = profileImageUrl;
        this.cingleImageUrl = cingleImageUrl;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.cingleWorth = cingleWorth;
        this.description = description;
        this.slash = slash;
        this.tradeMethod = tradeMethod;
    }

    public String getTitle() {
        return title;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public int getCingleHashtagWorth() {
        return cingleHashtagWorth;
    }

    public String getCscs() {
        return cscs;
    }

    public String getAccountUserName() {
        return accountUserName;
    }

    public String getCingleImageUrl() {
        return cingleImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public int getCingleWorth() {
        return cingleWorth;
    }

    public String getDescription() {
        return description;
    }

    public String getSlash() {
        return slash;
    }

    public String getTradeMethod() {
        return tradeMethod;
    }
}

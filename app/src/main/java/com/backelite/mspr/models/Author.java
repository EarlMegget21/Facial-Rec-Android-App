package com.backelite.mspr.models;

import com.google.gson.annotations.SerializedName;

public class Author {

    @SerializedName("screen_name") //soit même nom que l'attribut du JSON soit autre nom mais on fait le mapping avec ça
    private String name;
    @SerializedName("name")
    private String login;
    @SerializedName("profile_image_url_https")
    private String image;

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getLogin() {
        return login;
    }
}

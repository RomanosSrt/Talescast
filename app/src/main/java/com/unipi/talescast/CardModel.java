package com.unipi.talescast;

import java.io.Serializable;

public class CardModel implements Serializable {
    public String title;
    public String description;
    public String image;

    public CardModel() {}

    public CardModel(String title, String description, String image) {
        this.title = title;
        this.description = description;
        this.image = image;
    }


}

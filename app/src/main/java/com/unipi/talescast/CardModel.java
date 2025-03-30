package com.unipi.talescast;

import java.io.Serializable;

public class CardModel implements Serializable {
    public String title;
    public String year;
    public String image;
    public String story;


    public CardModel() {}

    public CardModel(String title, String year, String image, String story) {
        this.title = title;
        this.year = year;
        this.image = image;
        this.story = story;
    }


}

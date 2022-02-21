package com.jawaadianinc.valorant_stats.valorant;

import androidx.annotation.NonNull;

public class PlayerClass {
    public String name;
    public String tag;

    public PlayerClass(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    @NonNull
    @Override
    public String toString() {
        return name + "#" + tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}

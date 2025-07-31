package de.zuellich.meal_planner.pinterest.datatypes;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 */
public class Board {

    @Nullable()
    private String id = "";

    @Nullable()
    private String name = "";

    @Nullable()
    private String url = "";

    @Nullable()
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable()
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable()
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Board board = (Board) o;
        return Objects.equals(getId(), board.getId()) && Objects.equals(getName(), board.getName()) && Objects.equals(getUrl(), board.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getUrl());
    }

    @Override
    @Nullable()
    public String toString() {
        return "Board{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", url='" + url + '\'' + '}';
    }
}

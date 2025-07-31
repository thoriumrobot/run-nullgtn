package de.zuellich.meal_planner.pinterest.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PinList {

    @JsonProperty(value = "data")
    @Nullable
    private List<Pin> pins = ImmutableList.of();

    @JsonProperty(value = "page")
    @Nullable
    private PagingInformation page = new PagingInformation();

    public List<Pin> getPins() {
        return pins;
    }

    public void setPins(List<Pin> pins) {
        this.pins = pins;
    }

    public PagingInformation getPage() {
        return page;
    }

    public void setPage(PagingInformation page) {
        this.page = page;
    }
}

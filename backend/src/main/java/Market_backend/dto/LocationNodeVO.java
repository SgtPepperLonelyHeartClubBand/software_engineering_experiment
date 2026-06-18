package Market_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public class LocationNodeVO {

    private Long id;
    private String text;
    private String value;
    private Integer level;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LocationNodeVO> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<LocationNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<LocationNodeVO> children) {
        this.children = children;
    }
}

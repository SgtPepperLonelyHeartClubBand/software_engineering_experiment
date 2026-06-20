package Market_backend.dto;

import java.util.List;

public class ItemDetailVO extends ItemListVO {

    private String description;
    private List<String> images;
    private Long sellerId;
    private Integer viewCount;
    private Boolean favorited;
    private Boolean reservedByMe;
    private Long activeOrderId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public Boolean getReservedByMe() {
        return reservedByMe;
    }

    public void setReservedByMe(Boolean reservedByMe) {
        this.reservedByMe = reservedByMe;
    }

    public Long getActiveOrderId() {
        return activeOrderId;
    }

    public void setActiveOrderId(Long activeOrderId) {
        this.activeOrderId = activeOrderId;
    }
}

package Market_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class ItemRequest {

    @NotBlank(message = "请填写商品标题")
    @Size(max = 40, message = "商品标题最多 40 个字符")
    private String title;

    @NotBlank(message = "请选择商品分类")
    @Size(max = 32, message = "商品分类最多 32 个字符")
    private String category;

    @NotBlank(message = "请选择成色描述")
    @Size(max = 32, message = "成色描述最多 32 个字符")
    private String condition;

    @NotNull(message = "请输入出售价格")
    @DecimalMin(value = "0.01", message = "请输入有效的出售价格")
    @Digits(integer = 8, fraction = 2, message = "价格最多 8 位整数和 2 位小数")
    private BigDecimal price;

    @NotBlank(message = "请选择面交地点")
    private String locationCode;

    @Size(max = 500, message = "详细描述最多 500 个字符")
    private String description;

    @NotEmpty(message = "请至少上传一张商品图片")
    @Size(max = 6, message = "最多上传 6 张商品图片")
    private List<@NotBlank(message = "图片地址不能为空") String> imageUrls;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}

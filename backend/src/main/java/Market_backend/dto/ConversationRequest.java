package Market_backend.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationRequest {

    @NotNull(message = "请选择关联商品")
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}

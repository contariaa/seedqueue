package me.contaria.seedqueue.gui.config;

import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SeedQueueKeyEntryWidget extends AbstractParentElement implements Drawable {
    private final SeedQueueKeybindingsListWidget.KeyEntry entry;
    private final List<Element> children;
    private final SeedQueueKeyButtonWidget keyButton;
    private final ButtonWidget clearButton;

    public SeedQueueKeyEntryWidget(SeedQueueKeybindingsListWidget.KeyEntry entry) {
        this(entry, SeedQueueKeyButtonWidget.UNKNOWN_KEY);
    }

    public SeedQueueKeyEntryWidget(SeedQueueKeybindingsListWidget.KeyEntry entry, Text message) {
        super();
        this.entry = entry;
        this.children = new ArrayList<>();
        this.keyButton = new SeedQueueKeyButtonWidget(entry, this, message);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        this.clearButton = new ButtonWidget(0, 0, textRenderer.getWidth("x") + 3, textRenderer.fontHeight + 1, TextUtil.literal("x"), b -> entry.clearKey(this)) {
            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                textRenderer.draw(matrices, "x", this.x, this.y + 1, 0xFFFFFF);
            }
        };
        this.clearButton.visible = false;
        this.children.add(this.clearButton);
        this.children.add(this.keyButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.entry.isSelected(this)) {
            Text message = this.keyButton.getMessage();
            this.keyButton.setMessage(TextUtil.literal("> ").append(message.shallowCopy()).append(" <").formatted(Formatting.YELLOW));
            this.keyButton.render(matrices, mouseX, mouseY, delta);
            this.keyButton.setMessage(message);
        } else {
            this.keyButton.render(matrices, mouseX, mouseY, delta);
        }
        this.clearButton.render(matrices, mouseX, mouseY, delta);
    }

    public void update(int x, int y, int mouseX, int mouseY) {
        this.keyButton.x = x;
        this.keyButton.y = y;
        this.clearButton.x = x + this.keyButton.getWidth() - this.clearButton.getWidth();
        this.clearButton.y = y;
        this.clearButton.visible = this.keyButton.isMouseOver(mouseX, mouseY) && !this.keyButton.getMessage().equals(SeedQueueKeyButtonWidget.UNKNOWN_KEY) && !this.entry.isSelected(this);
    }

    public void updateMessage(Text message) {
        this.keyButton.setMessage(message);
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }
}

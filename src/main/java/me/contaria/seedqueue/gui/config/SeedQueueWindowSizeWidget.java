package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.SeedQueueConfig;
import me.contaria.speedrunapi.config.api.gui.SpeedrunWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.PagedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class SeedQueueWindowSizeWidget implements SpeedrunWidget {
    private final SeedQueueConfig.WindowSize windowSize;
    private final TextFieldWidget widthWidget;
    private final TextFieldWidget heightWidget;

    private final int width;
    private final int height;
    private int x;
    private int y;

    public SeedQueueWindowSizeWidget(SeedQueueConfig.WindowSize windowSize) {
        this.windowSize = windowSize;
        this.widthWidget = new TextFieldWidget(2, MinecraftClient.getInstance().textRenderer, 0, 0, 65, 20);
        this.widthWidget.setText(String.valueOf(this.windowSize.width()));
        this.widthWidget.setListener(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {
            }

            @Override
            public void setFloatValue(int id, float value) {
            }

            @Override
            public void setStringValue(int id, String text) {
                SeedQueueConfig.WindowSize windowSize = SeedQueueWindowSizeWidget.this.windowSize;
                if (text.isEmpty()) {
                    windowSize.setWidth(0);
                    return;
                }
                windowSize.setWidth(Integer.parseUnsignedInt(text));
                String newText = String.valueOf(windowSize.width());
                if (!text.equals(newText)) {
                    SeedQueueWindowSizeWidget.this.widthWidget.setText(newText);
                }
            }
        });
        this.widthWidget.setTextPredicate(text -> {
            try {
                return text.isEmpty() || Integer.parseUnsignedInt(text) >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.heightWidget = new TextFieldWidget(2, MinecraftClient.getInstance().textRenderer, 0, 0, 65, 20);
        this.heightWidget.setText(String.valueOf(this.windowSize.height()));
        this.heightWidget.setListener(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {
            }

            @Override
            public void setFloatValue(int id, float value) {
            }

            @Override
            public void setStringValue(int id, String text) {
                SeedQueueConfig.WindowSize windowSize = SeedQueueWindowSizeWidget.this.windowSize;
                if (text.isEmpty()) {
                    windowSize.setHeight(0);
                    return;
                }
                windowSize.setHeight(Integer.parseUnsignedInt(text));
                String newText = String.valueOf(windowSize.height());
                if (!text.equals(newText)) {
                    SeedQueueWindowSizeWidget.this.heightWidget.setText(newText);
                }
            }
        });
        this.heightWidget.setTextPredicate(text -> {
            try {
                return text.isEmpty() || Integer.parseUnsignedInt(text) >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.width = 150;
        this.height = 20;
        this.x = 0;
        this.y = 0;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        this.widthWidget.x = this.x;
        this.widthWidget.y = this.y;
        this.widthWidget.render();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        textRenderer.drawWithShadow("X", this.x + (this.width - textRenderer.getStringWidth("X")) / 2.0f, this.y, 0xFFFFFF);
        this.heightWidget.x = this.x + 85;
        this.heightWidget.y = this.y;
        this.heightWidget.render();
    }

    @Override
    public boolean keyPressed(char id, int code) {
        this.widthWidget.keyPressed(id, code);
        this.heightWidget.keyPressed(id, code);
        return true;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.widthWidget.mouseClicked(mouseX, mouseY, button);
        this.heightWidget.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public void tick() {
        this.widthWidget.tick();
        this.heightWidget.tick();
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}

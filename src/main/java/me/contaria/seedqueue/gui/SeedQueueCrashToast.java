package me.contaria.seedqueue.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

import java.util.List;

public class SeedQueueCrashToast implements Toast {
    private final String title;
    private final List<String> description;
    private boolean hasStarted;
    private long startTime = Long.MAX_VALUE;

    public SeedQueueCrashToast(String title, String description) {
        this.title = title;
        this.description = MinecraftClient.getInstance().textRenderer.wrapStringToWidthAsList(description, this.getWidth() - 7);
    }

    @Override
    public Visibility draw(ToastManager manager, long startTime) {
        if (!this.hasStarted && manager.getGame().isWindowFocused()) {
            this.startTime = startTime;
            this.hasStarted = true;
        }

        manager.getGame().getTextureManager().bindTexture(TOASTS_TEX);
        if (this.description.size() < 2) {
            manager.blit(0, 0, 0, 0, this.getWidth(), this.getHeight());
        } else {
            manager.blit(0, 0, 0, 0, this.getWidth(), 11);
            int y = 8;
            for (int i = 0; i < this.description.size(); i++) {
                manager.blit(0, y, 0, 11, this.getWidth(), 10);
                y += 10;
            }
            manager.blit(0, y, 0, 21, this.getWidth(), 11);
        }

        manager.getGame().textRenderer.draw(this.title, 7.0f, 7.0f, 0xFFFF00 | 0xFF000000);

        float y = 18.0f;
        for (String description : this.description) {
            manager.getGame().textRenderer.draw(description, 7.0f, y, -1);
            y += 10.0f;
        }

        return startTime - this.startTime < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    public int getWidth() {
        return 160;
    }

    public int getHeight() {
        return 32 + Math.max(1, this.description.size() - 1) * 10;
    }
}

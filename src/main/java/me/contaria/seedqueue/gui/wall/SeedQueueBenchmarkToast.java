package me.contaria.seedqueue.gui.wall;

import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.List;

public class SeedQueueBenchmarkToast implements Toast {
    private final SeedQueueWallScreen wall;
    private final Text title;

    private boolean finished;
    private boolean fadeOut;

    public SeedQueueBenchmarkToast(SeedQueueWallScreen wall) {
        this.wall = wall;
        this.title = TextUtil.translatable("seedqueue.menu.benchmark.title");
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        this.finished |= !this.wall.isBenchmarking();

        if (this.finished && !this.fadeOut && !this.wall.showFinishedBenchmarkResults) {
            this.fadeOut = true;
        }

        double time = (this.finished ? this.wall.benchmarkFinish : Util.getMeasuringTimeMs()) - this.wall.benchmarkStart;
        double rps = Math.round(this.wall.benchmarkedSeeds / (time / 10000.0)) / 10.0;

        TextRenderer textRenderer = manager.getGame().textRenderer;

        StringRenderable full = TextUtil.translatable(
            "seedqueue.menu.benchmark.result",
            this.wall.benchmarkedSeeds,
            Math.round(time / 1000.0),
            rps
        );

        List<StringRenderable> lines = textRenderer.wrapLines(full, this.getWidth() - 12);

        manager.getGame().getTextureManager().bindTexture(TOASTS_TEX);

        // Top of the toast texture
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), 16);

        // Center
        int textureY = 16;
        for (int i = 0; i < lines.size(); i++) {
            manager.drawTexture(matrices, 0, textureY, 0, 16, this.getWidth(), textRenderer.fontHeight + 2);
            textureY += textRenderer.fontHeight + 2;
        }

        // Bottom
        manager.drawTexture(matrices, 0, textureY, 0, 28, this.getWidth(), 4);

        textRenderer.draw(matrices, this.title, 7.0f, 7.0f, 0xFFFF00 | 0xFF000000);

        float currentY = 18.0f;
        for (StringRenderable line : lines) {
            textRenderer.draw(matrices, line, 7.0f, currentY, -1);
            currentY += textRenderer.fontHeight + 2;
        }

        return this.fadeOut ? Visibility.HIDE : Visibility.SHOW;
    }
}

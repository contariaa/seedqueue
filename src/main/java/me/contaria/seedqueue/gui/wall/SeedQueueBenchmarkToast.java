package me.contaria.seedqueue.gui.wall;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.Util;

public class SeedQueueBenchmarkToast implements Toast {
    private final SeedQueueWallScreen wall;
    private final String title;

    private boolean finished;
    private boolean fadeOut;

    public SeedQueueBenchmarkToast(SeedQueueWallScreen wall) {
        this.wall = wall;
        this.title = I18n.translate("seedqueue.menu.benchmark.title");
    }

    @Override
    public Visibility draw(ToastManager manager, long startTime) {
        manager.getGame().getTextureManager().bindTexture(TOASTS_TEX);
        manager.blit(0, 0, 0, 0, this.getWidth(), this.getHeight());
        manager.getGame().textRenderer.draw(this.title, 7.0f, 7.0f, 0xFFFF00 | 0xFF000000);

        this.finished |= !this.wall.isBenchmarking();

        if (this.finished && !this.fadeOut && !this.wall.showFinishedBenchmarkResults) {
            this.fadeOut = true;
        }

        double time = (this.finished ? this.wall.benchmarkFinish : Util.getMeasuringTimeMs()) - this.wall.benchmarkStart;
        double rps = Math.round(this.wall.benchmarkedSeeds / (time / 10000.0)) / 10.0;
        manager.getGame().textRenderer.draw(I18n.translate("seedqueue.menu.benchmark.result", this.wall.benchmarkedSeeds, Math.round(time / 1000.0), rps), 7.0f, 18.0f, -1);

        return this.fadeOut ? Visibility.HIDE : Visibility.SHOW;
    }

    public int getWidth() {
        return 160;
    }

    public int getHeight() {
        return 32;
    }
}

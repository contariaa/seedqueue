package me.contaria.seedqueue.gui.config;

import me.contaria.speedrunapi.config.api.gui.ButtonWidgetCallback;
import net.minecraft.client.gui.widget.ButtonWidget;

public class SeedQueueKeyButtonWidget extends ButtonWidget implements ButtonWidgetCallback {
    private final SeedQueueKeybindingsListWidget.KeyEntry entry;

    public SeedQueueKeyButtonWidget(SeedQueueKeybindingsListWidget.KeyEntry entry) {
        this(entry, "");
    }

    public SeedQueueKeyButtonWidget(SeedQueueKeybindingsListWidget.KeyEntry entry, String message) {
        super(-1, 0, 0, 75, 20, message);
        this.entry = entry;
    }

    @Override
    public void onPress() {
        this.entry.selectButton(this);
    }
}

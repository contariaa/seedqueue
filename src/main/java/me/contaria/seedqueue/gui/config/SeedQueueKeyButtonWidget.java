package me.contaria.seedqueue.gui.config;

import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;

public class SeedQueueKeyButtonWidget extends AbstractPressableButtonWidget {
    private static final String UNKNOWN_KEY = I18n.translate(InputUtil.UNKNOWN_KEYCODE.getName());

    private final SeedQueueKeybindingsListWidget.KeyEntry entry;

    public SeedQueueKeyButtonWidget(SeedQueueKeybindingsListWidget.KeyEntry entry) {
        this(entry, UNKNOWN_KEY);
    }

    public SeedQueueKeyButtonWidget(SeedQueueKeybindingsListWidget.KeyEntry entry, String message) {
        super(0, 0, 75, 20, message);
        this.entry = entry;
    }

    @Override
    public void onPress() {
        this.entry.selectButton(this);
    }

    @Override
    protected String getNarrationMessage() {
        if (UNKNOWN_KEY.equals(this.getMessage())) {
            return I18n.translate("narrator.controls.unbound", this.entry.title);
        }
        return I18n.translate("narrator.controls.bound", this.entry.title, this.getMessage());
    }
}

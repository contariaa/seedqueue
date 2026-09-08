package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.Nullable;

public class SeedQueueKeybindingsScreen extends Screen {
    private final Screen parent;
    private final String title;
    protected final SeedQueueMultiKeyBinding[] keyBindings;
    protected SeedQueueKeybindingsListWidget.KeyEntry focusedBinding;
    private SeedQueueKeybindingsListWidget keyBindingListWidget;

    @Nullable
    protected String tooltip;

    public SeedQueueKeybindingsScreen(Screen parent, SeedQueueMultiKeyBinding... keyBindings) {
        this.parent = parent;
        this.title = I18n.translate("seedqueue.menu.keys");
        this.keyBindings = keyBindings;
    }

    @Override
    public void init() {
        this.keyBindingListWidget = new SeedQueueKeybindingsListWidget(this, this.client);
        this.buttons.add(new ButtonWidget(1, this.width / 2 - 100, this.height - 27, 200, 20, I18n.translate("gui.done")));
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        this.keyBindingListWidget.handleMouse();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.focusedBinding != null) {
            this.focusedBinding.pressKey(button - 100);
            this.focusedBinding = null;
            return;
        }
        super.mouseClicked(mouseX, mouseY, button);
        this.keyBindingListWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        this.keyBindingListWidget.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(char id, int code) {
        if (this.focusedBinding != null) {
            this.focusedBinding.pressKey(code == 1 ? 0 : code != 0 ? code : id + 256);
            this.focusedBinding = null;
            return;
        }
        if (code == 1) {
            this.onClose();
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 1) {
            this.onClose();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.keyBindingListWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.client.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
        if (this.tooltip != null) {
            this.renderTooltip(this.client.textRenderer.wrapLines(this.tooltip, 200), mouseX, mouseY);
            this.tooltip = null;
        }
    }

    public void onClose() {
        this.client.setScreen(this.parent);
    }
}

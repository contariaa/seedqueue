package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SeedQueueKeybindingsScreen extends Screen {
    private final Screen parent;
    protected final SeedQueueMultiKeyBinding[] keyBindings;
    protected SeedQueueKeybindingsListWidget.KeyEntry focusedBinding;
    private SeedQueueKeybindingsListWidget keyBindingListWidget;

    public SeedQueueKeybindingsScreen(Screen parent, SeedQueueMultiKeyBinding... keyBindings) {
        super(TextUtil.translatable("seedqueue.menu.keys"));
        this.parent = parent;
        this.keyBindings = keyBindings;
    }

    @Override
    protected void init() {
        this.keyBindingListWidget = new SeedQueueKeybindingsListWidget(this, this.minecraft);
        this.children.add(this.keyBindingListWidget);
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, I18n.translate("gui.done"), button -> this.onClose()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.focusedBinding != null) {
            this.focusedBinding.pressKey(InputUtil.Type.MOUSE.createFromCode(button));
            this.focusedBinding = null;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.focusedBinding != null) {
            this.focusedBinding.pressKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? InputUtil.UNKNOWN_KEYCODE : InputUtil.getKeyCode(keyCode, scanCode));
            this.focusedBinding = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;
        this.renderBackground();
        this.keyBindingListWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.minecraft.textRenderer, this.title.asFormattedString(), this.width / 2, 10, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.openScreen(this.parent);
    }
}

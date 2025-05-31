package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SeedQueueKeybindingsListWidget extends ElementListWidget<SeedQueueKeybindingsListWidget.Entry> {
    private final SeedQueueKeybindingsScreen parent;

    public SeedQueueKeybindingsListWidget(SeedQueueKeybindingsScreen parent, MinecraftClient client) {
        super(client, parent.width, parent.height, 25, parent.height - 32, 25);
        this.parent = parent;

        Map<String, List<PrimaryKeyEntry>> categoryToKeyEntryMap = new LinkedHashMap<>();
        for (SeedQueueMultiKeyBinding keyBinding : parent.keyBindings) {
            categoryToKeyEntryMap.computeIfAbsent(keyBinding.getCategory(), category -> new ArrayList<>()).add(new PrimaryKeyEntry(keyBinding));
        }
        for (Map.Entry<String, List<PrimaryKeyEntry>> category : categoryToKeyEntryMap.entrySet()) {
            this.addEntry(new CategoryEntry(I18n.translate(category.getKey())));
            for (PrimaryKeyEntry entry : category.getValue()) {
                this.addEntry(entry);
            }
        }
    }

    @Override
    public void setSelected(@Nullable SeedQueueKeybindingsListWidget.Entry entry) {
        SeedQueueKeybindingsListWidget.Entry selected = this.getSelected();
        if (selected instanceof PrimaryKeyEntry) {
            this.children().removeIf(e -> e instanceof AdditionalKeysEntry);
            ((PrimaryKeyEntry) selected).updateAdditionalKeysText();
        }
        if (entry instanceof PrimaryKeyEntry) {
            SecondaryKeysEntry secondaryKeys = new SecondaryKeysEntry((PrimaryKeyEntry) entry);
            BlockingKeysEntry blockingKeys = new BlockingKeysEntry((PrimaryKeyEntry) entry);
            int index = this.children().indexOf(entry);
            this.children().add(index + 1, secondaryKeys);
            this.children().add(index + 2, blockingKeys);
            this.ensureVisible(blockingKeys);
            this.ensureVisible(entry);
        }
        super.setSelected(entry);
    }

    @Override
    public int getRowWidth() {
        return Math.min(this.parent.width, 550);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.parent.width - 6;
    }

    // copy of KeyBinding#getLocalizedName
    private static String getLocalizedName(InputUtil.KeyCode keyCode) {
        String translation = null;

        switch (keyCode.getCategory()) {
            case KEYSYM:
                translation = InputUtil.getKeycodeName(keyCode.getKeyCode());
                break;
            case SCANCODE:
                translation = InputUtil.getScancodeName(keyCode.getKeyCode());
                break;
            case MOUSE:
                if (I18n.hasTranslation(keyCode.getName())) {
                    translation = I18n.translate(keyCode.getName());
                } else {
                    translation = I18n.translate(InputUtil.Type.MOUSE.getName(), keyCode.getKeyCode() + 1);
                }
        }

        if (translation == null) {
            translation = I18n.translate(keyCode.getName());
        }
        return translation;
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
    }

    public class CategoryEntry extends Entry {
        private final String text;
        private final int textWidth;

        public CategoryEntry(String text) {
            this.text = text;
            this.textWidth = SeedQueueKeybindingsListWidget.this.minecraft.textRenderer.getStringWidth(this.text);
        }

        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            SeedQueueKeybindingsListWidget.this.minecraft.textRenderer.draw(this.text, (SeedQueueKeybindingsListWidget.this.parent.width - this.textWidth) / 2.0f, (float) (y + entryHeight - SeedQueueKeybindingsListWidget.this.minecraft.textRenderer.fontHeight - 1), 0xFFFFFF);
        }

        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }

    public abstract class KeyEntry extends Entry {
        protected final String title;
        @Nullable
        protected final String tooltip;

        protected KeyEntry(String title) {
            this(title, null);
        }

        protected KeyEntry(String title, @Nullable String tooltip) {
            this.title = title;
            this.tooltip = tooltip;
        }

        protected abstract void pressKey(InputUtil.KeyCode key);

        protected abstract void selectButton(SeedQueueKeyButtonWidget button);

        protected abstract boolean isSelected(SeedQueueKeyButtonWidget button);

        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer = SeedQueueKeybindingsListWidget.this.minecraft.textRenderer;
            float titleX = x + 10;
            float titleY = y + entryHeight / 2.0f - textRenderer.fontHeight / 2.0f;
            textRenderer.draw(this.title, titleX, titleY, 0xFFFFFF);

            x += 110;
            for (Element e : this.children()) {
                AbstractPressableButtonWidget button = (AbstractPressableButtonWidget) e;
                button.x = x;
                button.y = y;
                if (button instanceof SeedQueueKeyButtonWidget && this.isSelected((SeedQueueKeyButtonWidget) button)) {
                    String message = button.getMessage();
                    button.setMessage("> " + Formatting.YELLOW + message + Formatting.RESET + " <");
                    button.render(mouseX, mouseY, tickDelta);
                    button.setMessage(message);
                } else {
                    button.render(mouseX, mouseY, tickDelta);
                }
                x += button.getWidth();
            }

            if (this.tooltip != null && mouseX > titleX && mouseX < titleX + textRenderer.getStringWidth(this.title) && mouseY > titleY && mouseY < titleY + textRenderer.fontHeight) {
                SeedQueueKeybindingsListWidget.this.parent.renderTooltip(textRenderer.wrapStringToWidthAsList(this.tooltip, 200), mouseX, mouseY);
            }
        }
    }

    public class PrimaryKeyEntry extends KeyEntry {
        private final SeedQueueMultiKeyBinding binding;
        private final SeedQueueKeyButtonWidget primaryKeyButton;

        private List<String> secondaryKeys;
        private List<String> blockingKeys;

        private PrimaryKeyEntry(SeedQueueMultiKeyBinding keyBinding) {
            super(I18n.translate(keyBinding.getTranslationKey()));
            this.binding = keyBinding;
            this.primaryKeyButton = new SeedQueueKeyButtonWidget(this, getLocalizedName(keyBinding.getPrimaryKey()));
            this.updateAdditionalKeysText();
        }

        private void updateAdditionalKeysText() {
            this.secondaryKeys = this.createAdditionalKeysText("seedqueue.menu.keys.secondary_list", this.binding.getSecondaryKeys());
            this.blockingKeys = this.createAdditionalKeysText("seedqueue.menu.keys.blocking_list", this.binding.getBlockingKeys());
        }

        private List<String> createAdditionalKeysText(String translationKey, List<InputUtil.KeyCode> keys) {
            String text1 = I18n.translate(translationKey);
            StringBuilder text2 = new StringBuilder(Formatting.GRAY.toString() + Formatting.ITALIC);
            if (keys.isEmpty()) {
                text2.append(I18n.translate("gui.none"));
            } else {
                text2.append(getLocalizedName(keys.get(0)));
                for (int i = 1; i < keys.size(); i++) {
                    text2.append(", ").append(getLocalizedName(keys.get(i)));
                }
            }
            String combined = text1 + " " + text2;
            int maxWidth = (SeedQueueKeybindingsListWidget.this.getRowWidth() - 195) / 2;
            if (SeedQueueKeybindingsListWidget.this.minecraft.textRenderer.getStringWidth(combined) < maxWidth - 10) {
                return Collections.singletonList(combined);
            }
            List<String> texts = new ArrayList<>();
            texts.add(text1);
            texts.add(text2.toString());
            return texts;
        }

        @Override
        protected void pressKey(InputUtil.KeyCode key) {
            this.binding.setPrimaryKey(key);
            this.primaryKeyButton.setMessage(getLocalizedName(key));
        }

        @Override
        protected void selectButton(SeedQueueKeyButtonWidget button) {
            SeedQueueKeybindingsListWidget.this.parent.focusedBinding = this;
        }

        @Override
        protected boolean isSelected(SeedQueueKeyButtonWidget button) {
            return SeedQueueKeybindingsListWidget.this.parent.focusedBinding == this && this.primaryKeyButton == button;
        }

        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            if (SeedQueueKeybindingsListWidget.this.getSelected() != this) {
                TextRenderer textRenderer = SeedQueueKeybindingsListWidget.this.minecraft.textRenderer;
                int maxWidth = (entryWidth - 195) / 2;
                for (int i = 0; i < this.secondaryKeys.size(); i++) {
                    SeedQueueKeybindingsListWidget.this.parent.drawCenteredString(textRenderer, this.secondaryKeys.get(i), x + entryWidth - maxWidth - maxWidth / 2, y + (entryHeight - textRenderer.fontHeight * this.secondaryKeys.size()) / 2 + textRenderer.fontHeight * i, 0xFFFFFF);
                }
                for (int i = 0; i < this.blockingKeys.size(); i++) {
                    SeedQueueKeybindingsListWidget.this.parent.drawCenteredString(textRenderer, this.blockingKeys.get(i), x + entryWidth - maxWidth / 2, y + (entryHeight - textRenderer.fontHeight * this.blockingKeys.size()) / 2 + textRenderer.fontHeight * i, 0xFFFFFF);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            SeedQueueKeybindingsListWidget.this.setSelected(SeedQueueKeybindingsListWidget.this.getSelected() == this ? null : this);
            return true;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(this.primaryKeyButton);
        }
    }

    public abstract class AdditionalKeysEntry extends KeyEntry {
        protected final PrimaryKeyEntry key;
        private final List<SeedQueueKeyButtonWidget> keyButtons;
        private final ButtonWidget addKeyButton;

        private int selectedIndex;

        public AdditionalKeysEntry(PrimaryKeyEntry key, String title, String tooltip) {
            super(title, tooltip);
            this.key = key;
            this.keyButtons = new ArrayList<>();
            for (InputUtil.KeyCode k : this.getKeys()) {
                this.keyButtons.add(new SeedQueueKeyButtonWidget(this, getLocalizedName(k)));
            }
            this.addKeyButton = new ButtonWidget(0, 0, 20, 20, "+", button -> {
                this.addKey();
                SeedQueueKeyButtonWidget keyButton = new SeedQueueKeyButtonWidget(this);
                this.keyButtons.add(keyButton);
                this.setFocused(keyButton);
                this.selectButton(keyButton);
            });
        }

        protected abstract void setKey(int index, InputUtil.KeyCode key);

        protected abstract void addKey();

        protected abstract void removeKey(int index);

        protected abstract List<InputUtil.KeyCode> getKeys();

        @Override
        protected void pressKey(InputUtil.KeyCode key) {
            if (this.selectedIndex != -1) {
                if (key.equals(InputUtil.UNKNOWN_KEYCODE)) {
                    this.removeKey(this.selectedIndex);
                    this.keyButtons.remove(this.selectedIndex);
                } else {
                    this.setKey(this.selectedIndex, key);
                    this.keyButtons.get(this.selectedIndex).setMessage(getLocalizedName(key));
                }
                this.selectedIndex = -1;
            }
        }

        @Override
        protected void selectButton(SeedQueueKeyButtonWidget button) {
            this.selectedIndex = this.keyButtons.indexOf(button);
            SeedQueueKeybindingsListWidget.this.parent.focusedBinding = this;
        }

        @Override
        protected boolean isSelected(SeedQueueKeyButtonWidget button) {
            return SeedQueueKeybindingsListWidget.this.parent.focusedBinding == this && this.selectedIndex != -1 && this.selectedIndex == this.keyButtons.indexOf(button);
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>(this.keyButtons);
            children.add(this.addKeyButton);
            return children;
        }
    }

    public class SecondaryKeysEntry extends AdditionalKeysEntry {

        public SecondaryKeysEntry(PrimaryKeyEntry key) {
            super(key, I18n.translate("seedqueue.menu.keys.secondary"), I18n.translate("seedqueue.menu.keys.secondary.tooltip"));
        }

        @Override
        protected void setKey(int index, InputUtil.KeyCode key) {
            this.key.binding.setSecondaryKey(index, key);
        }

        @Override
        protected void addKey() {
            this.key.binding.addSecondaryKey(InputUtil.UNKNOWN_KEYCODE);
        }

        @Override
        protected void removeKey(int index) {
            this.key.binding.removeSecondaryKey(index);
        }

        @Override
        protected List<InputUtil.KeyCode> getKeys() {
            return this.key.binding.getSecondaryKeys();
        }
    }

    public class BlockingKeysEntry extends AdditionalKeysEntry {

        public BlockingKeysEntry(PrimaryKeyEntry key) {
            super(key, I18n.translate("seedqueue.menu.keys.blocking"), I18n.translate("seedqueue.menu.keys.blocking.tooltip"));
        }

        @Override
        protected void setKey(int index, InputUtil.KeyCode key) {
            this.key.binding.setBlockingKey(index, key);
        }

        @Override
        protected void addKey() {
            this.key.binding.addBlockingKey(InputUtil.UNKNOWN_KEYCODE);
        }

        @Override
        protected void removeKey(int index) {
            this.key.binding.removeBlockingKey(index);
        }

        @Override
        protected List<InputUtil.KeyCode> getKeys() {
            return this.key.binding.getBlockingKeys();
        }
    }
}

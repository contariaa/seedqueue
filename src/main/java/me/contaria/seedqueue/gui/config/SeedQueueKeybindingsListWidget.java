package me.contaria.seedqueue.gui.config;

import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import me.contaria.speedrunapi.config.api.gui.ButtonWidgetCallback;
import me.contaria.speedrunapi.config.api.gui.CallbackButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SeedQueueKeybindingsListWidget extends EntryListWidget {
    private final SeedQueueKeybindingsScreen parent;
    private final List<Entry> entries;

    public SeedQueueKeybindingsListWidget(SeedQueueKeybindingsScreen parent, MinecraftClient client) {
        super(client, parent.width, parent.height, 25, parent.height - 32, 25);
        this.parent = parent;
        this.entries = new ArrayList<>();

        Map<String, List<PrimaryKeyEntry>> categoryToKeyEntryMap = new LinkedHashMap<>();
        for (SeedQueueMultiKeyBinding keyBinding : parent.keyBindings) {
            categoryToKeyEntryMap.computeIfAbsent(keyBinding.getCategory(), category -> new ArrayList<>()).add(new PrimaryKeyEntry(keyBinding));
        }
        for (Map.Entry<String, List<PrimaryKeyEntry>> category : categoryToKeyEntryMap.entrySet()) {
            this.entries.add(new CategoryEntry(I18n.translate(category.getKey())));
            this.entries.addAll(category.getValue());
        }
    }

    @Override
    public EntryListWidget.Entry getEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    protected int getEntryCount() {
        return this.entries.size();
    }

    @Override
    protected void selectEntry(int index, boolean doubleClick, int lastMouseX, int lastMouseY) {
        Entry entry = index != -1 ? this.entries.get(index) : null;
        if (this.selectedEntry >= 0 && this.selectedEntry < this.entries.size()) {
            Entry selected = this.entries.get(this.selectedEntry);
            if (selected instanceof PrimaryKeyEntry) {
                this.entries.removeIf(e -> e instanceof AdditionalKeysEntry);
                ((PrimaryKeyEntry) selected).updateAdditionalKeysText();
            }
        }
        if (entry != null) {
            index = this.entries.indexOf(entry);
            if (entry instanceof PrimaryKeyEntry) {
                SecondaryKeysEntry secondaryKeys = new SecondaryKeysEntry((PrimaryKeyEntry) entry);
                BlockingKeysEntry blockingKeys = new BlockingKeysEntry((PrimaryKeyEntry) entry);
                this.entries.add(index + 1, secondaryKeys);
                this.entries.add(index + 2, blockingKeys);
            }
        }
        this.selectedEntry = index;
    }

    @Override
    protected boolean isEntrySelected(int index) {
        return this.selectedEntry == index;
    }

    @Override
    public int getRowWidth() {
        return Math.min(this.parent.width, 550);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.parent.width - 6;
    }

    public abstract static class Entry implements EntryListWidget.Entry {
        @Override
        public void updatePosition(int index, int x, int y) {
        }

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            return false;
        }

        @Override
        public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {
        }
    }

    public class CategoryEntry extends Entry {
        private final String text;
        private final int textWidth;

        public CategoryEntry(String text) {
            this.text = text;
            this.textWidth = SeedQueueKeybindingsListWidget.this.client.textRenderer.getStringWidth(this.text);
        }

        @Override
        public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
            SeedQueueKeybindingsListWidget.this.client.textRenderer.draw(this.text, (SeedQueueKeybindingsListWidget.this.parent.width - this.textWidth) / 2, y + entryHeight - SeedQueueKeybindingsListWidget.this.client.textRenderer.fontHeight - 1, 0xFFFFFF);
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

        protected abstract void pressKey(int key);

        protected abstract void selectButton(SeedQueueKeyButtonWidget button);

        protected abstract boolean isSelected(SeedQueueKeyButtonWidget button);

        @Override
        public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
            TextRenderer textRenderer = SeedQueueKeybindingsListWidget.this.client.textRenderer;
            int titleX = x + 10;
            int titleY = y + (entryHeight - textRenderer.fontHeight) / 2;
            textRenderer.draw(this.title, titleX, titleY, 0xFFFFFF);

            x += 110;
            for (ButtonWidget button : this.children()) {
                button.x = x;
                button.y = y;
                if (button instanceof SeedQueueKeyButtonWidget && this.isSelected((SeedQueueKeyButtonWidget) button)) {
                    String message = button.message;
                    button.message = "> " + Formatting.YELLOW + message + Formatting.RESET + " <";
                    button.render(SeedQueueKeybindingsListWidget.this.client, mouseX, mouseY);
                    button.message = message;
                } else {
                    button.render(SeedQueueKeybindingsListWidget.this.client, mouseX, mouseY);
                }
                x += button.getWidth();
            }

            if (this.tooltip != null && mouseX > titleX && mouseX < titleX + textRenderer.getStringWidth(this.title) && mouseY > titleY && mouseY < titleY + textRenderer.fontHeight) {
                SeedQueueKeybindingsListWidget.this.parent.tooltip = this.tooltip;
            }
        }

        @Override
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            for (ButtonWidget widget : this.children()) {
                if (widget.isMouseOver(SeedQueueKeybindingsListWidget.this.client, mouseX, mouseY)) {
                    if (widget instanceof ButtonWidgetCallback) {
                        ((ButtonWidgetCallback) widget).onPress();
                    }
                    return true;
                }
            }
            return false;
        }

        public abstract List<ButtonWidget> children();
    }

    public class PrimaryKeyEntry extends KeyEntry {
        private final SeedQueueMultiKeyBinding binding;
        private final SeedQueueKeyButtonWidget primaryKeyButton;

        private List<String> secondaryKeys;
        private List<String> blockingKeys;

        private PrimaryKeyEntry(SeedQueueMultiKeyBinding keyBinding) {
            super(I18n.translate(keyBinding.getTranslationKey()));
            this.binding = keyBinding;
            this.primaryKeyButton = new SeedQueueKeyButtonWidget(this, GameOptions.getFormattedNameForKeyCode((keyBinding.getPrimaryKey())));
            this.updateAdditionalKeysText();
        }

        private void updateAdditionalKeysText() {
            this.secondaryKeys = this.createAdditionalKeysText("seedqueue.menu.keys.secondary_list", this.binding.getSecondaryKeys());
            this.blockingKeys = this.createAdditionalKeysText("seedqueue.menu.keys.blocking_list", this.binding.getBlockingKeys());
        }

        private List<String> createAdditionalKeysText(String translationKey, List<Integer> keys) {
            String text1 = I18n.translate(translationKey);
            StringBuilder text2 = new StringBuilder(Formatting.GRAY.toString() + Formatting.ITALIC);
            if (keys.isEmpty()) {
                text2.append(I18n.translate("gui.none"));
            } else {
                text2.append(GameOptions.getFormattedNameForKeyCode(keys.get(0)));
                for (int i = 1; i < keys.size(); i++) {
                    text2.append(", ").append(GameOptions.getFormattedNameForKeyCode(keys.get(i)));
                }
            }
            String combined = text1 + " " + text2;
            int maxWidth = (SeedQueueKeybindingsListWidget.this.getRowWidth() - 195) / 2;
            if (SeedQueueKeybindingsListWidget.this.client.textRenderer.getStringWidth(combined) < maxWidth - 10) {
                return Collections.singletonList(combined);
            }
            List<String> texts = new ArrayList<>();
            texts.add(text1);
            texts.add(text2.toString());
            return texts;
        }

        @Override
        protected void pressKey(int key) {
            this.binding.setPrimaryKey(key);
            this.primaryKeyButton.message = GameOptions.getFormattedNameForKeyCode(key);
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
        public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
            super.render(index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered);
            if (!SeedQueueKeybindingsListWidget.this.isEntrySelected(index)) {
                TextRenderer textRenderer = SeedQueueKeybindingsListWidget.this.client.textRenderer;
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
        public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
            if (super.mouseClicked(index, mouseX, mouseY, button, x, y)) {
                return true;
            }
            SeedQueueKeybindingsListWidget.this.selectEntry(SeedQueueKeybindingsListWidget.this.isEntrySelected(index) ? -1 : index, false, mouseX, mouseY);
            return true;
        }

        @Override
        public List<ButtonWidget> children() {
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
            for (int k : this.getKeys()) {
                this.keyButtons.add(new SeedQueueKeyButtonWidget(this, GameOptions.getFormattedNameForKeyCode(k)));
            }
            this.addKeyButton = new CallbackButtonWidget(0, 0, 20, 20, "+", button -> {
                this.addKey();
                SeedQueueKeyButtonWidget keyButton = new SeedQueueKeyButtonWidget(this);
                this.keyButtons.add(keyButton);
                this.selectButton(keyButton);
            });
        }

        protected abstract void setKey(int index, int key);

        protected abstract void addKey();

        protected abstract void removeKey(int index);

        protected abstract List<Integer> getKeys();

        @Override
        protected void pressKey(int key) {
            if (this.selectedIndex != -1) {
                if (key == 0) {
                    this.removeKey(this.selectedIndex);
                    this.keyButtons.remove(this.selectedIndex);
                } else {
                    this.setKey(this.selectedIndex, key);
                    this.keyButtons.get(this.selectedIndex).message = GameOptions.getFormattedNameForKeyCode(key);
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
        public List<ButtonWidget> children() {
            List<ButtonWidget> children = new ArrayList<>(this.keyButtons);
            children.add(this.addKeyButton);
            return children;
        }
    }

    public class SecondaryKeysEntry extends AdditionalKeysEntry {

        public SecondaryKeysEntry(PrimaryKeyEntry key) {
            super(key, I18n.translate("seedqueue.menu.keys.secondary"), I18n.translate("seedqueue.menu.keys.secondary.tooltip"));
        }

        @Override
        protected void setKey(int index, int key) {
            this.key.binding.setSecondaryKey(index, key);
        }

        @Override
        protected void addKey() {
            this.key.binding.addSecondaryKey(0);
        }

        @Override
        protected void removeKey(int index) {
            this.key.binding.removeSecondaryKey(index);
        }

        @Override
        protected List<Integer> getKeys() {
            return this.key.binding.getSecondaryKeys();
        }
    }

    public class BlockingKeysEntry extends AdditionalKeysEntry {

        public BlockingKeysEntry(PrimaryKeyEntry key) {
            super(key, I18n.translate("seedqueue.menu.keys.blocking"), I18n.translate("seedqueue.menu.keys.blocking.tooltip"));
        }

        @Override
        protected void setKey(int index, int key) {
            this.key.binding.setBlockingKey(index, key);
        }

        @Override
        protected void addKey() {
            this.key.binding.addBlockingKey(0);
        }

        @Override
        protected void removeKey(int index) {
            this.key.binding.removeBlockingKey(index);
        }

        @Override
        protected List<Integer> getKeys() {
            return this.key.binding.getBlockingKeys();
        }
    }
}

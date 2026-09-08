package me.contaria.seedqueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import me.contaria.seedqueue.compat.ModCompat;
import me.contaria.seedqueue.gui.config.SeedQueueKeybindingsScreen;
import me.contaria.seedqueue.gui.config.SeedQueueWindowSizeWidget;
import me.contaria.seedqueue.keybindings.SeedQueueKeyBindings;
import me.contaria.seedqueue.keybindings.SeedQueueMultiKeyBinding;
import me.contaria.speedrunapi.config.SpeedrunConfigAPI;
import me.contaria.speedrunapi.config.SpeedrunConfigContainer;
import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import me.contaria.speedrunapi.config.api.SpeedrunOption;
import me.contaria.speedrunapi.config.api.annotations.Config;
import me.contaria.speedrunapi.config.api.gui.CallbackButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Config class based on SpeedrunAPI, initialized on prelaunch.
 * <p>
 * When implementing new options, make sure no Minecraft classes are loaded during initialization!
 */
@SuppressWarnings("FieldMayBeFinal")
public class SeedQueueConfig implements SpeedrunConfig {
    private static final boolean CAN_USE_WALL = ModCompat.HAS_STANDARDSETTINGS;

    @Config.Ignored
    public SpeedrunConfigContainer<?> container;

    @Config.Category("queue")
    @Config.Numbers.Whole.Bounds(min = 0, max = 30)
    public int maxCapacity = 0;

    @Config.Category("queue")
    @Config.Numbers.Whole.Bounds(min = 0, max = 30)
    public int maxConcurrently = 1;

    @Config.Category("queue")
    @Config.Numbers.Whole.Bounds(min = 1, max = 30)
    public int maxConcurrently_onWall = 1;

    @Config.Category("wall")
    public boolean useWall = false;

    @Config.Category("wall")
    @Config.Numbers.Whole.Bounds(min = 1, max = 10)
    public int rows = 2;

    @Config.Category("wall")
    @Config.Numbers.Whole.Bounds(min = 1, max = 10)
    public int columns = 2;

    @Config.Category("wall")
    public final WindowSize simulatedWindowSize = new WindowSize();

    @Config.Category("wall")
    @Config.Numbers.Whole.Bounds(max = 1000)
    public int resetCooldown = 150;

    @Config.Category("wall")
    public boolean waitForPreviewSetup = true;

    @Config.Category("wall")
    public boolean bypassWall = false;

    @Config.Category("wall")
    public boolean smartSwitch = false;

    @Config.Category("performance")
    @Config.Numbers.Whole.Bounds(min = 1, max = 255)
    public int wallFPS = 60;

    @Config.Category("performance")
    @Config.Numbers.Whole.Bounds(min = 1, max = 255)
    public int previewFPS = 15;

    @Config.Category("performance")
    @Config.Numbers.Whole.Bounds(min = -1, max = 30)
    public int preparingPreviews = -1; // auto

    @Config.Category("performance")
    public boolean reduceLevelList = true;

    @Config.Category("worldpreview")
    public boolean generateFakePreview = true;

    @Config.Category("worldpreview")
    @Config.Numbers.Whole.Bounds(min = 1, max = 16)
    public int previewChunkDistance = 5;

    @Config.Category("worldpreview")
    @Config.Numbers.Whole.Bounds(min = 1, max = 100)
    public int previewDataLimit = 50;

    @Config.Category("advanced")
    public boolean showAdvancedSettings = false;

    @Config.Category("threading")
    @Config.Numbers.Whole.Bounds(min = Thread.MIN_PRIORITY, max = Thread.NORM_PRIORITY)
    public int seedQueueThreadPriority = Thread.NORM_PRIORITY;

    @Config.Category("threading")
    @Config.Numbers.Whole.Bounds(min = Thread.MIN_PRIORITY, max = Thread.NORM_PRIORITY)
    public int serverThreadPriority = 4;

    @Config.Category("debug")
    public boolean showDebugMenu = false;

    @Config.Category("debug")
    @Config.Numbers.Whole.Bounds(min = 1, max = Integer.MAX_VALUE)
    @Config.Numbers.TextField
    public int benchmarkResets = 1000;

    @Config.Category("debug")
    public boolean useWatchdog = false;

    @Config.Category("wall")
    public final SeedQueueMultiKeyBinding[] keyBindings = new SeedQueueMultiKeyBinding[]{
            SeedQueueKeyBindings.play,
            SeedQueueKeyBindings.focusReset,
            SeedQueueKeyBindings.reset,
            SeedQueueKeyBindings.lock,
            SeedQueueKeyBindings.resetAll,
            SeedQueueKeyBindings.resetColumn,
            SeedQueueKeyBindings.resetRow,
            SeedQueueKeyBindings.playNextLock,
            SeedQueueKeyBindings.scheduleJoin,
            SeedQueueKeyBindings.scheduleAll,
            SeedQueueKeyBindings.startBenchmark,
            SeedQueueKeyBindings.cancelBenchmark
    };

    {
        SeedQueue.config = this;
    }

    // see Window#calculateScaleFactor
    public int calculateSimulatedScaleFactor(int guiScale, boolean forceUnicodeFont) {
        int scaleFactor = 1;
        while (scaleFactor != guiScale && scaleFactor < this.simulatedWindowSize.width() && scaleFactor < this.simulatedWindowSize.height() && this.simulatedWindowSize.width() / (scaleFactor + 1) >= 320 && this.simulatedWindowSize.height() / (scaleFactor + 1) >= 240) {
            scaleFactor++;
        }
        if (forceUnicodeFont) {
            scaleFactor += guiScale % 2;
        }
        return scaleFactor;
    }

    public boolean shouldUseWall() {
        return CAN_USE_WALL && this.maxCapacity > 0 && this.useWall;
    }

    @Override
    public @Nullable SpeedrunOption<?> parseField(Field field, SpeedrunConfig config, String... idPrefix) {
        if ("useWall".equals(field.getName())) {
            return new SpeedrunConfigAPI.CustomOption.Builder<Boolean>(config, this, field, idPrefix)
                    .createWidget((option, config_, configStorage, optionField) -> {
                        if (!CAN_USE_WALL) {
                            ButtonWidget widget = new ButtonWidget(-1, 0, 0, 150, 20, I18n.translate("seedqueue.menu.config.useWall.notAvailable"));
                            widget.active = false;
                            return widget;
                        }
                        return new CallbackButtonWidget(I18n.translate(option.get() ? "options.on" : "options.off"), button -> {
                            option.set(!option.get());
                            button.message = I18n.translate(option.get() ? "options.on" : "options.off");
                        });
                    })
                    .build();
        }
        if ("showAdvancedSettings".equals(field.getName())) {
            return new SpeedrunConfigAPI.CustomOption.Builder<Boolean>(config, this, field, idPrefix)
                    .createWidget((option, config_, configStorage, optionField) -> new CallbackButtonWidget(I18n.translate(option.get() ? "options.on" : "options.off"), button -> {
                        if (!option.get()) {
                            Screen configScreen = MinecraftClient.getInstance().currentScreen;
                            MinecraftClient.getInstance().setScreen(new ConfirmScreen((confirm, id) -> {
                                option.set(confirm);
                                MinecraftClient.getInstance().setScreen(configScreen);
                            }, I18n.translate("seedqueue.menu.config.showAdvancedSettings.confirm.title"), I18n.translate("seedqueue.menu.config.showAdvancedSettings.confirm.message"), I18n.translate("gui.yes"), I18n.translate("gui.cancel"), 0));
                        } else {
                            option.set(false);
                            MinecraftClient.getInstance().setScreen(MinecraftClient.getInstance().currentScreen);
                        }
                    }))
                    .build();
        }
        if (WindowSize.class.equals(field.getType())) {
            return new SpeedrunConfigAPI.CustomOption.Builder<WindowSize>(config, this, field, idPrefix)
                    .fromJson((option, config_, configStorage, optionField, jsonElement) -> option.get().fromJson(jsonElement.getAsJsonObject()))
                    .toJson((option, config_, configStorage, optionField) -> option.get().toJson())
                    .setter((option, config_, configStorage, optionField, value) -> {
                        throw new UnsupportedOperationException();
                    })
                    .createWidget((option, config_, configStorage, optionField) -> new SeedQueueWindowSizeWidget(option.get()))
                    .build();
        }
        if (SeedQueueMultiKeyBinding[].class.equals(field.getType())) {
            return new SpeedrunConfigAPI.CustomOption.Builder<SeedQueueMultiKeyBinding[]>(config, this, field, idPrefix)
                    .fromJson((option, config_, configStorage, optionField, jsonElement) -> {
                        for (SeedQueueMultiKeyBinding keyBinding : option.get()) {
                            keyBinding.fromJson(jsonElement.getAsJsonObject().get(keyBinding.getTranslationKey()));
                        }
                    })
                    .toJson((option, config_, configStorage, optionField) -> {
                        JsonObject jsonObject = new JsonObject();
                        for (SeedQueueMultiKeyBinding keyBinding : option.get()) {
                            jsonObject.add(keyBinding.getTranslationKey(), keyBinding.toJson());
                        }
                        return jsonObject;
                    })
                    .setter((option, config_, configStorage, optionField, value) -> {
                        throw new UnsupportedOperationException();
                    })
                    .createWidget((option, config_, configStorage, optionField) -> new CallbackButtonWidget(I18n.translate("seedqueue.menu.keys.configure"), button -> MinecraftClient.getInstance().setScreen(new SeedQueueKeybindingsScreen(MinecraftClient.getInstance().currentScreen, this.keyBindings))))
                    .build();
        }
        return SpeedrunConfig.super.parseField(field, config, idPrefix);
    }

    /**
     * Reloads the config from disk.
     */
    public void reload() throws IOException, JsonParseException {
        if (this.container != null) {
            this.container.load();
        }
    }

    @Override
    public void finishInitialization(SpeedrunConfigContainer<?> container) {
        this.container = container;
    }

    @Override
    public boolean shouldShowCategory(String category) {
        if (!this.showAdvancedSettings) {
            return !category.equals("threading") && !category.equals("experimental") && !category.equals("debug");
        }
        return true;
    }

    @Override
    public String modID() {
        return "seedqueue";
    }

    @Override
    public boolean isAvailable() {
        return !SeedQueue.isActive();
    }

    public static class WindowSize {
        private int width;
        private int height;

        public int width() {
            if (this.width == 0) {
                this.width = MinecraftClient.getInstance().width;
            }
            return this.width;
        }

        public void setWidth(int width) {
            this.width = Math.max(0, Math.min(16384, width));
        }

        public int height() {
            if (this.height == 0) {
                this.height = MinecraftClient.getInstance().height;
            }
            return this.height;
        }

        public void setHeight(int height) {
            this.height = Math.max(0, Math.min(16384, height));
        }

        public void init() {
            this.width();
            this.height();
        }

        public void fromJson(JsonObject jsonObject) {
            this.setWidth(jsonObject.get("width").getAsInt());
            this.setHeight(jsonObject.get("height").getAsInt());
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("width", new JsonPrimitive(this.width));
            jsonObject.add("height", new JsonPrimitive(this.height));
            return jsonObject;
        }
    }
}

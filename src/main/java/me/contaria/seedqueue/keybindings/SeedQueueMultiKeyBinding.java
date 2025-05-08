package me.contaria.seedqueue.keybindings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SeedQueueMultiKeyBinding {
    private final String translationKey;
    private final String category;

    private InputUtil.KeyCode primaryKey;
    private final List<InputUtil.KeyCode> secondaryKeys;
    private final List<InputUtil.KeyCode> blockingKeys;

    public SeedQueueMultiKeyBinding(String translationKey) {
        this(translationKey, "seedqueue.key.categories.builtin");
    }

    public SeedQueueMultiKeyBinding(String translationKey, String category) {
        this(translationKey, category, InputUtil.UNKNOWN_KEYCODE);
    }

    public SeedQueueMultiKeyBinding(String translationKey, int code) {
        this(translationKey, "seedqueue.key.categories.builtin", code);
    }

    public SeedQueueMultiKeyBinding(String translationKey, String category, int code) {
        this(translationKey, category, InputUtil.Type.KEYSYM, code);
    }

    public SeedQueueMultiKeyBinding(String translationKey, String category, InputUtil.Type type, int code) {
        this(translationKey, category, type.createFromCode(code));
    }

    protected SeedQueueMultiKeyBinding(String translationKey, String category, InputUtil.KeyCode primaryKey) {
        this.translationKey = translationKey;
        this.category = category;
        this.primaryKey = primaryKey;
        this.secondaryKeys = new ArrayList<>();
        this.blockingKeys = new ArrayList<>();
    }

    public boolean matchesKey(int keyCode, int scanCode) {
        return keyCode == InputUtil.UNKNOWN_KEYCODE.getKeyCode() ? this.matchesPrimary(InputUtil.Type.SCANCODE, scanCode) : this.matchesPrimary(InputUtil.Type.KEYSYM, keyCode) && this.areSecondaryKeysDown() && this.areBlockingKeysNotDown();
    }

    public boolean matchesMouse(int code) {
        return this.matchesPrimary(InputUtil.Type.MOUSE, code) && this.areSecondaryKeysDown() && this.areBlockingKeysNotDown();
    }

    private boolean matchesPrimary(InputUtil.Type type, int code) {
        return this.primaryKey.getCategory() == type && this.primaryKey.getKeyCode() == code;
    }

    private boolean areSecondaryKeysDown() {
        for (InputUtil.KeyCode key : this.secondaryKeys) {
            if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.getKeyCode())) {
                return false;
            }
        }
        return true;
    }

    private boolean areBlockingKeysNotDown() {
        for (InputUtil.KeyCode key : this.blockingKeys) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.getKeyCode())) {
                return false;
            }
        }
        return true;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public String getCategory() {
        return this.category;
    }

    public InputUtil.KeyCode getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(InputUtil.KeyCode key) {
        this.primaryKey = key;
    }

    public void setSecondaryKey(int index, InputUtil.KeyCode key) {
        this.secondaryKeys.set(index, key);
    }

    public void addSecondaryKey(InputUtil.KeyCode key) {
        this.secondaryKeys.add(key);
    }

    public void removeSecondaryKey(int index) {

        this.secondaryKeys.remove(index);
    }

    public List<InputUtil.KeyCode> getSecondaryKeys() {
        return this.secondaryKeys;
    }

    public void setBlockingKey(int index, InputUtil.KeyCode key) {
        this.blockingKeys.set(index, key);
    }

    public void addBlockingKey(InputUtil.KeyCode key) {
        this.blockingKeys.add(key);
    }

    public void removeBlockingKey(int index) {
        this.blockingKeys.remove(index);
    }

    public List<InputUtil.KeyCode> getBlockingKeys() {
        return this.blockingKeys;
    }

    public JsonElement toJson() {
        if (this.secondaryKeys.isEmpty() && this.blockingKeys.isEmpty()) {
            return new JsonPrimitive(this.primaryKey.getName());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("primary", new JsonPrimitive(this.primaryKey.getName()));

        JsonArray secondary = new JsonArray();
        for (InputUtil.KeyCode key : this.secondaryKeys) {
            secondary.add(new JsonPrimitive(key.getName()));
        }
        jsonObject.add("secondary", secondary);

        JsonArray blocking = new JsonArray();
        for (InputUtil.KeyCode key : this.blockingKeys) {
            blocking.add(new JsonPrimitive(key.getName()));
        }
        jsonObject.add("blocking", blocking);

        return jsonObject;
    }

    public void fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null) {
            return;
        }

        this.secondaryKeys.clear();
        this.blockingKeys.clear();

        if (!jsonElement.isJsonObject()) {
            this.setPrimaryKey(InputUtil.fromName(jsonElement.getAsString()));
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        this.setPrimaryKey(InputUtil.fromName(jsonObject.get("primary").getAsString()));
        for (JsonElement key : jsonObject.getAsJsonArray("secondary")) {
            this.addSecondaryKey(InputUtil.fromName(key.getAsString()));
        }
        for (JsonElement key : jsonObject.getAsJsonArray("blocking")) {
            this.addBlockingKey(InputUtil.fromName(key.getAsString()));
        }
    }
}

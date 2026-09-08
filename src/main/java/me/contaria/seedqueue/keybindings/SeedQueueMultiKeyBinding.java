package me.contaria.seedqueue.keybindings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class SeedQueueMultiKeyBinding {
    private final String translationKey;
    private final String category;

    private int primaryKey;
    private final List<Integer> secondaryKeys;
    private final List<Integer> blockingKeys;

    public SeedQueueMultiKeyBinding(String translationKey) {
        this(translationKey, "seedqueue.key.categories.builtin");
    }

    public SeedQueueMultiKeyBinding(String translationKey, String category) {
        this(translationKey, category, 0);
    }

    public SeedQueueMultiKeyBinding(String translationKey, int code) {
        this(translationKey, "seedqueue.key.categories.builtin", code);
    }

    public SeedQueueMultiKeyBinding(String translationKey, String category, int code) {
        this.translationKey = translationKey;
        this.category = category;
        this.primaryKey = code;
        this.secondaryKeys = new ArrayList<>();
        this.blockingKeys = new ArrayList<>();
    }

    public boolean matchesKey(int code) {
        return this.matchesPrimary(code) && this.areSecondaryKeysDown() && this.areBlockingKeysNotDown();
    }

    public boolean matchesMouse(int code) {
        return this.matchesPrimary(code - 100) && this.areSecondaryKeysDown() && this.areBlockingKeysNotDown();
    }

    private boolean matchesPrimary(int code) {
        return this.primaryKey == code;
    }

    private boolean areSecondaryKeysDown() {
        for (int key : this.secondaryKeys) {
            if (!Keyboard.isKeyDown(key)) {
                return false;
            }
        }
        return true;
    }

    private boolean areBlockingKeysNotDown() {
        for (int key : this.blockingKeys) {
            if (Keyboard.isKeyDown(key)) {
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

    public int getPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(int code) {
        this.primaryKey = code;
    }

    public void setSecondaryKey(int index, int code) {
        this.secondaryKeys.set(index, code);
    }

    public void addSecondaryKey(int code) {
        this.secondaryKeys.add(code);
    }

    public void removeSecondaryKey(int index) {
        this.secondaryKeys.remove(index);
    }

    public List<Integer> getSecondaryKeys() {
        return this.secondaryKeys;
    }

    public void setBlockingKey(int index, int code) {
        this.blockingKeys.set(index, code);
    }

    public void addBlockingKey(int code) {
        this.blockingKeys.add(code);
    }

    public void removeBlockingKey(int index) {
        this.blockingKeys.remove(index);
    }

    public List<Integer> getBlockingKeys() {
        return this.blockingKeys;
    }

    public JsonElement toJson() {
        if (this.secondaryKeys.isEmpty() && this.blockingKeys.isEmpty()) {
            return new JsonPrimitive(this.primaryKey);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("primary", new JsonPrimitive(this.primaryKey));

        JsonArray secondary = new JsonArray();
        for (int key : this.secondaryKeys) {
            secondary.add(new JsonPrimitive(key));
        }
        jsonObject.add("secondary", secondary);

        JsonArray blocking = new JsonArray();
        for (int key : this.blockingKeys) {
            blocking.add(new JsonPrimitive(key));
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
            this.setPrimaryKey(jsonElement.getAsInt());
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        this.setPrimaryKey(jsonObject.get("primary").getAsInt());
        for (JsonElement key : jsonObject.getAsJsonArray("secondary")) {
            this.addSecondaryKey(key.getAsInt());
        }
        for (JsonElement key : jsonObject.getAsJsonArray("blocking")) {
            this.addBlockingKey(key.getAsInt());
        }
    }
}

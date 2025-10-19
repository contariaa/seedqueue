package me.contaria.seedqueue.customization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resource.metadata.ResourceMetadataReader;

@SuppressWarnings("unused") // fields set by GSON
class LockTextureMetadata {
    public static final ResourceMetadataReader<LockTextureMetadata> READER = new Reader();

    private int weight;

    public int getWeight() {
        return Math.max(1, weight);
    }

    private static class Reader implements ResourceMetadataReader<LockTextureMetadata> {
        private static final Gson GSON = new Gson();

        @Override
        public String getKey() {
            return "seedqueue";
        }

        @Override
        public LockTextureMetadata fromJson(JsonObject json) {
            return GSON.fromJson(json, LockTextureMetadata.class);
        }
    }
}

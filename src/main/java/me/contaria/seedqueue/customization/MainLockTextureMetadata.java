package me.contaria.seedqueue.customization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resource.metadata.ResourceMetadataReader;

class MainLockTextureMetadata {
    public static final ResourceMetadataReader<MainLockTextureMetadata> READER = new Reader();

    @SuppressWarnings("unused") // set by GSON
    public int defaultWeight;

    private static class Reader implements ResourceMetadataReader<MainLockTextureMetadata> {
        private static final Gson GSON = new Gson();

        @Override
        public String getKey() {
            return "seedqueue";
        }

        @Override
        public MainLockTextureMetadata fromJson(JsonObject json) {
            return GSON.fromJson(json, MainLockTextureMetadata.class);
        }
    }
}

package me.contaria.seedqueue.interfaces.worldpreview;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

public interface SQWorldRenderer {

    void seedQueue$buildChunks(MatrixStack matrices, Camera camera, Matrix4f projectionMatrix);
}

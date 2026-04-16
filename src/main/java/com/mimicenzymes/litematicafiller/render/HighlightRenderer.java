package com.mimicenzymes.litematicafiller.render;

import com.mimicenzymes.litematicafiller.config.Configs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

//#if MC > 12104
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;
//#else
//$$ import net.minecraft.client.render.*;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import org.joml.Matrix4f;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import java.util.OptionalDouble;
//$$ import fi.dy.masa.malilib.util.Color4f;
//#endif

public class HighlightRenderer {
    private static final HighlightRenderer INSTANCE = new HighlightRenderer();

    public static HighlightRenderer getInstance() {
        return INSTANCE;
    }
    MinecraftClient client = MinecraftClient.getInstance();

    //#if MC > 12104
    // 1.21.10 版本：无参方法
    //#if MC >=12108
    public void render() {
    //#else
    //$$ public void render(Object context) {
    //#endif
        if (!Configs.ENABLE_MOD.getBooleanValue() || !Configs.HIGHLIGHT_CONTAINERS.getBooleanValue())
            return;

        Map<BlockPos, HighlightState> highlights = HighlightScanner.getHighlights();
        if (highlights.isEmpty())
            return;

        try {
            boolean xray = Configs.HIGHLIGHT_XRAY.getBooleanValue();

            RenderContext ctx = new RenderContext(
                    () -> "litematica_filler_lines",
                    xray ? MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL
                            : MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_OFFSET_2);

            var buffer = ctx.getBuilder();
            if (buffer == null) return;

            float lineWidth = Math.max(2.5F, (float)client.getWindow().getFramebufferWidth() / 1920.0F * 2.5F);

            for (Map.Entry<BlockPos, HighlightState> entry : highlights.entrySet()) {
                Color4f c = getColor(entry.getValue());
                //#if MC > 12110
                RenderUtils.drawBlockBoundingBoxOutlinesBatchedLinesSimple(entry.getKey(), c, 0.015, lineWidth, buffer);
                //#else
                //$$ RenderUtils.drawBlockBoundingBoxOutlinesBatchedLinesSimple(entry.getKey(), c, 0.015, buffer);
                //#endif
            }
            BuiltBuffer meshData = buffer.endNullable();
            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }
            ctx.reset();

        } catch (Throwable e) {
            if (client.player != null && client.world != null) {
                if (client.world.getTime() % 60 == 0) {
                    client.player.sendMessage(net.minecraft.text.Text.literal("§c[容器填充机] 渲染错误: " + e.getMessage()),
                            false);
                }
            }
            e.printStackTrace();
        }
    }
    //#else
    //$$ // 1.21.4 版本：带 context 参数的方法
    //$$ private static final RenderLayer LINES_XRAY = RenderLayer.of(
    //$$         "lines_xray",
    //$$         VertexFormats.LINES,
    //$$         VertexFormat.DrawMode.LINES,
    //$$         1536,
    //$$         RenderLayer.MultiPhaseParameters.builder()
    //$$                 .program(RenderPhase.LINES_PROGRAM)
    //$$                 .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
    //$$                 .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
    //$$                 .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
    //$$                 .target(RenderPhase.ITEM_ENTITY_TARGET)
    //$$                 .writeMaskState(RenderPhase.COLOR_MASK)
    //$$                 .cull(RenderPhase.DISABLE_CULLING)
    //$$                 .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
    //$$                 .build(false)
    //$$ );
    //$$
    //$$ public void render(Object context) {
    //$$     if (!Configs.ENABLE_MOD.getBooleanValue() || !Configs.HIGHLIGHT_CONTAINERS.getBooleanValue()) return;
    //$$
    //$$     Map<BlockPos, HighlightState> highlights = HighlightScanner.getHighlights();
    //$$     if (highlights.isEmpty()) return;
    //$$
    //$$     MatrixStack matrices = null;
    //$$     if (context instanceof MatrixStack) {
    //$$         matrices = (MatrixStack) context;
    //$$     } else if (context != null) {
    //$$         try {
    //$$             for (java.lang.reflect.Method m : context.getClass().getMethods()) {
    //$$                 if (m.getReturnType() == MatrixStack.class) {
    //$$                     matrices = (MatrixStack) m.invoke(context);
    //$$                     break;
    //$$                 }
    //$$             }
    //$$         } catch (Exception ignored) {}
    //$$     }
    //$$     if (matrices == null) {
    //$$         matrices = new MatrixStack();
    //$$     }
    //$$
    //$$     try {
    //$$         MinecraftClient client = MinecraftClient.getInstance();
    //$$         if (client.player == null) return;
    //$$
    //$$         Vec3d cam = client.gameRenderer.getCamera().getPos();
    //$$         boolean xray = Configs.HIGHLIGHT_XRAY.getBooleanValue();
    //$$
    //$$         RenderLayer lineLayer = xray ? LINES_XRAY : RenderLayer.getLines();
    //$$         VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
    //$$         VertexConsumer buffer = immediate.getBuffer(lineLayer);
    //$$
    //$$         for (Map.Entry<BlockPos, HighlightState> entry : highlights.entrySet()) {
    //$$             Color4f c = getColor(entry.getValue());
    //$$             drawBox(matrices, buffer, entry.getKey(), cam, c);
    //$$         }
    //$$
    //$$         float lineWidth = Math.max(2.5F, (float)client.getWindow().getFramebufferWidth() / 1920.0F * 2.5F);
    //$$         RenderSystem.lineWidth(lineWidth);
    //$$
    //$$         immediate.draw(lineLayer);
    //$$
    //$$         RenderSystem.lineWidth(1.0F);
    //$$
    //$$     } catch (Throwable e) {
    //$$         e.printStackTrace();
    //$$     }
    //$$ }
    //$$
    //$$ private void drawBox(MatrixStack matrices, VertexConsumer buffer, BlockPos pos, Vec3d cam, Color4f c) {
    //$$     matrices.push();
    //$$     matrices.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
    //$$     Matrix4f model = matrices.peek().getPositionMatrix();
    //$$
    //$$     float s = -0.015f;
    //$$     float e = 1.015f;
    //$$
    //$$     int r = Math.max(0, Math.min(255, (int) (c.r * 255.0f)));
    //$$     int g = Math.max(0, Math.min(255, (int) (c.g * 255.0f)));
    //$$     int b = Math.max(0, Math.min(255, (int) (c.b * 255.0f)));
    //$$     int a = Math.max(0, Math.min(255, (int) (c.a * 255.0f)));
    //$$
    //$$     line(buffer, model, s, s, s, e, s, s, r, g, b, a);
    //$$     line(buffer, model, e, s, s, e, s, e, r, g, b, a);
    //$$     line(buffer, model, e, s, e, s, s, e, r, g, b, a);
    //$$     line(buffer, model, s, s, e, s, s, s, r, g, b, a);
    //$$
    //$$     line(buffer, model, s, e, s, e, e, s, r, g, b, a);
    //$$     line(buffer, model, e, e, s, e, e, e, r, g, b, a);
    //$$     line(buffer, model, e, e, e, s, e, e, r, g, b, a);
    //$$     line(buffer, model, s, e, e, s, e, s, r, g, b, a);
    //$$
    //$$     line(buffer, model, s, s, s, s, e, s, r, g, b, a);
    //$$     line(buffer, model, e, s, s, e, e, s, r, g, b, a);
    //$$     line(buffer, model, e, s, e, e, e, e, r, g, b, a);
    //$$     line(buffer, model, s, s, e, s, e, e, r, g, b, a);
    //$$
    //$$     matrices.pop();
    //$$ }
    //$$
    //$$ private void line(VertexConsumer buffer, Matrix4f model, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
    //$$     try {
    //$$         buffer.vertex(model, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0);
    //$$         buffer.vertex(model, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0);
    //$$     } catch (Throwable t) {
    //$$         try {
    //$$             buffer.vertex(model, x1, y1, z1).color(r, g, b, a);
    //$$             buffer.vertex(model, x2, y2, z2).color(r, g, b, a);
    //$$         } catch (Throwable t2) {}
    //$$     }
    //$$ }
    //#endif

    private Color4f getColor(HighlightState type) {
        return switch (type) {
            case UNFILLED -> Configs.HIGHLIGHT_COLOR_UNFILLED.getColor();
            case PARTIAL -> Configs.HIGHLIGHT_COLOR_PARTIAL.getColor();
            case OVERFILLED -> Configs.HIGHLIGHT_COLOR_OVERFILLED.getColor();
            case WRONG_ITEM -> Configs.HIGHLIGHT_COLOR_WRONG.getColor();
            case SATISFIED -> Configs.HIGHLIGHT_COLOR_SATISFIED.getColor();
            default -> Configs.HIGHLIGHT_COLOR_UNKNOWN.getColor();
        };
    }
}
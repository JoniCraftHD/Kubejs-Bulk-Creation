package com.joni.kjs_bc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonAssetGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void generateAssetsForCreations() {
        File baseDir = new File("kubejs/assets/kubejs");
        File blockstatesDir = new File(baseDir, "blockstates");
        File blockModelsDir = new File(baseDir, "models/block");
        File itemModelsDir = new File(baseDir, "models/item");
        File particleTexturesDir = new File(baseDir, "textures/block/particle");

        blockstatesDir.mkdirs();
        blockModelsDir.mkdirs();
        itemModelsDir.mkdirs();
        particleTexturesDir.mkdirs();

        for (BulkBuilderLogic.BulkCreation creation : BulkBuilderLogic.getCreations()) {
            // Expand bundles + dependencies into the real preset list for this creation
            List<String> resolvedPresetIds = BulkBuilderLogic.getResolvedPresetIds(creation);

            for (String presetId : resolvedPresetIds) {
                BulkBuilderLogic.Preset preset = BulkBuilderLogic.getPresets().get(presetId);

                if (preset != null && "block".equalsIgnoreCase(preset.type())) {
                    String blockId = creation.id() + preset.idSuffix();

                    String baseTex = preset.baseTexture().isEmpty() ? "minecraft:block/stone" : preset.baseTexture();

                    // If the creation defines an overlay override, it wins over the preset's own overlay.
                    // This lets you reuse the same block preset (e.g. different base rock types) across
                    // many materials while still swapping the overlay art per-creation if needed.
                    String overlayTex;
                    if (creation.overlayOverride() != null && !creation.overlayOverride().isEmpty()) {
                        overlayTex = creation.overlayOverride();
                    } else if (!preset.overlays().isEmpty()) {
                        overlayTex = preset.overlays().get(0);
                    } else {
                        overlayTex = "kubejs:block/ore_overlay";
                    }

                    // Baked particle texture (base + tinted overlay, pre-composited)
                    String particleTexId = "kubejs:block/particle/" + blockId + "_particle";
                    File particleFile = new File(particleTexturesDir, blockId + "_particle.png");
                    TextureGenerator.generateCompositeTexture(baseTex, overlayTex, creation.tintColor(), particleFile);

                    writeJson(new File(blockstatesDir, blockId + ".json"), createBlockstateJson(blockId));
                    writeJson(new File(blockModelsDir, blockId + ".json"), createBlockModelJson(baseTex, overlayTex, particleTexId));
                    writeJson(new File(itemModelsDir, blockId + ".json"), createBlockItemModelJson(blockId));
                }

                // Note: fluid presets don't need generated block/item models here -
                // fluid still/flowing textures are referenced directly in the fluid registry script.
            }
        }
    }

    private static JsonObject createBlockstateJson(String blockId) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();

        defaultVariant.addProperty("model", "kubejs:block/" + blockId);
        variants.add("", defaultVariant);
        root.add("variants", variants);

        return root;
    }

    private static JsonObject createBlockModelJson(String baseTexture, String overlayTexture, String particleTexture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        root.addProperty("render_type", "minecraft:cutout");

        JsonObject textures = new JsonObject();
        textures.addProperty("particle", particleTexture);
        textures.addProperty("base", baseTexture);
        textures.addProperty("bloom", overlayTexture);
        root.add("textures", textures);

        JsonArray elements = new JsonArray();
        elements.add(createCubeElement("#base", -1));
        elements.add(createCubeElement("#bloom", 1));
        root.add("elements", elements);
        return root;
    }

    private static JsonObject createBlockItemModelJson(String blockId) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "kubejs:block/" + blockId);
        return root;
    }

    private static JsonObject createCubeElement(String textureKey, int tintIndex) {
        JsonObject element = new JsonObject();

        JsonArray from = new JsonArray();
        from.add(0); from.add(0); from.add(0);
        element.add("from", from);

        JsonArray to = new JsonArray();
        to.add(16); to.add(16); to.add(16);
        element.add("to", to);

        JsonObject faces = new JsonObject();
        String[] directions = {"down", "up", "north", "south", "west", "east"};

        for (String dir : directions) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", textureKey);
            face.addProperty("cullface", dir);

            if (tintIndex >= 0) {
                face.addProperty("tintindex", tintIndex);
            }
            faces.add(dir, face);
        }

        element.add("faces", faces);
        return element;
    }

    private static void writeJson(File file, JsonObject json) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
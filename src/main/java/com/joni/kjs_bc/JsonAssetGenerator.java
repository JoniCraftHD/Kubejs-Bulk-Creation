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

        blockstatesDir.mkdirs();
        blockModelsDir.mkdirs();
        itemModelsDir.mkdirs();

        for (BulkBuilderLogic.BulkCreation creation : BulkBuilderLogic.getCreations()) {
            List<String> resolvedPresetIds = BulkBuilderLogic.getResolvedPresetIds(creation);

            for (String presetId : resolvedPresetIds) {
                BulkBuilderLogic.Preset preset = BulkBuilderLogic.getPresets().get(presetId);

                if (preset != null) {
                    String fullId = creation.id() + preset.idSuffix();

                    // --- BLOCK MODEL GENERATION ---
                    if ("block".equalsIgnoreCase(preset.type())) {
                        String baseTex = preset.baseTexture().isEmpty() ? "minecraft:block/stone" : preset.baseTexture();
                        String overlayTex = (creation.overlayOverride() != null && !creation.overlayOverride().isEmpty())
                                ? creation.overlayOverride()
                                : (!preset.overlays().isEmpty() ? preset.overlays().get(0) : "kubejs:block/ore_overlay");

                        writeJson(new File(blockstatesDir, fullId + ".json"), createBlockstateJson(fullId));
                        writeJson(new File(blockModelsDir, fullId + ".json"), createBlockModelJson(baseTex, overlayTex, baseTex));
                        writeJson(new File(itemModelsDir, fullId + ".json"), createBlockItemModelJson(fullId));
                    }

                    // --- ITEM MODEL GENERATION (Multi-Layer / Overlays) ---
                    else if ("item".equalsIgnoreCase(preset.type())) {
                        if (!preset.overlays().isEmpty()) {
                            // Creates model with layer0 (base) and layer1 (overlay)
                            writeJson(new File(itemModelsDir, fullId + ".json"),
                                    createLayeredItemModelJson(preset.baseTexture(), preset.overlays().get(0)));
                        }
                    }
                }
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
        elements.add(createCubeElement("#base", 0));
        elements.add(createCubeElement("#bloom", 1));
        root.add("elements", elements);
        return root;
    }

    private static JsonObject createBlockItemModelJson(String blockId) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "kubejs:block/" + blockId);
        return root;
    }

    // Generates item/generated model with layer0 and layer1 for items with overlays
    private static JsonObject createLayeredItemModelJson(String baseTexture, String overlayTexture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", baseTexture);
        textures.addProperty("layer1", overlayTexture);
        root.add("textures", textures);

        return root;
    }

    private static JsonObject createCubeElement(String textureKey, int tintIndex) {
        JsonObject element = new JsonObject();
        JsonArray from = new JsonArray(); from.add(0); from.add(0); from.add(0); element.add("from", from);
        JsonArray to = new JsonArray(); to.add(16); to.add(16); to.add(16); element.add("to", to);

        JsonObject faces = new JsonObject();
        for (String dir : new String[]{"down", "up", "north", "south", "west", "east"}) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", textureKey);
            face.addProperty("cullface", dir);
            if (tintIndex >= 0) face.addProperty("tintindex", tintIndex);
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
package com.joni.kjs_bc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            for (String presetId : creation.presetIds()) {
                BulkBuilderLogic.Preset preset = BulkBuilderLogic.getPresets().get(presetId);

                if (preset != null && "block".equalsIgnoreCase(preset.type())) {
                    String blockId = creation.id() + "_" + presetId.toLowerCase().replace("ore_", "");

                    String baseTex = preset.baseTexture().isEmpty() ? "minecraft:block/stone" : preset.baseTexture();
                    String overlayTex = (!preset.overlays().isEmpty()) ? preset.overlays().get(0) : "kubejs:block/ore_overlay";

                    // Gebackene Partikel-Textur erzeugen (Base + getöntes Overlay, fest verschmolzen)
                    String particleTexId = "kubejs:block/particle/" + blockId + "_particle";
                    File particleFile = new File(particleTexturesDir, blockId + "_particle.png");
                    TextureGenerator.generateCompositeTexture(baseTex, overlayTex, creation.tintColor(), particleFile);

                    // 1. Blockstate-JSON
                    writeJson(new File(blockstatesDir, blockId + ".json"), createBlockstateJson(blockId));

                    // 2. 3D-Block-Model-JSON (particle zeigt jetzt auf die gebackene Textur!)
                    writeJson(new File(blockModelsDir, blockId + ".json"), createBlockModelJson(baseTex, overlayTex, particleTexId));

                    // 3. 3D-Block-Item Model (erbt direkt vom 3D Block-Model)
                    writeJson(new File(itemModelsDir, blockId + ".json"), createBlockItemModelJson(blockId));
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
        // Partikel nutzen jetzt die gebackene, statische Composite-Textur
        textures.addProperty("particle", particleTexture);
        textures.addProperty("base", baseTexture);
        textures.addProperty("bloom", overlayTexture);
        root.add("textures", textures);

        JsonArray elements = new JsonArray();

        // Layer 1: Stein-Untergrund, kein Tint (tintindex -1 -> wird weggelassen)
        elements.add(createCubeElement("#base", -1));

        // Layer 2: Erz-Overlay, tintindex 1 (nicht 0!) damit Partikel-Tinting nicht mehr greift
        elements.add(createCubeElement("#bloom", 1));

        root.add("elements", elements);
        return root;
    }

    // Zeigt im Inventar als 3D-Würfel an
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
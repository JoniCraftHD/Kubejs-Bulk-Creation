package com.joni.kjs_bc;

import java.util.*;

public class BulkBuilderLogic {

    private static final Map<String, Preset> PRESETS = new HashMap<>();
    private static final List<BulkCreation> CREATIONS = new ArrayList<>();

    public static PresetBuilder create_preset(String presetId) {
        return new PresetBuilder(presetId);
    }

    public static class PresetBuilder {
        private final String id;
        private String presetType = "item";
        private String baseTexture = "";
        private final List<String> overlays = new ArrayList<>();
        private boolean noItem = false;
        private String dropPresetId = null;
        private String soundType = "stone";

        // Block Optionen
        private float hardness = 3.0f;
        private float resistance = 3.0f;
        private int lightLevel = 0;
        private boolean hasGravity = false;
        private boolean opaque = true;
        private boolean fullBlock = true;
        private boolean requiresTool = true;

        // Item Optionen
        private int maxStackSize = 64;
        private String rarity = "common";

        // Tags
        private final List<String> blockTags = new ArrayList<>();
        private final List<String> itemTags = new ArrayList<>();
        private final List<String> bothTags = new ArrayList<>();

        public PresetBuilder(String id) {
            this.id = id;
        }

        public PresetBuilder presetType(String type) { this.presetType = type; return this; }
        public PresetBuilder texture(String texture) { this.baseTexture = texture; return this; }
        public PresetBuilder overlay(String overlayPath) { this.overlays.add(overlayPath); return this; }
        public PresetBuilder noItem() { this.noItem = true; return this; }
        public PresetBuilder dropsPreset(String presetId) { this.dropPresetId = presetId; return this; }
        public PresetBuilder soundType(String sound) { this.soundType = sound; return this; }

        // Neue Eigenschaften
        public PresetBuilder hardness(float hardness) { this.hardness = hardness; return this; }
        public PresetBuilder resistance(float resistance) { this.resistance = resistance; return this; }
        public PresetBuilder lightLevel(int light) { this.lightLevel = light; return this; }
        public PresetBuilder hasGravity(boolean gravity) { this.hasGravity = gravity; return this; }
        public PresetBuilder opaque(boolean opaque) { this.opaque = opaque; return this; }
        public PresetBuilder fullBlock(boolean fullBlock) { this.fullBlock = fullBlock; return this; }
        public PresetBuilder requiresTool(boolean requiresTool) { this.requiresTool = requiresTool; return this; }

        public PresetBuilder maxStackSize(int size) { this.maxStackSize = size; return this; }
        public PresetBuilder rarity(String rarity) { this.rarity = rarity; return this; }

        public PresetBuilder tagBlock(String tag) { this.blockTags.add(tag); return this; }
        public PresetBuilder tagItem(String tag) { this.itemTags.add(tag); return this; }
        public PresetBuilder tagBoth(String tag) { this.bothTags.add(tag); return this; }

        public void build() {
            PRESETS.put(id, new Preset(
                    id, presetType, baseTexture, overlays, noItem, dropPresetId, soundType,
                    hardness, resistance, lightLevel, hasGravity, opaque, fullBlock, requiresTool,
                    maxStackSize, rarity, blockTags, itemTags, bothTags
            ));
            System.out.println("[BulkCreation] Preset gespeichert: " + id);
        }
    }

    public static BulkCreationBuilder bulkCreate(String id) {
        return new BulkCreationBuilder(id);
    }

    public static class BulkCreationBuilder {
        private final String id;
        private String displayName;
        private String tintColor = "#FFFFFF";
        private String formula = null;
        private final List<String> presetIds = new ArrayList<>();

        public BulkCreationBuilder(String id) {
            this.id = id;
            this.displayName = id;
        }

        public BulkCreationBuilder displayName(String name) { this.displayName = name; return this; }
        public BulkCreationBuilder formula(String formula) { this.formula = formula; return this; }
        public BulkCreationBuilder presets(String... presets) { this.presetIds.addAll(Arrays.asList(presets)); return this; }
        public BulkCreationBuilder tintOverlay(String hexColor) { this.tintColor = hexColor; return this; }

        public void build() {
            CREATIONS.add(new BulkCreation(id, displayName, tintColor, formula, presetIds));
        }
    }

    // Records
    public record Preset(
            String id, String type, String baseTexture, List<String> overlays, boolean noItem, String dropPresetId, String soundType,
            float hardness, float resistance, int lightLevel, boolean hasGravity, boolean opaque, boolean fullBlock, boolean requiresTool,
            int maxStackSize, String rarity, List<String> blockTags, List<String> itemTags, List<String> bothTags
    ) {}

    public record BulkCreation(String id, String displayName, String tintColor, String formula, List<String> presetIds) {}

    public static Map<String, Preset> getPresets() { return PRESETS; }
    public static List<BulkCreation> getCreations() { return CREATIONS; }
}
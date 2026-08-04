package com.joni.kjs_bc;

import java.util.*;

public class BulkBuilderLogic {

    private static final Map<String, Preset> PRESETS = new HashMap<>();
    private static final Map<String, Bundle> BUNDLES = new HashMap<>();
    private static final List<BulkCreation> CREATIONS = new ArrayList<>();

    public enum TintTarget {
        BASE,
        OVERLAY,
        BOTH,
        NONE
    }

    // ==========================================
    // PRESETS
    // ==========================================

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
        private TintTarget tintTarget = TintTarget.OVERLAY;

        // Naming
        private String namePrefix = "";
        private String nameSuffix = "";
        private String idSuffix;

        // Block & Item options
        private float hardness = 3.0f;
        private float resistance = 3.0f;
        private int lightLevel = 0;
        private boolean hasGravity = false;
        private boolean opaque = true;
        private boolean fullBlock = true;
        private boolean requiresTool = true;
        private int maxStackSize = 64;
        private String rarity = "common";

        // Fluid-specific options
        private String flowingTexture = null;
        private int luminosity = 0;

        // Tags
        private final List<String> blockTags = new ArrayList<>();
        private final List<String> itemTags = new ArrayList<>();
        private final List<String> bothTags = new ArrayList<>();

        // Dependencies
        private final List<String> dependsOn = new ArrayList<>();

        public PresetBuilder(String id) {
            this.id = id;
            this.idSuffix = "_" + id.toLowerCase();
        }

        public PresetBuilder presetType(String type) {
            this.presetType = type;
            return this;
        }

        public PresetBuilder texture(String texture) {
            this.baseTexture = texture;
            return this;
        }

        public PresetBuilder overlay(String overlayPath) {
            this.overlays.add(overlayPath);
            return this;
        }

        public PresetBuilder noItem() {
            this.noItem = true;
            return this;
        }

        public PresetBuilder dropsPreset(String presetId) {
            this.dropPresetId = presetId;
            return this;
        }

        public PresetBuilder soundType(String sound) {
            this.soundType = sound;
            return this;
        }

        // Tint options
        public PresetBuilder tintBase() {
            this.tintTarget = TintTarget.BASE;
            return this;
        }

        public PresetBuilder tintOverlay() {
            this.tintTarget = TintTarget.OVERLAY;
            return this;
        }

        public PresetBuilder tintBoth() {
            this.tintTarget = TintTarget.BOTH;
            return this;
        }

        public PresetBuilder tintNone() {
            this.tintTarget = TintTarget.NONE;
            return this;
        }

        public PresetBuilder namePrefix(String prefix) {
            this.namePrefix = prefix;
            return this;
        }

        public PresetBuilder nameSuffix(String suffix) {
            this.nameSuffix = suffix;
            return this;
        }

        public PresetBuilder idSuffix(String suffix) {
            this.idSuffix = suffix;
            return this;
        }

        public PresetBuilder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public PresetBuilder resistance(float resistance) {
            this.resistance = resistance;
            return this;
        }

        public PresetBuilder lightLevel(int light) {
            this.lightLevel = light;
            return this;
        }

        public PresetBuilder hasGravity(boolean gravity) {
            this.hasGravity = gravity;
            return this;
        }

        public PresetBuilder opaque(boolean opaque) {
            this.opaque = opaque;
            return this;
        }

        public PresetBuilder fullBlock(boolean fullBlock) {
            this.fullBlock = fullBlock;
            return this;
        }

        public PresetBuilder requiresTool(boolean requiresTool) {
            this.requiresTool = requiresTool;
            return this;
        }

        public PresetBuilder maxStackSize(int size) {
            this.maxStackSize = size;
            return this;
        }

        public PresetBuilder rarity(String rarity) {
            this.rarity = rarity;
            return this;
        }

        public PresetBuilder tagBlock(String tag) {
            this.blockTags.add(tag);
            return this;
        }

        public PresetBuilder tagItem(String tag) {
            this.itemTags.add(tag);
            return this;
        }

        public PresetBuilder tagBoth(String tag) {
            this.bothTags.add(tag);
            return this;
        }

        public PresetBuilder flowingTexture(String texture) {
            this.flowingTexture = texture;
            return this;
        }

        public PresetBuilder luminosity(int luminosity) {
            this.luminosity = luminosity;
            return this;
        }

        public PresetBuilder dependsOn(String... presetIds) {
            this.dependsOn.addAll(Arrays.asList(presetIds));
            return this;
        }

        public void build() {
            String finalFlow = (flowingTexture != null && !flowingTexture.isEmpty())
                    ? flowingTexture
                    : baseTexture;

            PRESETS.put(id, new Preset(
                    id, presetType, baseTexture, overlays, noItem, dropPresetId, soundType, tintTarget,
                    namePrefix, nameSuffix, idSuffix,
                    hardness, resistance, lightLevel, hasGravity, opaque, fullBlock, requiresTool,
                    maxStackSize, rarity, blockTags, itemTags, bothTags,
                    finalFlow, luminosity, dependsOn
            ));
            System.out.println("[BulkCreation] Preset saved: " + id);
        }
    }

    // ==========================================
    // BUNDLES
    // ==========================================

    public static BundleBuilder create_bundle(String bundleId) {
        return new BundleBuilder(bundleId);
    }

    public static class BundleBuilder {
        private final String id;
        private final List<String> presetIds = new ArrayList<>();

        public BundleBuilder(String id) {
            this.id = id;
        }

        public BundleBuilder presets(String... presets) {
            this.presetIds.addAll(Arrays.asList(presets));
            return this;
        }

        public void build() {
            BUNDLES.put(id, new Bundle(id, presetIds));
            System.out.println("[BulkCreation] Bundle saved: " + id);
        }
    }

    public record Bundle(String id, List<String> presetIds) {}

    // ==========================================
    // BULK CREATIONS
    // ==========================================

    public static BulkCreationBuilder bulkCreate(String id) {
        return new BulkCreationBuilder(id);
    }

    public static class BulkCreationBuilder {
        private final String id;
        private String displayName;
        private String tintColor = "#FFFFFF";
        private Object tooltip = null;
        private final List<String> presetIds = new ArrayList<>();
        private String overlayOverride = null;
        private String dropsOverride = null;

        public BulkCreationBuilder(String id) {
            this.id = id;
            this.displayName = id;
        }

        public BulkCreationBuilder displayName(String name) {
            this.displayName = name;
            return this;
        }

        public BulkCreationBuilder tooltip(Object tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public BulkCreationBuilder presets(String... presets) {
            this.presetIds.addAll(Arrays.asList(presets));
            return this;
        }

        public BulkCreationBuilder tintOverlay(String hexColor) {
            this.tintColor = hexColor;
            return this;
        }

        public BulkCreationBuilder addOverlayAllBlocks(String overlayTexture) {
            this.overlayOverride = overlayTexture;
            return this;
        }

        public BulkCreationBuilder dropsOverride(String dropsPresetId) {
            this.dropsOverride = dropsPresetId;
            return this;
        }

        public BulkCreationBuilder dropTarget(String dropsPresetId) {
            this.dropsOverride = dropsPresetId;
            return this;
        }

        public void build() {
            CREATIONS.add(new BulkCreation(id, displayName, tintColor, tooltip, presetIds, overlayOverride, dropsOverride));
        }
    }

    // ==========================================
    // RECORDS
    // ==========================================

    public record Preset(
            String id, String type, String baseTexture, List<String> overlays, boolean noItem, String dropPresetId, String soundType, TintTarget tintTarget,
            String namePrefix, String nameSuffix, String idSuffix,
            float hardness, float resistance, int lightLevel, boolean hasGravity, boolean opaque, boolean fullBlock, boolean requiresTool,
            int maxStackSize, String rarity, List<String> blockTags, List<String> itemTags, List<String> bothTags,
            String flowingTexture, int luminosity, List<String> dependsOn
    ) {}

    public record BulkCreation(
            String id, String displayName, String tintColor, Object tooltip, List<String> presetIds, String overlayOverride, String dropsOverride
    ) {
        public int tintColorInt() {
            try {
                String hex = tintColor.replace("#", "");
                return Integer.parseInt(hex, 16);
            } catch (Exception e) {
                return 0xFFFFFF;
            }
        }
    }

    // ==========================================
    // RESOLUTION
    // ==========================================

    public static List<String> resolvePresetIds(List<String> rawIds) {
        LinkedHashSet<String> resolved = new LinkedHashSet<>();
        for (String id : rawIds) {
            resolveRecursive(id, resolved, new HashSet<>());
        }
        return new ArrayList<>(resolved);
    }

    private static void resolveRecursive(String id, LinkedHashSet<String> resolved, Set<String> visiting) {
        if (visiting.contains(id)) {
            System.err.println("[BulkCreation] Circular preset/bundle reference detected at: " + id);
            return;
        }
        visiting.add(id);

        if (BUNDLES.containsKey(id)) {
            for (String sub : BUNDLES.get(id).presetIds()) {
                resolveRecursive(sub, resolved, visiting);
            }
        } else if (PRESETS.containsKey(id)) {
            Preset preset = PRESETS.get(id);

            if (preset.dropPresetId() != null && !preset.dropPresetId().isEmpty()) {
                resolveRecursive(preset.dropPresetId(), resolved, visiting);
            }

            for (String dep : preset.dependsOn()) {
                resolveRecursive(dep, resolved, visiting);
            }

            resolved.add(id);
        } else {
            System.err.println("[BulkCreation] Unknown preset/bundle id referenced: " + id);
        }

        visiting.remove(id);
    }

    public static List<String> getResolvedPresetIds(BulkCreation creation) {
        List<String> raw = new ArrayList<>(creation.presetIds());

        if (creation.dropsOverride() != null && !creation.dropsOverride().isEmpty()) {
            raw.add(creation.dropsOverride());
        }
        return resolvePresetIds(raw);
    }

    public static Map<String, Preset> getPresets() { return PRESETS; }
    public static Map<String, Bundle> getBundles() { return BUNDLES; }
    public static List<BulkCreation> getCreations() { return CREATIONS; }
}
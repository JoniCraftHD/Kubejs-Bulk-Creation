package com.joni.kjs_bc;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;

public class BulkCreationKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void init() {
        System.out.println("[BulkCreation] Generiere dynamische Block-JSONs...");

        JsonAssetGenerator.generateAssetsForCreations();
    }
}
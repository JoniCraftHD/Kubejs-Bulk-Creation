package com.joni.kjs_bc;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(KubejsBulkcreation.MODID)
public class KubejsBulkcreation {
    public static final String MODID = "kjs_bc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KubejsBulkcreation(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("KubeJS BulkCreation Initialized!");
    }
}
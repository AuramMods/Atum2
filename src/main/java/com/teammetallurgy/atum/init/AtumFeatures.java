package com.teammetallurgy.atum.init;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.world.gen.feature.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Atum.MOD_ID)
public class AtumFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES_DEFERRED = DeferredRegister.create(ForgeRegistries.FEATURES, Atum.MOD_ID);

    //Features
    public static final RegistryObject<BonusCrateFeature> BONUS_CRATE = register("bonus_crate", new BonusCrateFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<StartStructureFeature> START_STRUCTURE = register("start_structure", new StartStructureFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LimestoneDungeonsFeature> LIMESTONE_DUNGEONS = register("limestone_dungeon", new LimestoneDungeonsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<DirtyBoneFossilsFeature> DIRTY_BONE_FOSSILS = register("dirty_bone_fossil", new DirtyBoneFossilsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<DeadwoodFeature> DEADWOOD_TREE = register("deadwood_tree", new DeadwoodFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LimestoneSpikeFeature> LIMESTONE_SPIKE = register("limestone_spike", new LimestoneSpikeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<AnputsFingersFeature> ANPUTS_FINGERS = register("anputs_fingers", new AnputsFingersFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SAND_LAYER = register("sand_layer", new SandLayerFeature(NoneFeatureConfiguration.CODEC));

    //Configured Feature Resource Keys (Only use when in-code references is needed)
    public static final ResourceKey<ConfiguredFeature<?, ?>> PALM_TREE = registerConfiguredKey("palm_tree");

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name, F feature) {
        return FEATURES_DEFERRED.register(name, () -> feature);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerConfiguredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Atum.MOD_ID, name));
    }
}

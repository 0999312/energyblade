package cn.mmf.energyblade.data;

import java.util.List;

import cn.mmf.energyblade.Energyblade;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.slashblade.PropertiesDefinition;
import mods.flammpfeil.slashblade.registry.slashblade.RenderDefinition;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BuiltInSlashBladeRegistry {
	public static final ResourceKey<SlashBladeDefinition> HF_BLADE = register("hf_blade");

	public static void registerAll(BootstapContext<SlashBladeDefinition> bootstrap) {

		bootstrap.register(HF_BLADE, new SlashBladeDefinition(Energyblade.FORGE_ENERGY_BLADE.getId(),
				ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "hf_blade"),
				RenderDefinition.Builder.newInstance()
						.textureName(ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "model/hf_blade.png"))
						.modelName(ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "model/hf_blade.obj"))
						.standbyRenderType(CarryType.PSO2).build(),
				PropertiesDefinition.Builder.newInstance().baseAttackModifier(7.0F).maxDamage(50)
						.defaultSwordType(List.of(SwordType.BEWITCHED))
						.slashArtsType(SlashArtsRegistry.CIRCLE_SLASH.getId()).build(),
				List.of()));

	}

	private static ResourceKey<SlashBladeDefinition> register(String id) {
		ResourceKey<SlashBladeDefinition> loc = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY,
				ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, id));
		return loc;
	}
}

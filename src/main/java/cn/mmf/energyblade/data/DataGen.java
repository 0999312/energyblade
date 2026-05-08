package cn.mmf.energyblade.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import cn.mmf.energyblade.Energyblade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGen {
	@SubscribeEvent
	public static void dataGen(GatherDataEvent event) {
		DataGenerator dataGenerator = event.getGenerator();
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
		PackOutput packOutput = dataGenerator.getPackOutput();

		dataGenerator.addProvider(event.includeServer(), new SlashBladeRecipeProvider(packOutput, lookupProvider));


		final RegistrySetBuilder bladeBuilder = new RegistrySetBuilder().add(SlashBladeDefinition.REGISTRY_KEY,
				BuiltInSlashBladeRegistry::registerAll);

		
		dataGenerator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider,
				bladeBuilder, Set.of(Energyblade.MODID)) {

			@Override
			public String getName() {
				return "SlashBlade Definition Registry";
			}

		});

	}
}

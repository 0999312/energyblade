package mod.arcomit.energyblade;

import com.mojang.logging.LogUtils;
import mod.arcomit.energyblade.item.ItemFEBlade;
import mods.flammpfeil.slashblade.item.ItemTierSlashBlade;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Energyblade.MODID)
public class Energyblade {
    public static final String MODID = "energyblade";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // FE能量拔刀剑
    public static final RegistryObject<Item> FORGE_ENERGY_BLADE = ITEMS.register("forge_energy_blade",
            () -> new ItemFEBlade(new ItemTierSlashBlade(40, 4F), 4, -2.4F, (new Item.Properties())));

    public Energyblade() {
        LOGGER.info("Energyblade is loaded!");
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}

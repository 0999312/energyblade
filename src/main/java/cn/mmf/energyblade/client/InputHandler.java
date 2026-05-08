package cn.mmf.energyblade.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import cn.mmf.energyblade.Energyblade;
import cn.mmf.energyblade.NetworkPacketHandler;
import cn.mmf.energyblade.PowerSwitchPacket;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)
@OnlyIn(Dist.CLIENT)
public class InputHandler {
    public static final KeyMapping KEY_CHARGE = new KeyMapping("key.energyblade.charge_switch",
            KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V,
            "key.category.slashblade");
    
	@OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onPlayerPostTick(InputEvent.Key event) {
        @SuppressWarnings("resource")
		LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)
        	return;
        
        if (player.getMainHandItem().isEmpty() || !player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent())
            return;

        if(InputHandler.KEY_CHARGE.isDown()) {
        	PacketDistributor.sendToServer(new PowerSwitchPacket("triggered"));
        }
	}
}

package cn.mmf.energyblade;

import cn.mmf.energyblade.energy.FEBladeStorage;
import io.netty.buffer.ByteBuf;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PowerSwitchPacket(String message) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PowerSwitchPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "power_switch"));

    public static final StreamCodec<ByteBuf, PowerSwitchPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    PowerSwitchPacket::message,
                    PowerSwitchPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PowerSwitchPacket data, final IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.isSpectator())
            return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getCapability(ItemSlashBlade.BLADESTATE) == null)
            return;

        var energy = mainHandItem.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy instanceof FEBladeStorage bladeFE) {
            if (!bladeFE.isPowered()
                    && bladeFE.extractEnergy(bladeFE.getPowerupExtract(), true) == bladeFE.getPowerupExtract()) {
                bladeFE.extractEnergy(bladeFE.getPowerupExtract(), false);
                bladeFE.setPowered(true);
                ServerLevel serverLevel = player.serverLevel();
                var random = serverLevel.random;
                for (int i = 0; i < 32; ++i) {
                    double xDist = (random.nextFloat() * 2.0F - 1.0F);
                    double yDist = (random.nextFloat() * 2.0F - 1.0F);
                    double zDist = (random.nextFloat() * 2.0F - 1.0F);
                    if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
                        double x = player.getX(xDist / 4.0D);
                        double y = player.getY(0.5D + yDist / 4.0D);
                        double z = player.getZ(zDist / 4.0D);
                        serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 0, xDist, yDist + 0.2D, zDist, 1);
                    }
                }
                player.playNotifySound(SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.5F, 1F);
            } else {
                bladeFE.setPowered(false);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1F, 1F);
            }
        }
    }
}

package com.aranaira.arcanearchives.items.gems.pampel;

import com.aranaira.arcanearchives.init.ItemRegistry;
import com.aranaira.arcanearchives.items.gems.ArcaneGemItem;
import com.aranaira.arcanearchives.items.gems.GemUtil;
import com.aranaira.arcanearchives.items.gems.GemUtil.AvailableGemsHandler;
import com.aranaira.arcanearchives.items.gems.GemUtil.GemStack;
import com.aranaira.arcanearchives.network.Networking;
import com.aranaira.arcanearchives.network.PacketArcaneGems.GemParticle;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class Elixirspindle extends ArcaneGemItem {
	public static final String NAME = "elixirspindle";

	public Elixirspindle () {
		super(NAME, GemCut.PAMPEL, GemColor.PURPLE, 5, 20);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation (ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("arcanearchives.tooltip.gemcharge") + ": " + getTooltipData(stack));
		tooltip.add(TextFormatting.GOLD + I18n.format("arcanearchives.tooltip.gem.elixirspindle"));
		tooltip.add(TextFormatting.GOLD + I18n.format("arcanearchives.tooltip.gem.recharge.elixirspindle"));
	}

	@Override
	public boolean doesSneakBypassUse (ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick (World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) {
			AvailableGemsHandler handler = GemUtil.getHeldGem(player, hand);
			GemStack gem = handler.getHeld();
			recharge(world, player, gem);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public boolean recharge (World world, EntityPlayer player, GemStack gem) {
		if (gem != null && GemUtil.getCharge(gem) == 0 && gem.getItem() == ItemRegistry.ELIXIRSPINDLE) {
			for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
				ItemStack stack = player.inventory.mainInventory.get(i);
				if (stack.getItem() == Items.NETHER_WART) {
					int numConsumed = Math.min(stack.getCount(), 5);
					informPlayerOfItemConsumption(player, gem, stack, numConsumed);
					GemUtil.restoreCharge(gem, numConsumed);
					stack.shrink(numConsumed);
					//TODO: Play a particle effect
					Vec3d pos = player.getPositionVector().add(0, 1, 0);
					GemParticle packet = new GemParticle(cut, color, pos, pos);
					Networking.sendToAllTracking(packet, player);
					return true;
				}
			}
		}

		return tryRechargingWithPowder(world, player, gem);
	}
}

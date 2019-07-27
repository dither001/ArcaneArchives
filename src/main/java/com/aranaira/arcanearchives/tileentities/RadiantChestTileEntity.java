package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.ArcaneArchives;
import com.aranaira.arcanearchives.config.ConfigHandler;
import com.aranaira.arcanearchives.data.ServerNetwork;
import com.aranaira.arcanearchives.init.BlockRegistry;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.inventory.handlers.ITrackingHandler;
import com.aranaira.arcanearchives.tileentities.interfaces.IBrazierRouting;
import com.aranaira.arcanearchives.tileentities.interfaces.IManifestTileEntity;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class RadiantChestTileEntity extends ImmanenceTileEntity implements IManifestTileEntity, IBrazierRouting {
	private final TrackingExtendedItemStackHandler inventory = new TrackingExtendedItemStackHandler(54);
	private ItemStack displayStack = ItemStack.EMPTY;
	private EnumFacing displayFacing = EnumFacing.NORTH;
	private BrazierRoutingType routingType = BrazierRoutingType.ANY;
	public String chestName = "";

	public RadiantChestTileEntity () {
		super("radiantchest");
	}

	@Override
	public void firstJoinedNetwork (ServerNetwork network) {
		super.firstJoinedNetwork(network);

		// TODO: This might cause problems in the future with chests randomly changing behaviour when
		// loaded or unloaded.
		if (network.getNoNewDefault()) {
			this.routingType = BrazierRoutingType.NO_NEW_STACKS;
			defaultServerSideUpdate();
		}
	}

	public Int2IntOpenHashMap getOrCalculateReference (boolean force) {
		if (force) {
			inventory.manualRecount();
		}
		return inventory.getItemReference();
	}

	@Override
	public Int2IntOpenHashMap getOrCalculateReference () {
		return getOrCalculateReference(false);
	}

	@Override
	public BrazierRoutingType getRoutingType () {
		return routingType;
	}

	@Override
	public int totalEmptySlots () {
		return inventory.getEmptyCount();
	}

	@Override
	public int totalSlots () {
		return inventory.getSlots();
	}

	@Override
	public int slotMultiplier () {
		return ConfigHandler.RadiantMultiplier;
	}

	@Override
	public ItemStack acceptStack (ItemStack stack) {
		ItemStack result = ItemHandlerHelper.insertItemStacked(this.inventory, stack, false);
		this.markDirty();
		return result;
	}

	public void toggleRoutingType () {
		if (routingType == BrazierRoutingType.ANY) {
			this.routingType = BrazierRoutingType.NO_NEW_STACKS;
		} else {
			this.routingType = BrazierRoutingType.ANY;
		}
		markDirty();
	}

	public ItemStack getDisplayStack () {
		return displayStack;
	}

	public void setDisplay (ItemStack displayStack, EnumFacing facing) {
		this.displayStack = displayStack;
		this.displayFacing = facing;
		defaultServerSideUpdate();
	}

	public EnumFacing getDisplayFacing () {
		return displayFacing;
	}

	@Override
	public String getDescriptor () {
		if (getChestName().isEmpty()) {
			return "Chest";
		} else {
			return "Chest: " + getChestName();
		}
	}

	@Override
	public String getChestName () {
		return chestName;
	}

	public void setChestName (String newName) {
		this.chestName = newName;
	}

	@Override
	public TrackingExtendedItemStackHandler getInventory () {
		return inventory;
	}

	@Override
	@Nonnull
	public SPacketUpdateTileEntity getUpdatePacket () {
		NBTTagCompound compound = writeToNBT(new NBTTagCompound());

		return new SPacketUpdateTileEntity(pos, 0, compound);
	}

	@Override
	public NBTTagCompound getUpdateTag () {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	@Nonnull
	public NBTTagCompound writeToNBT (NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag(AATileEntity.Tags.INVENTORY, inventory.serializeNBT());
		if (chestName != null && !chestName.isEmpty()) {
			compound.setString(Tags.CHEST_NAME, chestName);
		}
		compound.setInteger(Tags.DISPLAY_FACING, displayFacing.getIndex());
		if (!displayStack.isEmpty()) {
			compound.setTag(Tags.DISPLAY_STACK, displayStack.serializeNBT());
		}
		compound.setInteger(Tags.ROUTING_TYPE, routingType.ordinal());

		return compound;
	}

	@Override
	public void readFromNBT (NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (!compound.hasKey(AATileEntity.Tags.INVENTORY)) {
			ArcaneArchives.logger.info(String.format("Radiant Chest tile entity at %d/%d/%d is missing its inventory.", pos.getX(), pos.getY(), pos.getZ()));
		}
		inventory.deserializeNBT(compound.getCompoundTag(AATileEntity.Tags.INVENTORY));

		if (compound.hasKey(Tags.CHEST_NAME)) {
			chestName = compound.getString(Tags.CHEST_NAME);
		}
		displayFacing = EnumFacing.byIndex(compound.getInteger(Tags.DISPLAY_FACING));
		if (compound.hasKey(Tags.DISPLAY_STACK)) {
			displayStack = new ItemStack(compound.getCompoundTag(Tags.DISPLAY_STACK));
		}
		routingType = BrazierRoutingType.fromInt(compound.getInteger(Tags.ROUTING_TYPE));
	}

	@Override
	public void onDataPacket (NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
	}

	@Override
	public boolean hasCapability (@Nonnull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability (@Nonnull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		}
		return super.getCapability(capability, facing);
	}

	public int countEmptySlots () {
		int empty = 0;
		for (int i = 0; i < inventory.getSlots(); i++) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				empty++;
			}
		}
		return empty;
	}

	public static class Tags {
		public static final String CHEST_NAME = "chestName";
		public static final String DISPLAY_STACK = "displayStack";
		public static final String DISPLAY_FACING = "displayFacing";
		public static final String ROUTING_TYPE = "routingType";

		private Tags () {
		}
	}

	public class TrackingExtendedItemStackHandler extends ExtendedItemStackHandler implements ITrackingHandler {
		private Int2IntOpenHashMap itemReference = new Int2IntOpenHashMap();
		private int emptySlots = 0;

		public TrackingExtendedItemStackHandler (int size) {
			super(size);
			itemReference.defaultReturnValue(0);
		}

		@Override
		public void deserializeNBT (NBTTagCompound nbt) {
			super.deserializeNBT(nbt);
			if (RadiantChestTileEntity.this.world == null || !RadiantChestTileEntity.this.world.isRemote) {
				manualRecount();
			}
		}

		public Int2IntOpenHashMap getItemReference () {
			return itemReference;
		}

		@Override
		public int totalSlots () {
			return getSlots();
		}

		@Override
		public int getEmptyCount () {
			return emptySlots;
		}

		@Override
		public void setEmptyCount (int amount) {
			this.emptySlots = amount;
		}

		@Override
		public void incrementEmptyCount () {
			this.emptySlots++;
		}

		@Override
		public void decrementEmptyCount () {
			this.emptySlots--;
		}

		@Override
		public void setStackInSlot (int slot, @Nonnull ItemStack stack) {
			ItemStack inSlot = getStackInSlot(slot);
			if (inSlot.isEmpty()) {
				decrementEmptyCount();
			}
			subtraction(inSlot, -1);
			super.setStackInSlot(slot, stack);
			addition(stack, ItemStack.EMPTY);
			world.updateComparatorOutputLevel(pos, BlockRegistry.RADIANT_CHEST);
			if (stack.isEmpty()) {
				incrementEmptyCount();
			}
			markDirty();
		}

		@Nonnull
		@Override
		public ItemStack insertItem (int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (!simulate) {
				ItemStack inSlot = getStackInSlot(slot);
				if (inSlot.isEmpty()) {
					decrementEmptyCount();
				}
				ItemStack test = super.insertItem(slot, stack, true);
				addition(stack, test);
			}
			ItemStack result = super.insertItem(slot, stack, simulate);
			world.updateComparatorOutputLevel(pos, BlockRegistry.RADIANT_CHEST);
			markDirty();
			return result;
		}

		@Nonnull
		@Override
		public ItemStack extractItem (int slot, int amount, boolean simulate) {
			if (!simulate) {
				ItemStack test = getStackInSlot(slot);
				subtraction(test, amount);
			}
			ItemStack result = super.extractItem(slot, amount, simulate);
			if (!simulate) {
				ItemStack inSlot = getStackInSlot(slot);
				if (inSlot.isEmpty()) {
					incrementEmptyCount();
				}
			}
			world.updateComparatorOutputLevel(pos, BlockRegistry.RADIANT_CHEST);
			markDirty();
			return result;
		}
	}
}

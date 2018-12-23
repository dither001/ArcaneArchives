package com.aranaira.arcanearchives.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ManifestItemHandler implements IItemHandlerModifiable 
{
	private UUID mPlayerID;
	private List<ItemStack> mItemStacks;
	//Keeps track of all items in the item handler. (Used for when we have a scroll bar implemented);
	private List<ItemStack> mAllItemStacks;
	private String mSearchText = "";

	public ManifestItemHandler(UUID playerID)
	{
		mPlayerID = playerID;
		mItemStacks= new ArrayList();
		mAllItemStacks = new ArrayList();
	}
	
	//Returns the slot number of the item added.
	public void AddItemStack(ItemStack item)
	{
		mAllItemStacks.add(item);
		//TODO Rework for scroll bar
		mItemStacks.add(item);
	}
	
	@Override
	public int getSlots() {
		// TODO Auto-generated method stub
		return 81;
	}

	@Override
	public ItemStack getStackInSlot(int slot) 
	{
		if (mSearchText == "")
		{
			if (slot > mItemStacks.size() - 1)
				return ItemStack.EMPTY;
			
			return mItemStacks.get(slot);
		}
		else
		{
			List<ItemStack> s = filteredResults();
			
			if (slot > s.size() - 1)
				return ItemStack.EMPTY;
			
			return filteredResults().get(slot);
		}
			
	}
	
	public List<ItemStack> filteredResults()
	{
		
		List<ItemStack> temp = new ArrayList();
		
		for (ItemStack s : mItemStacks)
		{
			if (s.getDisplayName().toLowerCase().contains(mSearchText.toLowerCase()))
			{
				temp.add(s);
			}
		}
		
		return temp;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) 
	{
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) 
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) 
	{
		
	}

	public void setSearchText(String s)
	{
		mSearchText = s;
	}
}
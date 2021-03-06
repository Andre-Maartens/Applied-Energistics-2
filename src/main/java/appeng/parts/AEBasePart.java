/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.networking.PartCable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class AEBasePart implements IPart, IGridProxyable, IActionHost, IUpgradeableHost, ICustomNameObject
{

	protected ISimplifiedBundle renderCache = null;

	protected final AENetworkProxy proxy;
	protected TileEntity tile = null;
	protected IPartHost host = null;
	protected ForgeDirection side = null;

	protected final ItemStack is;

	public AEBasePart(Class c, ItemStack is) {
		this.is = is;
		this.proxy = new AENetworkProxy( this, "part", is, this instanceof PartCable );
		this.proxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderBlock( x, y, z, renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public ItemStack getItemStack(PartItemStack type)
	{
		if ( type == PartItemStack.Network )
		{
			ItemStack copy = this.is.copy();
			copy.setTagCompound( null );
			return copy;
		}
		return this.is;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		this.proxy.readFromNBT( data );
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		this.proxy.writeToNBT( data );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{

	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		return false;
	}

	@Override
	public IGridNode getGridNode()
	{
		return this.proxy.getNode();
	}

	@Override
	public void onEntityCollision(Entity entity)
	{

	}

	@Override
	public void removeFromWorld()
	{
		this.proxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		this.proxy.onReady();
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		this.side = side;
		this.tile = tile;
		this.host = host;
	}

	public IPartHost getHost()
	{
		return this.host;
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return null;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		return this.proxy.getNode();
	}

	protected AEColor getColor()
	{
		if ( this.getHost() == null )
			return AEColor.Transparent;
		return this.getHost().getColor();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.tile );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.GLASS;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public void gridChanged()
	{

	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return 0;
	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from source of settings
	 * @param compound compound of source
	 */
	public void uploadSettings(SettingsFrom from, NBTTagCompound compound)
	{
		if ( compound != null )
		{
			IConfigManager cm = this.getConfigManager();
			if ( cm != null )
				cm.readFromNBT( compound );
		}

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			pHost.setPriority( compound.getInteger( "priority" ) );
		}

		IInventory inv = this.getInventoryByName( "config" );
		if ( inv != null && inv instanceof AppEngInternalAEInventory )
		{
			AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
			AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSizeInventory() );
			tmp.readFromNBT( compound, "config" );
			for (int x = 0; x < tmp.getSizeInventory(); x++)
				target.setInventorySlotContents( x, tmp.getStackInSlot( x ) );
		}
	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 * @return compound of source
	 */
	public NBTTagCompound downloadSettings(SettingsFrom from)
	{
		NBTTagCompound output = new NBTTagCompound();

		IConfigManager cm = this.getConfigManager();
		if ( cm != null )
			cm.writeToNBT( output );

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			output.setInteger( "priority", pHost.getPriority() );
		}

		IInventory inv = this.getInventoryByName( "config" );
		if ( inv != null && inv instanceof AppEngInternalAEInventory )
		{
			((AppEngInternalAEInventory) inv).writeToNBT( output, "config" );
		}

		return output.hasNoTags() ? null : output;
	}

	public boolean useStandardMemoryCard()
	{
		return true;
	}

	private boolean useMemoryCard(EntityPlayer player)
	{
		ItemStack memCardIS = player.inventory.getCurrentItem();

		if ( memCardIS != null && this.useStandardMemoryCard() && memCardIS.getItem() instanceof IMemoryCard )
		{
			IMemoryCard memoryCard = (IMemoryCard) memCardIS.getItem();

			ItemStack is = this.getItemStack( PartItemStack.Network );

			// Blocks and parts share the same soul!
			if ( AEApi.instance().parts().partInterface.sameAsStack( is ) )
				is = AEApi.instance().blocks().blockInterface.stack( 1 );

			String name = is.getUnlocalizedName();

			if ( player.isSneaking() )
			{
				NBTTagCompound data = this.downloadSettings( SettingsFrom.MEMORY_CARD );
				if ( data != null )
				{
					memoryCard.setMemoryCardContents( memCardIS, name, data );
					memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
				}
			}
			else
			{
				String storedName = memoryCard.getSettingsName( memCardIS );
				NBTTagCompound data = memoryCard.getData( memCardIS );
				if ( name.equals( storedName ) )
				{
					this.uploadSettings( SettingsFrom.MEMORY_CARD, data );
					memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
				}
				else
					memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
			}
			return true;
		}
		return false;
	}

	@Override
	final public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		if ( this.useMemoryCard( player ) )
			return true;

		return this.onPartActivate( player, pos );
	}

	@Override
	final public boolean onShiftActivate(EntityPlayer player, Vec3 pos)
	{
		if ( this.useMemoryCard( player ) )
			return true;

		return this.onPartShiftActivate( player, pos );
	}

	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	public boolean onPartShiftActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		this.proxy.setOwner( player );
	}

	@Override
	public TileEntity getTile()
	{
		return this.tile;
	}

	@Override
	public void securityBreak()
	{
		if ( this.is.stackSize > 0 )
		{
			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.is.copy() );
			this.host.removePart( this.side, false );
			Platform.spawnDrops( this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, items );
			this.is.stackSize = 0;
		}
	}

	@Override
	public AENetworkProxy getProxy()
	{
		return this.proxy;
	}

	@Override
	public IGridNode getActionableNode()
	{
		return this.proxy.getNode();
	}

	@Override
	public boolean canBePlacedOn(BusSupport what)
	{
		return what == BusSupport.CABLE;
	}

	public void saveChanges()
	{
		this.host.markForSave();
	}

	@Override
	public boolean requireDynamicRender()
	{
		return false;
	}

	@Override
	public String getCustomName()
	{
		return this.is.getDisplayName();
	}

	@Override
	public boolean hasCustomName()
	{
		return this.is.hasDisplayName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBreakingTexture()
	{
		return null;
	}

	public void addEntityCrashInfo(CrashReportCategory crashreportcategory)
	{
		crashreportcategory.addCrashSection( "Part Side", this.side );
	}
}
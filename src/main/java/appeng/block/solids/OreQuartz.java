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

package appeng.block.solids;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzOre;
import appeng.core.features.AEFeature;

public class OreQuartz extends AEBaseBlock
{

	public int boostBrightnessLow;
	public int boostBrightnessHigh;
	public boolean enhanceBrightness;

	public OreQuartz(Class self) {
		super( self, Material.rock );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setHardness( 3.0F );
		this.setResistance( 5.0F );
		this.boostBrightnessLow = 0;
		this.boostBrightnessHigh = 1;
		this.enhanceBrightness = false;
	}

	@Override
	public void postInit()
	{
		OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( this ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzOre.class;
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess par1iBlockAccess, int par2, int par3, int par4)
	{
		int j1 = super.getMixedBrightnessForBlock( par1iBlockAccess, par2, par3, par4 );
		if ( this.enhanceBrightness )
		{
			j1 = Math.max( j1 >> 20, j1 >> 4 );

			if ( j1 > 4 )
				j1 += this.boostBrightnessHigh;
			else
				j1 += this.boostBrightnessLow;

			if ( j1 > 15 )
				j1 = 15;
			return j1 << 20 | j1 << 4;
		}
		return j1;
	}

	public OreQuartz() {
		this( OreQuartz.class );
	}

	ItemStack getItemDropped()
	{
		return AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 );
	}

	@Override
	public Item getItemDropped(int id, Random rand, int meta)
	{
		return this.getItemDropped().getItem();
	}

	@Override
	public int damageDropped(int id)
	{
		return this.getItemDropped().getItemDamage();
	}

	@Override
	public int quantityDropped(Random rand)
	{
		return 1 + rand.nextInt( 2 );
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand)
	{
		if ( fortune > 0 && Item.getItemFromBlock( this ) != this.getItemDropped( 0, rand, fortune ) )
		{
			int j = rand.nextInt( fortune + 2 ) - 1;

			if ( j < 0 )
			{
				j = 0;
			}

			return this.quantityDropped( rand ) * (j + 1);
		}
		else
		{
			return this.quantityDropped( rand );
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World w, int x, int y, int z, int blockID, float something, int meta)
	{
		super.dropBlockAsItemWithChance( w, x, y, z, blockID, something, meta );

		if ( this.getItemDropped( blockID, w.rand, meta ) != Item.getItemFromBlock( this ) )
		{
			int xp = MathHelper.getRandomIntegerInRange( w.rand, 2, 5 );

			this.dropXpOnBlockBreak( w, x, y, z, xp );
		}
	}

}

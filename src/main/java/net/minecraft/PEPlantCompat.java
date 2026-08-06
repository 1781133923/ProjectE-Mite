package net.minecraft;

/**
 * Same-package accessor for protected members of MITE's crop blocks.
 * Lets the ProjectE port read the seed/crop item ids without reflection.
 */
public final class PEPlantCompat
{
	public static int getCropSeedItem(BlockCrops crop)
	{
		return crop.getSeedItem();
	}

	public static int getCropItem(BlockCrops crop)
	{
		return crop.getCropItem();
	}

	public static int getCropMatureYield(BlockCrops crop)
	{
		return crop.getMatureYield();
	}

	private PEPlantCompat()
	{
	}
}

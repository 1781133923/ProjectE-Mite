package moze_intel.projecte.emc.mappers;

import moze_intel.projecte.emc.NormalizedSimpleStack;
import moze_intel.projecte.emc.arithmetics.FullFractionArithmetic;
import moze_intel.projecte.emc.collector.IExtendedMappingCollector;
import moze_intel.projecte.emc.collector.IMappingCollector;
import moze_intel.projecte.utils.PELogger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class FluidMapper implements IEMCMapper<NormalizedSimpleStack, Integer> {
	private static List<Pair<NormalizedSimpleStack, FluidStack>> melting = Lists.newArrayList();

	public static void addMelting(String odName, String fluidName, int amount) {
		addMelting(NormalizedSimpleStack.forOreDictionary(odName), fluidName, amount);
	}
	public static void addMelting(Item item, String fluidName, int amount) {
		addMelting(NormalizedSimpleStack.getFor(item), fluidName, amount);
	}
	public static void addMelting(Block block, String fluidName, int amount) {
		addMelting(NormalizedSimpleStack.getFor(block), fluidName, amount);
	}
	public static void addMelting(NormalizedSimpleStack stack, String fluidName, int amount) {
		Fluid fluid = FluidRegistry.getFluid(fluidName);
		if (fluid != null) {
			melting.add(Pair.of(stack, new FluidStack(fluid, amount)));
		} else {
			PELogger.logWarn("Can not get Fluid '%s'", fluidName);
		}
	}
	static {
		addMelting(Blocks.obsidian, "obisidan.molten", 288);
		addMelting(Blocks.glass, "glass.molten", 1000);
		addMelting(Blocks.glass_pane, "glass.molten", 250);
		addMelting(Items.ender_pearl, "ender", 250);

		addMelting("ingotIron", "iron.molten", 144);
		addMelting("ingotGold", "gold.molten", 144);
		addMelting("ingotCopper", "copper.molten", 144);
		addMelting("ingotTin", "tin.molten", 144);
		addMelting("ingotSilver", "silver.molten", 144);
		addMelting("ingotLead", "lead.molten", 144);
		addMelting("ingotNickel", "nickel.molten", 144);
		addMelting("ingotAluminum", "aluminum.molten", 144);
		addMelting("ingotArdite", "ardite.molten", 144);
		addMelting("ingotCobalt", "cobalt.molten", 144);
		addMelting("ingotPlatinum", "platinum.molten", 144);
		addMelting("ingotObsidian", "obsidian.molten", 144);
		addMelting("ingotElectrum", "electrum.molten", 144);
		addMelting("ingotInvar", "invar.molten", 144);
		addMelting("ingotSignalum", "signalum.molten", 144);
		addMelting("ingotLumium", "lumium.molten", 144);
		addMelting("ingotEnderium", "enderium.molten", 144);
		addMelting("ingotMithril", "mithril.molten", 144);

		addMelting("ingotBronze", "bronze.molten", 144);
		addMelting("ingotAluminumBrass", "aluminumbrass.molten", 144);
		addMelting("ingotManyullyn", "manyullyn.molten", 144);
		addMelting("ingotAlumite", "alumite.molten", 144);

		addMelting("gemEmerald", "emerald.liquid", 640);
		addMelting("dustRedstone", "redstone", 100);
		addMelting("dustGlowstone", "glowstone", 250);

		addMelting("dustCryotheum", "cryotheum", 100);
		addMelting("dustPryotheum", "pryotheum", 100);
	}

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Integer> mapper, Configuration config) {
		mapper.setValueBefore(NormalizedSimpleStack.getFor(FluidRegistry.WATER), Integer.MIN_VALUE/*=Free. TODO: Use IntArithmetic*/);
		//1 Bucket of Lava = 1 Block of Obsidian
		mapper.addConversion(1000, NormalizedSimpleStack.getFor(FluidRegistry.LAVA), Arrays.asList(NormalizedSimpleStack.getFor(Blocks.obsidian)));

		// Add Conversion in case MFR is not present and milk is not an actual fluid
		NormalizedSimpleStack fakeMilkFluid = NormalizedSimpleStack.createFake("fakeMilkFluid");
		mapper.setValueBefore(fakeMilkFluid, 16);
		mapper.addConversion(1, NormalizedSimpleStack.getFor(Items.milk_bucket), Arrays.asList(NormalizedSimpleStack.getFor(Items.bucket), fakeMilkFluid));

		Fluid milkFluid = FluidRegistry.getFluid("milk");
		if (milkFluid != null) {
			mapper.addConversion(1000, NormalizedSimpleStack.getFor(milkFluid), Arrays.asList(fakeMilkFluid));
		}

		// MITE registers no FluidContainerRegistry entries for its buckets, so
		// register "filled bucket = empty bucket" for the water/lava/milk
		// buckets. This is required so that recipes using them (e.g. the
		// Evertide/Volcanite amulets) can be inferred.
		// Stone buckets are excluded on purpose: MITE's "pour out" recipes
		// (empty = stoneBucket - empty) would form a cycle with this conversion
		// and the solver would zero every bucket. Stone buckets are handled by
		// the post-processing step in EMCMapper instead. Liquids stay without
		// EMC (water is free, lava/milk never enter the EMC map).
		for (Item item : Item.itemsList) {
			if (item == null || !(item instanceof net.minecraft.ItemVessel)) {
				continue;
			}
			net.minecraft.ItemVessel vessel = (net.minecraft.ItemVessel) item;
			if (vessel.isEmpty() || vessel.contains(net.minecraft.Material.stone)) {
				continue;
			}
			// Milk buckets use ItemBucketMilk (extends ItemVessel, not
			// ItemBucket); identify filled buckets by their container item
			// being an empty bucket. Bowls (container is a bowl) stay alone.
			net.minecraft.Item container = vessel.getContainerItem();
			if (container == null || !(container instanceof net.minecraft.ItemBucket)) {
				continue;
			}
			mapper.addConversion(1, NormalizedSimpleStack.getFor(vessel),
					ImmutableMap.of(NormalizedSimpleStack.getFor(container), 1));
		}

		if (!(mapper instanceof IExtendedMappingCollector)) throw new RuntimeException("Cannot add Extended Fluid Mappings to mapper!");
		IExtendedMappingCollector emapper = (IExtendedMappingCollector) mapper;
		FullFractionArithmetic fluidArithmetic = new FullFractionArithmetic();

		for (Pair<NormalizedSimpleStack, FluidStack> pair: melting) {
			emapper.addConversion(pair.getValue().amount, NormalizedSimpleStack.getFor(pair.getValue().getFluid()), Arrays.asList(pair.getKey()), fluidArithmetic);
		}

		for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
			Fluid fluid = data.fluid.getFluid();
			mapper.addConversion(1, NormalizedSimpleStack.getFor(data.filledContainer),
					ImmutableMap.of(NormalizedSimpleStack.getFor(data.emptyContainer), 1, NormalizedSimpleStack.getFor(fluid), data.fluid.amount)
			);
		}
	}

	@Override
	public String getName() {
		return "FluidMapper";
	}

	@Override
	public String getDescription() {
		return "Adds Conversions for fluid container items and fluids.";
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}

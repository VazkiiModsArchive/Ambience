package vazkii.ambience;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.HashMap;
import java.util.Map;

public class BiomeMapper {
	
	private static Map<String, Biome> biomeMap = null;
	
	public static void applyMappings() {
		biomeMap = new HashMap<String, Biome>();
		for(ResourceLocation biomeResource : Biome.REGISTRY.getKeys()) {
			Biome biome = Biome.REGISTRY.getObject(biomeResource);
			String biomeName = biome.getBiomeName();
			if (biomeResource.getResourceDomain() != "minecraft") {
				biomeName = biomeResource.getResourceDomain()+" "+biomeName;
			}
			biomeMap.put(biomeName, biome);
		}
	}

	public static Map<String, Biome> getBiomes() {
		Map<String, Biome> tmpBiomeMap = new HashMap<String, Biome>();
		for(ResourceLocation biomeResource : Biome.REGISTRY.getKeys()) {
			Biome biome = Biome.REGISTRY.getObject(biomeResource);
			String biomeName = biome.getBiomeName();
			if (biomeResource.getResourceDomain() != "minecraft") {
				biomeName = biomeResource.getResourceDomain()+" "+biomeName;
			}
			tmpBiomeMap.put(biomeName, biome);
		}
		return tmpBiomeMap;
	}
	
	public static Biome getBiome(String s) {
		if(biomeMap == null) {
			applyMappings();
		}
		return biomeMap.get(s);
	}
	
	public static Type getBiomeType(String s) {
		return BiomeDictionary.Type.getType(s);
	}
	
}

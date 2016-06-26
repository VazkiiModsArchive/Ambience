package vazkii.ambience;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeMapper {

	private static Map<String, Biome> biomeMap = null;
	private static Map<String, BiomeDictionary.Type> typeMap = null;
	
	public static void applyMappings() {
		biomeMap = new HashMap<String, Biome>();
		typeMap = new HashMap<String, BiomeDictionary.Type>();
		
		
		Biome.REGISTRY.forEach((Biome biome) -> {
			if(biome != null)
				biomeMap.put(biome.getBiomeName(), biome);
		});
		
		for(BiomeDictionary.Type t : BiomeDictionary.Type.class.getEnumConstants())
			typeMap.put(t.name(), t);
	}
	
	public static Biome getBiome(String s) {
		if(biomeMap == null)
			applyMappings();
		return biomeMap.get(s);
	}
	
	public static BiomeDictionary.Type getBiomeType(String s) {
		if(typeMap == null)
			applyMappings();
		return typeMap.get(s);
	}
	
}

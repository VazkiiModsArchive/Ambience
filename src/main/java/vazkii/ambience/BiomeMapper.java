package vazkii.ambience;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeMapper {

	private Map<String, BiomeGenBase> biomeMap = null;
	private Map<String, BiomeDictionary.Type> typeMap = null;
	
	public void applyMappings() {
		biomeMap = new HashMap<String, BiomeGenBase>();
		typeMap = new HashMap<String, BiomeDictionary.Type>();
		
		for(BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if(biome == null)
				continue;
			biomeMap.put(biome.biomeName, biome);
		}
		
		for(BiomeDictionary.Type t : BiomeDictionary.Type.class.getEnumConstants())
			typeMap.put(t.name(), t);
	}
	
	public BiomeGenBase getBiome(String s) {
		if(biomeMap == null)
			applyMappings();
		return biomeMap.get(s);
	}
	
	public BiomeDictionary.Type getBiomeType(String s) {
		if(typeMap == null)
			applyMappings();
		return typeMap.get(s);
	}
	
}

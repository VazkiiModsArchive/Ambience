package vazkii.ambience;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public final class SongPicker {

	public static final String EVENT_MAIN_MENU = "mainMenu";
	public static final String EVENT_BOSS = "boss";
	public static final String EVENT_NIGHT = "night";
	public static final String EVENT_UNDERGROUND = "underground";
	public static final String EVENT_DEEP_UNDEGROUND = "deepUnderground";
	public static final String EVENT_HIGH_UP = "highUp";
	public static final String EVENT_MINECART = "minecart";
	public static final String EVENT_BOAT = "boat";
	public static final String EVENT_HORSE = "horse";
	public static final String EVENT_PIG = "pig";
	public static final String EVENT_GENERIC = "generic";
	
	public static final Map<String, String> eventMap = new HashMap();
	public static final Map<BiomeGenBase, String> biomeMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String> primaryTagMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String> secondaryTagMap = new HashMap();
	
	public static void reset() {
		eventMap.clear();
		biomeMap.clear();
		primaryTagMap.clear();
		secondaryTagMap.clear();
	}
	
	public static String getSong() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player == null)
			return getSongForEvent(EVENT_MAIN_MENU);
		
		World world = mc.theWorld;
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);

        if(BossStatus.bossName != null && BossStatus.statusBarTime > 0)
        	return getSongForEvent(EVENT_BOSS);
        	
		Entity riding = player.ridingEntity;
		if(riding != null)
			if(riding instanceof EntityMinecart)
				return getSongForEvent(EVENT_MINECART);
			else if(riding instanceof EntityBoat)
				return getSongForEvent(EVENT_BOAT);
			else if(riding instanceof EntityHorse)
				return getSongForEvent(EVENT_HORSE);
			else if(riding instanceof EntityPig)
				return getSongForEvent(EVENT_PIG);
		
		if(y > 128)
			return getSongForEvent(EVENT_HIGH_UP);
		else if(!world.canBlockSeeTheSky(x, y, z))
			if(y < 20)
				return getSongForEvent(EVENT_DEEP_UNDEGROUND);
			else if(y < 50)
				return getSongForEvent(EVENT_UNDERGROUND);
		
		if(!world.provider.isDaytime())
			return getSongForEvent(EVENT_NIGHT);
		
        if(world != null && world.blockExists(x, y, z)) {
            Chunk chunk = world.getChunkFromBlockCoords(x, z);
            BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager());
            if(biomeMap.containsKey(biome))
            	return biomeMap.get(biome);
            
            BiomeDictionary.Type[] types = BiomeDictionary.getTypesForBiome(biome);
            for(Type t : types)
            	if(primaryTagMap.containsKey(t))
            		return primaryTagMap.get(t);
            for(Type t : types)
            	if(secondaryTagMap.containsKey(t))
            		return secondaryTagMap.get(t);
        }
        
        return getSongForEvent(EVENT_GENERIC);
	}
	
	public static String getSongForEvent(String event) {
		if(eventMap.containsKey(event))
			return eventMap.get(event);
		
		if(event.equals(EVENT_GENERIC))
			return null;
		
		return getSongForEvent(EVENT_GENERIC);
	}
	
	public static String getSongName(String song) {
		return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
	}
	
}

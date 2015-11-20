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

        if(BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
        	String song = getSongForEvent(EVENT_BOSS);
        	if(song != null)
        		return song;
        }
        	
		Entity riding = player.ridingEntity;
		if(riding != null) {
			if(riding instanceof EntityMinecart) {
	        	String song = getSongForEvent(EVENT_MINECART);
	        	if(song != null)
	        		return song;
	        } 
			if(riding instanceof EntityBoat) {
	        	String song = getSongForEvent(EVENT_BOAT);
	        	if(song != null)
	        		return song;
	        } 
			if(riding instanceof EntityHorse) {
	        	String song = getSongForEvent(EVENT_HORSE);
	        	if(song != null)
	        		return song;
	        } 
			if(riding instanceof EntityPig) {
	        	String song = getSongForEvent(EVENT_PIG);
	        	if(song != null)
	        		return song;
	        }
		}
		
		if(y > 128) {
        	String song = getSongForEvent(EVENT_HIGH_UP);
        	if(song != null)
        		return song;
        }
		
		if(!world.canBlockSeeTheSky(x, y, z)) {
			if(y < 20) {
	        	String song = getSongForEvent(EVENT_DEEP_UNDEGROUND);
	        	if(song != null)
	        		return song;
	        }
			if(y < 50) {
	        	String song = getSongForEvent(EVENT_UNDERGROUND);
	        	if(song != null)
	        		return song;
	        }
		}
		
		long time = world.getWorldTime() % 24000;
		if(time > 13600) {
        	String song = getSongForEvent(EVENT_NIGHT);
        	if(song != null)
        		return song;
        }
		
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
		
		return null;
	}
	
	public static void getSongForBiome(World world, int x, int y, int z) {
		
	}
	
	public static String getSongName(String song) {
		return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
	}
	
}

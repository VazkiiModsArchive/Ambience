package vazkii.ambience;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.ReferralException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.reflect.Reflection;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent.BossInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.common.MinecraftForge;

public final class SongPicker {

	public static final String EVENT_MAIN_MENU = "mainMenu";
	public static final String EVENT_BOSS = "boss";
	public static final String EVENT_IN_NETHER = "nether";
	public static final String EVENT_IN_END = "end";
	public static final String EVENT_HORDE = "horde";
	public static final String EVENT_NIGHT = "night";
	public static final String EVENT_RAIN = "rain";
	public static final String EVENT_UNDERWATER = "underwater";
	public static final String EVENT_UNDERGROUND = "underground";
	public static final String EVENT_DEEP_UNDEGROUND = "deepUnderground";
	public static final String EVENT_HIGH_UP = "highUp";
	public static final String EVENT_VILLAGE = "village";
	public static final String EVENT_MINECART = "minecart";
	public static final String EVENT_BOAT = "boat";
	public static final String EVENT_HORSE = "horse";
	public static final String EVENT_PIG = "pig";
	public static final String EVENT_FISHING = "fishing";
	public static final String EVENT_DYING = "dying";
	public static final String EVENT_PUMPKIN_HEAD = "pumpkinHead";
	public static final String EVENT_GENERIC = "generic";
	
	public static final Map<String, String[]> eventMap = new HashMap();
	public static final Map<Biome, String[]> biomeMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String[]> primaryTagMap = new HashMap();
	public static final Map<BiomeDictionary.Type, String[]> secondaryTagMap = new HashMap();
	
	public static final Random rand = new Random();
	
	public static void reset() {
		eventMap.clear();
		biomeMap.clear();
		primaryTagMap.clear();
		secondaryTagMap.clear();
	}
	
	public static String[] getSongs() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		World world = mc.theWorld;

		if(player == null || world == null)
			return getSongsForEvent(EVENT_MAIN_MENU);
		
		BlockPos pos = new BlockPos(player);

		AmbienceEventEvent event = new AmbienceEventEvent.Pre(world, pos);
		MinecraftForge.EVENT_BUS.post(event);
    	String[] eventr = getSongsForEvent(event.event);
    	if(eventr != null)
    		return eventr;
		
    	GuiBossOverlay bossOverlay = mc.ingameGUI.getBossOverlay();
    	Map map = ReflectionHelper.getPrivateValue(GuiBossOverlay.class, bossOverlay, Ambience.OBF_MAP_BOSS_INFOS);
        if(!map.isEmpty()) {
        	String[] songs = getSongsForEvent(EVENT_BOSS);
        	if(songs != null)
        		return songs;
        }
        
        float hp = player.getHealth();
        if(hp < 7) {
        	String[] songs = getSongsForEvent(EVENT_DYING);
        	if(songs != null)
        		return songs;
        }

	        int monsterCount = world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16)).size();
		if(monsterCount > 5) {
        	String[] songs = getSongsForEvent(EVENT_HORDE);
        	if(songs != null)
        		return songs;
		}
        
        if(player.fishEntity != null) {
        	String[] songs = getSongsForEvent(EVENT_FISHING);
        	if(songs != null)
        		return songs;
        }
        
        ItemStack headItem = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if(headItem != null && headItem.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
        	String[] songs = getSongsForEvent(EVENT_PUMPKIN_HEAD);
        	if(songs != null)
        		return songs;
        }
        	int indimension = world.provider.getDimension();

		if(indimension == -1) {
		String[] songs = getSongsForEvent(EVENT_IN_NETHER);
	        	if(songs != null)
	        	return songs;
		} else if(indimension == 1) {
			String[] songs = getSongsForEvent(EVENT_IN_END);
	        	if(songs != null)
	        	return songs;
		}

		Entity riding = player.getRidingEntity();
		if(riding != null) {
			if(riding instanceof EntityMinecart) {
	        	String[] songs = getSongsForEvent(EVENT_MINECART);
	        	if(songs != null)
	        		return songs;
	        } 
			if(riding instanceof EntityBoat) {
	        	String[] songs = getSongsForEvent(EVENT_BOAT);
	        	if(songs != null)
	        		return songs;
	        } 
			if(riding instanceof EntityHorse) {
	        	String[] songs = getSongsForEvent(EVENT_HORSE);
	        	if(songs != null)
	        		return songs;
	        } 
			if(riding instanceof EntityPig) {
	        	String[] songs = getSongsForEvent(EVENT_PIG);
	        	if(songs != null)
	        		return songs;
	        }
		}
		
		if(player.isInsideOfMaterial(Material.WATER)) {
        	String[] songs = getSongsForEvent(EVENT_UNDERWATER);
        	if(songs != null)
        		return songs;
		}
		
		boolean underground = !world.canSeeSky(pos);
		
		if(underground) {
			if(pos.getY() < 20) {
	        	String[] songs = getSongsForEvent(EVENT_DEEP_UNDEGROUND);
	        	if(songs != null)
	        		return songs;
	        }
			if(pos.getY() < 55) {
	        	String[] songs = getSongsForEvent(EVENT_UNDERGROUND);
	        	if(songs != null)
	        		return songs;
	        }
		} else if(world.isRaining()) {
        	String[] songs = getSongsForEvent(EVENT_RAIN);
        	if(songs != null)
        		return songs;
		}
		
		if(pos.getY() > 128) {
        	String[] songs = getSongsForEvent(EVENT_HIGH_UP);
        	if(songs != null)
        		return songs;
        }
		
		long time = world.getWorldTime() % 24000;
		if(time > 13300 && time < 23200) {
        	String[] songs = getSongsForEvent(EVENT_NIGHT);
        	if(songs != null)
        		return songs;
        }
		
		int villagerCount = world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(player.posX - 30, player.posY - 8, player.posZ - 30, player.posX + 30, player.posY + 8, player.posZ + 30)).size();
		if(villagerCount > 3) {
        	String[] songs = getSongsForEvent(EVENT_VILLAGE);
        	if(songs != null)
        		return songs;
		}


		
		event = new AmbienceEventEvent.Post(world, pos);
		MinecraftForge.EVENT_BUS.post(event);
    	eventr = getSongsForEvent(event.event);
    	if(eventr != null)
    		return eventr;
		
        if(world != null) {
            Chunk chunk = world.getChunkFromBlockCoords(pos);
            Biome biome = chunk.getBiome(pos, world.getBiomeProvider());
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
        
        return getSongsForEvent(EVENT_GENERIC);
	}
	
	public static String getSongsString() {
		return StringUtils.join(getSongs(), ",");
	}
	
	public static String getRandomSong() {
		String[] songChoices = getSongs();
		
		return songChoices[rand.nextInt(songChoices.length)];
	}
	
	public static String[] getSongsForEvent(String event) {
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

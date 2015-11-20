package vazkii.ambience;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Mod(modid = Ambience.MOD_ID, name = Ambience.MOD_NAME, version = Ambience.VERSION, dependencies = Ambience.DEPENDENCIES)
public class Ambience {

	public static final String MOD_ID = "Ambience";
	public static final String MOD_NAME = MOD_ID;
	public static final String BUILD = "GRADLE:BUILD";
	public static final String VERSION = "GRADLE:VERSION-" + BUILD;
	public static final String DEPENDENCIES = "";

	public static final String SONG_MAIN_MENU = "ScapeTheme";
	public static final String SONG_BOSS = "AttackII";
	public static final String SONG_UNDERGROUND = "ScapeCave";
	public static final String SONG_DEEP_UNDERGROUND = "CaveBackground";
	public static final String SONG_HIGH_UP = "MilesAway";
	public static final String SONG_MINECART = "DwarfTheme";
	public static final String SONG_BOAT = "SeaShantyII";
	public static final String SONG_HORSE = "FluteSalad";
	public static final String SONG_PIG = "GoblinVillage";
	public static final String SONG_GENERIC = "AutumnVoyage";
	
	private static final int WAIT_DURATION = 40;
	public static final int FADE_DURATION = 20;
	
	private static final Map<BiomeGenBase, String> biomeMap = new HashMap();
	private static final Map<BiomeDictionary.Type, String> primaryTagMap = new HashMap();
	private static final Map<BiomeDictionary.Type, String> secondaryTagMap = new HashMap();

	private static final List<String> instantSwitch = new ArrayList();
	
	static {
		biomeMap.put(getMutation(BiomeGenBase.plains), "Parade");
		biomeMap.put(getMutation(BiomeGenBase.forest), "Parade");
		biomeMap.put(BiomeGenBase.roofedForest, "Start");
		biomeMap.put(BiomeGenBase.deepOcean, "LongWayHome");

		primaryTagMap.put(Type.SPOOKY, "UnknownLand");
		primaryTagMap.put(Type.DEAD, "Spooky");
		primaryTagMap.put(Type.LUSH, "Expanse");
		primaryTagMap.put(Type.NETHER, "Moody");
		primaryTagMap.put(Type.END, "Wonder");
		primaryTagMap.put(Type.MUSHROOM, "Wander");
		primaryTagMap.put(Type.MAGICAL, "Nightfall");
		primaryTagMap.put(Type.OCEAN, "NewbieMelody");
		primaryTagMap.put(Type.RIVER, "Greatness");
		
		secondaryTagMap.put(Type.MESA, "Barbarianism");
		secondaryTagMap.put(Type.FOREST, "Dream");
		secondaryTagMap.put(Type.PLAINS, "Harmony");
		secondaryTagMap.put(Type.MOUNTAIN, "Adventure");
		secondaryTagMap.put(Type.HILLS, "Medieval");
		secondaryTagMap.put(Type.SWAMP, "Yesteryear");
		secondaryTagMap.put(Type.SANDY, "AlKharid");
		secondaryTagMap.put(Type.SNOWY, "Starlight");
		secondaryTagMap.put(Type.WASTELAND, "Forever");
		secondaryTagMap.put(Type.BEACH, "Attention");
		
		instantSwitch.add(SONG_BOSS);
		instantSwitch.add(SONG_MINECART);
		instantSwitch.add(SONG_BOAT);
		instantSwitch.add(SONG_HORSE);
		instantSwitch.add(SONG_PIG);
	}
	
	PlayerThread thread;
	
	String nextSong;
	int waitTick = 0;
	int fadeOutTicks = 0;
	int fadeInTicks = 0;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		thread = new PlayerThread();
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(event.phase == Phase.END) {
			String song  = getSong();
			if((song == null && PlayerThread.currentSong != null) || (song != null && !song.equals(PlayerThread.currentSong))) {
				if(nextSong != null && nextSong.equals(song))
					waitTick--;
				else waitTick = switchInstantly(song) || switchInstantly(PlayerThread.currentSong) ? 0 : 40;
				nextSong = song;
				
				if(waitTick <= 0) {
					if(fadeOutTicks < FADE_DURATION) {
						thread.setGain(PlayerThread.fadeGains[fadeOutTicks]);
						fadeOutTicks++;
					} else {
						nextSong = null;
						changeSongTo(song);	
						fadeInTicks = 0;
					}
				}
					
			} else {
				nextSong = null;
				if(fadeInTicks < FADE_DURATION) {
					thread.setGain(PlayerThread.fadeGains[FADE_DURATION - 1 - fadeInTicks]);
					fadeInTicks++;
					fadeOutTicks = 0;
				}
			}
			
			Minecraft mc = Minecraft.getMinecraft();
			MusicTicker ticker = ReflectionHelper.getPrivateValue(Minecraft.class, mc, new String[] { "mcMusicTicker", "field_147126_aw", "ax" });
			if(ticker != null) {
				ISound sound = ReflectionHelper.getPrivateValue(MusicTicker.class, ticker, new String[] { "currentMusic", "field_147678_c", "c" });
				if(sound != null)
					mc.getSoundHandler().stopSound(sound);
			}
			if(thread != null)
				thread.setRealGain();
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
		if(!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			return;
		
		event.right.add(null);
		if(PlayerThread.currentSong != null) {
			String name = "Now Playing: " + getSongName(PlayerThread.currentSong);
			event.right.add(name);
		}
		if(nextSong != null) {
			String name = "Next Song: " + getSongName(nextSong);
			event.right.add(name);
		}
	}
	
	public void changeSongTo(String song) {
		thread.play(song);
	}
	
	public String getSong() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player == null)
			return SONG_MAIN_MENU;
		
		World world = mc.theWorld;
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.posY);
		int z = MathHelper.floor_double(player.posZ);

        if(BossStatus.bossName != null && BossStatus.statusBarTime > 0)
        	return SONG_BOSS;
        	
		Entity riding = player.ridingEntity;
		if(riding != null)
			if(riding instanceof EntityMinecart)
				return SONG_MINECART;
			else if(riding instanceof EntityBoat)
				return SONG_BOAT;
			else if(riding instanceof EntityHorse)
				return SONG_HORSE;
			else if(riding instanceof EntityPig)
				return SONG_PIG;
		
		if(y > 128)
			return SONG_HIGH_UP;
		else if(!world.canBlockSeeTheSky(x, y, z))
			if(y < 20)
				return SONG_DEEP_UNDERGROUND;
			else if(y < 50)
				return SONG_UNDERGROUND;
		
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
        
        return SONG_GENERIC;
	}
	
	public boolean switchInstantly(String song) {
		return song == null || instantSwitch.contains(song); 
	}
	
	public String getSongName(String song) {
		return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
	}
	
	public static BiomeGenBase getMutation(BiomeGenBase biome) {
		return BiomeGenBase.getBiome(biome.biomeID + 128);
	}
	
}

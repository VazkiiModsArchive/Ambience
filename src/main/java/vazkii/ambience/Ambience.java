package vazkii.ambience;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlayBackgroundMusicEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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

	private static final int WAIT_DURATION = 40;
	public static final int FADE_DURATION = 40;
	public static final int SILENCE_DURATION = 20;

	public static PlayerThread thread;
	
	String nextSong;
	int waitTick = WAIT_DURATION;
	int fadeOutTicks = FADE_DURATION;
	int fadeInTicks = 0;
	int silenceTicks = 0;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		File configDir = event.getSuggestedConfigurationFile().getParentFile();
		File ambienceDir = new File(configDir.getParentFile(), "ambience_music");
		if(!ambienceDir.exists())
			ambienceDir.mkdir();
		
		SongLoader.loadFrom(ambienceDir);
		
		if(SongLoader.enabled)
			thread = new PlayerThread();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		MusicTicker ticker = new NilMusicTicker(mc);
		ReflectionHelper.setPrivateValue(Minecraft.class, mc, ticker, "mcMusicTicker", "field_147126_aw", "ax");
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(thread == null)
			return;
		
		if(event.phase == Phase.END) {
			String song = SongPicker.getSong();
			if((song == null && PlayerThread.currentSong != null) || (song != null && !song.equals(PlayerThread.currentSong))) {
				if(nextSong != null && nextSong.equals(song))
					waitTick--;
				nextSong = song;
				
				if(waitTick <= 0) {
					if(fadeOutTicks < FADE_DURATION) {
						thread.setGain(PlayerThread.fadeGains[fadeOutTicks]);
						fadeOutTicks++;
						silenceTicks = 0;
					} else {
						nextSong = null;
						if(silenceTicks < SILENCE_DURATION) {
							silenceTicks++;
						} else {
							changeSongTo(song);
							fadeOutTicks = 0;
							waitTick = WAIT_DURATION;
						}
					}
				}
			} else {
				nextSong = null;
				thread.setGain(PlayerThread.fadeGains[0]);
				silenceTicks = 0;
				fadeOutTicks = 0;
				waitTick = WAIT_DURATION;
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
			String name = "Now Playing: " + SongPicker.getSongName(PlayerThread.currentSong);
			event.right.add(name);
		}
		if(nextSong != null) {
			String name = "Next Song: " + SongPicker.getSongName(nextSong);
			event.right.add(name);
		}
	}
	
	@SubscribeEvent
	public void onBackgroundMusic(PlayBackgroundMusicEvent event) {
		if(SongLoader.enabled)
			event.setCanceled(true);
	}
	
	public void changeSongTo(String song) {
		thread.play(song);
	}
	
	public static BiomeGenBase getMutation(BiomeGenBase biome) {
		return BiomeGenBase.getBiome(biome.biomeID + 128);
	}
	
}

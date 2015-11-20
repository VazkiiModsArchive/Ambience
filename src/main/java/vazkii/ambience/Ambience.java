package vazkii.ambience;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
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
	public static final int FADE_DURATION = 20;

	public static PlayerThread thread;
	
	String nextSong;
	int waitTick = 0;
	int fadeOutTicks = 0;
	int fadeInTicks = 0;
	
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
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(thread == null)
			return;
		
		if(event.phase == Phase.END) {
			String song = SongPicker.getSong();
			if((song == null && PlayerThread.currentSong != null) || (song != null && !song.equals(PlayerThread.currentSong))) {
				if(nextSong != null && nextSong.equals(song))
					waitTick--;
				else waitTick = WAIT_DURATION;
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
			String name = "Now Playing: " + SongPicker.getSongName(PlayerThread.currentSong);
			event.right.add(name);
		}
		if(nextSong != null) {
			String name = "Next Song: " + SongPicker.getSongName(nextSong);
			event.right.add(name);
		}
	}
	
	public void changeSongTo(String song) {
		thread.play(song);
	}
	
	public static BiomeGenBase getMutation(BiomeGenBase biome) {
		return BiomeGenBase.getBiome(biome.biomeID + 128);
	}
	
}

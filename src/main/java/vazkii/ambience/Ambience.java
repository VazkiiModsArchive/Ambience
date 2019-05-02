package vazkii.ambience;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.File;
import java.util.logging.LogManager;

@Mod(modid = Ambience.MOD_ID, name = Ambience.MOD_NAME, version = Ambience.VERSION, dependencies = Ambience.DEPENDENCIES)
public class Ambience {

	public static final String MOD_ID = "ambience";
	public static final String MOD_NAME = MOD_ID;
	public static final String BUILD = "GRADLE:BUILD";
	public static final String VERSION = "GRADLE:VERSION-" + BUILD;
	public static final String DEPENDENCIES = "";

	private static final int WAIT_DURATION = 40;
	public static final int FADE_DURATION = 40;
	public static final int SILENCE_DURATION = 20;

	public static final String[] OBF_MC_MUSIC_TICKER = { "aM", "field_147126_aw", "mcMusicTicker" };
	public static final String[] OBF_MAP_BOSS_INFOS = { "g", "field_184060_g", "mapBossInfos" };

	public static PlayerThread thread;
	
	String currentSong;
	String nextSong;
	int waitTick = WAIT_DURATION;
	int fadeOutTicks = FADE_DURATION;
	int fadeInTicks = 0;
	int silenceTicks = 0;
	File ambienceDir;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		File configDir = event.getSuggestedConfigurationFile().getParentFile();
		ambienceDir = new File(configDir.getParentFile(), "ambience_music");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

		SongPicker.resetBiomes();
		BiomeMapper.applyMappings();

		if(!ambienceDir.exists())
			ambienceDir.mkdir();

		SongLoader.loadFrom(ambienceDir);

		if (SongLoader.debug) LogManager.getLogManager().getLogger(Ambience.MOD_ID).info("Debug log messages enabled. See below.");
		if (SongLoader.debug) LogManager.getLogManager().getLogger(Ambience.MOD_ID).info("[DEBUG] BIOME LIST: "+BiomeMapper.getBiomes());
		if (SongLoader.debug) LogManager.getLogManager().getLogger(Ambience.MOD_ID).info("[DEBUG] USED EVENTS LIST: "+SongPicker.eventMap);

		if(SongLoader.enabled)
			thread = new PlayerThread();

		Minecraft mc = Minecraft.getMinecraft();
		MusicTicker ticker = new NilMusicTicker(mc);
		ReflectionHelper.setPrivateValue(Minecraft.class, mc, ticker, OBF_MC_MUSIC_TICKER);
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(thread == null)
			return;
		if(event.phase == Phase.END) {
			String songs = SongPicker.getSongsString();
			String song = null;
			
			if(songs != null) {
				if(nextSong == null || !songs.contains(nextSong)) {
					do {
						song = SongPicker.getRandomSong();
					} while(song.equals(currentSong) && songs.contains(","));
				} else
					song = nextSong;
			}
			
			if(songs != null && (!songs.equals(PlayerThread.currentSongChoices) || (song == null && PlayerThread.currentSong != null) || !thread.playing)) {
				if(nextSong != null && nextSong.equals(song))
					waitTick--;
				
				if (!song.equals(currentSong)) {
					if (currentSong != null && PlayerThread.currentSong != null && !PlayerThread.currentSong.equals(song) && songs.equals(PlayerThread.currentSongChoices))
						currentSong = PlayerThread.currentSong;
					else
						nextSong = song;
				} else if (nextSong != null && !songs.contains(nextSong))
					nextSong = null;
				
				if(waitTick <= 0) {
					if(PlayerThread.currentSong == null) {
						currentSong = nextSong;
						nextSong = null;
						PlayerThread.currentSongChoices = songs;
						changeSongTo(song);
						fadeOutTicks = 0;
						waitTick = WAIT_DURATION;
					} else if(fadeOutTicks < FADE_DURATION) {
						thread.setGain(PlayerThread.fadeGains[fadeOutTicks]);
						fadeOutTicks++;
						silenceTicks = 0;
					} else {
						if(silenceTicks < SILENCE_DURATION) {
							silenceTicks++;
						} else {
							nextSong = null;
							PlayerThread.currentSongChoices = songs;
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
		
		event.getRight().add(null);
		if(PlayerThread.currentSong != null) {
			String name = "Now Playing: " + SongPicker.getSongName(PlayerThread.currentSong);
			event.getRight().add(name);
		}
		if(nextSong != null) {
			String name = "Next Song: " + SongPicker.getSongName(nextSong);
			event.getRight().add(name);
		}
	}
	
	@SubscribeEvent
	public void onBackgroundMusic(PlaySoundEvent event) {
		if(SongLoader.enabled && event.getSound().getCategory() == SoundCategory.MUSIC) {
			if(event.isCancelable())
				event.setCanceled(true);
			
			event.setResultSound(null);
		}
	}
	
	public void changeSongTo(String song) {
		currentSong = song;
		thread.play(song);
	}
	
}

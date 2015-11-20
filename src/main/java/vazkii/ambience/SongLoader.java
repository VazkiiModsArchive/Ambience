package vazkii.ambience;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

public final class SongLoader {

	public static File mainDir;
	public static boolean enabled = false;
	
	public static void loadFrom(File f) {
		File config = new File(f, "ambience.properties");
		if(!config.exists())
			initConfig(config); 
		
		Properties props = new Properties();
		try {
			props.load(new FileReader(config));
			enabled = props.getProperty("enabled").equals("true");
			
			if(enabled) {
				SongPicker.reset();
				Set<Object> keys = props.keySet();
				for(Object obj : keys) {
					String s = (String) obj;
					
					String[] tokens = s.split(".");
					if(tokens.length >= 2) {
						
					}
					String keyType = s.
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		File musicDir = new File(f, "music");
		if(!musicDir.exists())
			musicDir.mkdir();
			
		mainDir = musicDir;
	}
	
	public static void initConfig(File f) {
		try {
			f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("# Ambience Config\n");
			writer.write("enabled=false");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static InputStream getStream() {
		File f = new File(mainDir, PlayerThread.currentSong + ".mp3");
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			FMLLog.log(Level.ERROR, "File " + f + " not found. Fix your Ambience config!");
			e.printStackTrace();
			return null;
		}
	}
}

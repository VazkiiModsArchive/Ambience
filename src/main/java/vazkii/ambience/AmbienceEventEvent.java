package vazkii.ambience;

import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event;

// top lel name
// works as an api, feel free to include in your mods to add custom events
public class AmbienceEventEvent extends Event {
	
	// Set this string to something as the answer
	public String event = "";
	
	public World world;
	public int x, y, z;
	
	AmbienceEventEvent(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public static class Pre extends AmbienceEventEvent {

		public Pre(World world, int x, int y, int z) {
			super(world, x, y, z);
		} 
	}
	
	
	public static class Post extends AmbienceEventEvent {

		public Post(World world, int x, int y, int z) {
			super(world, x, y, z);
		}
		
	}
}

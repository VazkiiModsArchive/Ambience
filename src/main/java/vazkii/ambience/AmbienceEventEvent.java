package vazkii.ambience;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

// top lel name
// works as an api, feel free to include in your mods to add custom events
public class AmbienceEventEvent extends Event {
	
	// Set this string to something as the answer
	public String event = "";
	
	public World world;
	public BlockPos pos;
	
	AmbienceEventEvent(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}
	
	public static class Pre extends AmbienceEventEvent {

		public Pre(World world, BlockPos pos) {
			super(world, pos);
		} 
	}
	
	
	public static class Post extends AmbienceEventEvent {

		public Post(World world, BlockPos pos) {
			super(world, pos);
		}
		
	}
}

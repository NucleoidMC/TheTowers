package com.hugman.the_towers.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import xyz.nucleoid.plasmid.game.GameSpace;

public class TheTowersSpawner {
	private final GameSpace gameSpace;

	public TheTowersSpawner(GameSpace gameSpace) {
		this.gameSpace = gameSpace;
	}

	public void spawnPlayerAtCenter(ServerPlayerEntity player) {
		this.spawnPlayerAt(player, this.getSurfaceBlock(0, 0));
	}

	public void spawnPlayerAt(ServerPlayerEntity player, BlockPos pos) {
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.gameSpace.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		player.teleport(this.gameSpace.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}

	public BlockPos getSurfaceBlock(int x, int z) {
		WorldChunk chunk = this.gameSpace.getWorld().getWorldChunk(new BlockPos(x, 0, z));

		return new BlockPos(x, chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z) + 1, z);
	}
}

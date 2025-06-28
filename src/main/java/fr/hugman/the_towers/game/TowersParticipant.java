package fr.hugman.the_towers.game;

public class TowersParticipant {
    // ticks until the participant respawns
    // If this is >0, the participant should be considered dead by the game
    // if it is < 0, the participant is alive
    // if it is == 0, the participant is alive and should respawn immediately
    public long ticksUntilRespawn;

    TowersParticipant() {}

    public boolean isDead() {
        return ticksUntilRespawn > 0;
    }

    public boolean isAlive() {
        return ticksUntilRespawn < 0;
    }
}

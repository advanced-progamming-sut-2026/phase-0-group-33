package models.game;

import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;

public class Vase {

    public enum VaseKind {
        ORDINARY,
        PLANT,
        GHOUL
    }

    private final int x;
    private final int y;
    private final VaseKind kind;
    private final ZombieType zombie;
    private final PlantType packet;

    public Vase(int x, int y, VaseKind kind, ZombieType zombie, PlantType packet) {
        this.x = x;
        this.y = y;
        this.kind = kind;
        this.zombie = zombie;
        this.packet = packet;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public VaseKind getKind() {
        return kind;
    }

    public ZombieType getZombie() {
        return zombie;
    }

    public PlantType getPacket() {
        return packet;
    }
}

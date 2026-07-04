package models.game;


public class Sun {

    public enum SunKind {
        NORMAL("normal", 25),
        SPECIAL("special", 100),
        RADIOACTIVE("radioactive", 25);

        private final String label;
        private final int value;

        SunKind(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public int getValue() {
            return value;
        }
    }

    private final SunKind kind;
    private final int x;
    private final int y;
    private int ticksToLand;
    private final int value;
    private final boolean producedByPlant;

    private Sun(SunKind kind, int x, int y, int ticksToLand, int value, boolean producedByPlant) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.ticksToLand = ticksToLand;
        this.value = value;
        this.producedByPlant = producedByPlant;
    }

    /** A sun dropping from the sky; lands after 5 seconds (50 ticks). */
    public static Sun falling(SunKind kind, int x, int y) {
        return new Sun(kind, x, y, 50, kind.getValue(), false);
    }

    /** A sun produced by a sun-producer plant, waiting on its tile. */
    public static Sun produced(int x, int y, int value) {
        return new Sun(SunKind.NORMAL, x, y, 0, value, true);
    }

    public boolean isFalling() {
        return ticksToLand > 0;
    }

    /**
     * Advances the fall by one tick; returns true the moment it touches the ground.
     */
    public boolean tickFall() {
        if (ticksToLand > 0) {
            ticksToLand--;
            return ticksToLand == 0;
        }
        return false;
    }

    public SunKind getKind() {
        return kind;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public boolean isProducedByPlant() {
        return producedByPlant;
    }
}

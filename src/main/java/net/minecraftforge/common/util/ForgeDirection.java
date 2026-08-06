package net.minecraftforge.common.util;

public enum ForgeDirection {
    UNKNOWN(-1, 0, 0, 0),
    DOWN(0, 0, -1, 0),
    UP(1, 0, 1, 0),
    NORTH(2, 0, 0, -1),
    SOUTH(3, 0, 0, 1),
    WEST(4, -1, 0, 0),
    EAST(5, 1, 0, 0);

    public static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public final int ordinalValue;

    ForgeDirection(int ordinalValue, int offsetX, int offsetY, int offsetZ) {
        this.ordinalValue = ordinalValue;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.flag = 1 << ordinalValue;
    }

    public static ForgeDirection getOrientation(int id) {
        // Vanilla Forge convention: DOWN=0, UP=1, NORTH=2, SOUTH=3, WEST=4,
        // EAST=5. The VALUES array must be indexed directly by that id -
        // putting UNKNOWN first shifted every direction by one (0 -> UNKNOWN,
        // 1 -> DOWN, ...), so AOE digging facing up resolved to an empty box
        // and hammer right-click could never break blocks above the player.
        if (id >= 0 && id <= 5) {
            return VALUES[id];
        }
        return UNKNOWN;
    }

    private static final ForgeDirection[] VALUES = {DOWN, UP, NORTH, SOUTH, WEST, EAST, UNKNOWN};

    public ForgeDirection getOpposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
            default -> UNKNOWN;
        };
    }

    public ForgeDirection getRotation(ForgeDirection axis) {
        if (axis == UP || axis == DOWN) {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
                default -> UNKNOWN;
            };
        }
        if (axis == NORTH || axis == SOUTH) {
            return switch (this) {
                case UP -> EAST;
                case EAST -> DOWN;
                case DOWN -> WEST;
                case WEST -> UP;
                default -> UNKNOWN;
            };
        }
        if (axis == EAST || axis == WEST) {
            return switch (this) {
                case UP -> NORTH;
                case NORTH -> DOWN;
                case DOWN -> SOUTH;
                case SOUTH -> UP;
                default -> UNKNOWN;
            };
        }
        return UNKNOWN;
    }

    public static ForgeDirection getFront(int index) {
        return VALID_DIRECTIONS[index % VALID_DIRECTIONS.length];
    }
}

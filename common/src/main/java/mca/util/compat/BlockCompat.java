package mca.util.compat;

public interface BlockCompat {
    /**
     * Sends a neighbor update event to surrounding blocks.
     *
     * @since MC 1.17
     */
    int NOTIFY_NEIGHBORS = 1;
    /**
     * Notifies listeners and clients who need to react when the block changes.
     *
     * @since MC 1.17
     */
    int NOTIFY_LISTENERS = 2;
    /**
     * Used in conjunction with {@link #NOTIFY_LISTENERS} to suppress the render pass on clients.
     *
     * @since MC 1.17
     */
    int NO_REDRAW = 4;
    /**
     * Forces a synchronous redraw on clients.
     *
     * @since MC 1.17
     */
    int REDRAW_ON_MAIN_THREAD = 8;
    /**
     * Bypass virtual block state changes and forces the passed state to be stored as-is.
     *
     * @since MC 1.17
     */
    int FORCE_STATE = 16;
    /**
     * Prevents the previous block (container) from dropping items when destroyed.
     *
     * @since MC 1.17
     */
    int SKIP_DROPS = 32;
    /**
     * Signals that the current block is being moved to a different location, usually because of a piston.
     *
     * @since MC 1.17
     */
    int MOVED = 64;
    /**
     * Signals that lighting updates should be skipped.
     *
     * @since MC 1.17
     */
    int SKIP_LIGHTING_UPDATES = 128;
    /**
     * The default setBlockState behavior. Same as {@code NOTIFY_NEIGHBORS | NOTIFY_LISTENERS}.
     *
     * @since MC 1.17
     */
    int NOTIFY_ALL = 3;
}

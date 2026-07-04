package models.game;

/** Lifecycle of a game session: plant selection, battle, then a terminal state. */
public enum GamePhase {
    PREPARATION,
    BATTLE,
    WON,
    LOST
}

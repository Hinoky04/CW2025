package com.comp2042;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    /**
     * Triggered when the player requests a hold action.
     * Implementations can use this to hold or swap the active brick.
     */
    ViewData onHoldEvent(MoveEvent event);

    void createNewGame();
}

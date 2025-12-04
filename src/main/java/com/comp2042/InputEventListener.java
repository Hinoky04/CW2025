package com.comp2042;

public interface InputEventListener {

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    ViewData onHoldEvent(MoveEvent event);

    DownData onDownEvent(MoveEvent event);

    // NEW: Space key – hard drop all the way down
    DownData onHardDropEvent(MoveEvent event);
}

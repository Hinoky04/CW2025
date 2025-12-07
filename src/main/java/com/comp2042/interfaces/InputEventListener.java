package com.comp2042.interfaces;

import com.comp2042.models.DownData;
import com.comp2042.models.MoveEvent;
import com.comp2042.models.ViewData;

public interface InputEventListener {

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    ViewData onHoldEvent(MoveEvent event);

    DownData onDownEvent(MoveEvent event);

    // NEW: Space key – hard drop all the way down
    DownData onHardDropEvent(MoveEvent event);
}

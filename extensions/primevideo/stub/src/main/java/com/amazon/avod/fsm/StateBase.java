package com.amazon.avod.fsm;

public abstract class StateBase<S, T> {
    // This method orginally has protected access (modified in patch code).
    public void doTrigger(Trigger<T> trigger) {
    }
}
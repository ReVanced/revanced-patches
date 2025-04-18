package com.amazon.avod.fsm;

public abstract class StateBase<S, T> {
    // originally protected access
    public void doTrigger(Trigger<T> trigger) {
    }
}
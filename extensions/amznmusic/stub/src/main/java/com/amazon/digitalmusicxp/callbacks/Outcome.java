package com.amazon.digitalmusicxp.callbacks;

@SuppressWarnings("unused")
public interface Outcome<T> {

    public static final class Success<T> implements Outcome<T> {
        private final T value;

        public Success(T value) {
            this.value = value;
        }

        public T getValue() { return value; }
    }

    public static final class Failure<T> implements Outcome<T> {
        private final Throwable cause;

        public Failure(Throwable cause) {
            this.cause = cause;
        }

        public Throwable getCause() { return cause; }
    }
}
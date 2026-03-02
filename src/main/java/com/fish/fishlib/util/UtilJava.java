package com.fish.fishlib.util;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import java.util.function.Consumer;

public class UtilJava {
    public static <T> Function1<T, Unit> consumerKotlin(Consumer<T> action) {
        return t -> {
            action.accept(t);
            return Unit.INSTANCE;
        };
    }

    public static Function0<Unit> runnableKotlin(Runnable action) {
        return () -> {
            action.run();
            return Unit.INSTANCE;
        };
    }
}

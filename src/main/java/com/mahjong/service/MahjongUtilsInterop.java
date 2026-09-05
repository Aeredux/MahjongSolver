package com.mahjong.service;

import mahjongutils.shanten.FuroChanceShantenKt;
import mahjongutils.shanten.FuroChanceShantenResult;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Java bridge for Kotlin value-class overloads that are name-mangled in bytecode.
 */
final class MahjongUtilsInterop {

    private static final Method FURO_CHANCE;

    static {
        try {
            FURO_CHANCE = FuroChanceShantenKt.class.getDeclaredMethod(
                    "furoChanceShanten-oR4HYDs",
                    List.class,
                    int.class,
                    boolean.class,
                    boolean.class,
                    boolean.class
            );
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private MahjongUtilsInterop() {
    }

    static FuroChanceShantenResult furoChanceShanten(
            List<mahjongutils.models.Tile> tiles,
            mahjongutils.models.Tile chanceTile,
            boolean allowChi
    ) {
        try {
            return (FuroChanceShantenResult) FURO_CHANCE.invoke(
                    null, tiles, chanceTile.getCode(), allowChi, false, false);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("furoChanceShanten interop failed", e);
        }
    }
}

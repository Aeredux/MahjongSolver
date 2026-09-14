package com.mahjong.service;

import mahjongutils.models.Furo;
import mahjongutils.shanten.FuroChanceShantenKt;
import mahjongutils.shanten.FuroChanceShantenResult;
import mahjongutils.shanten.ShantenKt;
import mahjongutils.shanten.UnionShantenResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Java bridge for Kotlin value-class overloads that are name-mangled in bytecode.
 */
final class MahjongUtilsInterop {

    private static final Method FURO_CHANCE;
    private static final Method FURO_PARSE;
    private static final Method FURO_BOX;

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
            FURO_PARSE = Furo.Companion.getClass().getMethod(
                    "parse-VPTBgb8",
                    List.class,
                    boolean.class
            );
            FURO_BOX = Furo.class.getDeclaredMethod("box-impl", int.class);
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

    static Furo parseFuro(List<mahjongutils.models.Tile> tiles, boolean ankan) {
        try {
            int value = (int) FURO_PARSE.invoke(Furo.Companion, tiles, ankan);
            return (Furo) FURO_BOX.invoke(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Furo.parse interop failed", e);
        }
    }

    static UnionShantenResult shanten(
            List<mahjongutils.models.Tile> tiles,
            List<Furo> furo
    ) {
        if (furo == null || furo.isEmpty()) {
            return ShantenKt.shanten(tiles);
        }
        return ShantenKt.shanten(tiles, furo);
    }

    static List<Furo> toFuroList(List<com.mahjong.dto.MeldDTO> melds) {
        if (melds == null || melds.isEmpty()) {
            return List.of();
        }
        List<Furo> result = new ArrayList<>();
        for (com.mahjong.dto.MeldDTO meld : melds) {
            Furo furo = MahjongUtilsTiles.toFuro(meld);
            if (furo != null) {
                result.add(furo);
            }
        }
        return result;
    }
}

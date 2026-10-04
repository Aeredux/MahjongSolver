package com.mahjong.service;

import com.mahjong.dto.MeldDTO;
import com.mahjong.dto.PlayerDiscardsDTO;
import com.mahjong.model.MeldType;
import com.mahjong.model.MoveSuggestion;
import com.mahjong.model.Tile;
import com.mahjong.model.TileType;
import com.mahjong.model.Wind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MoveSuggestionServiceTest {

    @Autowired
    private MoveSuggestionService suggestionService;

    @Test
    void testSuggestMovesForTenpaiHand() {
        List<Tile> tenpaiHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(tenpaiHand);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        MoveSuggestion bestMove = suggestions.get(0);
        assertEquals(0, bestMove.getShantenAfterDiscard());
    }

    @Test
    void testSuggestMovesForOneShantenHand() {
        List<Tile> oneShantenHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.EAST)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(oneShantenHand);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        MoveSuggestion bestMove = suggestions.get(0);
        assertTrue(bestMove.getShantenAfterDiscard() <= 1);
    }

    @Test
    void testGetBestMove() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        MoveSuggestion bestMove = suggestionService.getBestMove(hand);

        assertNotNull(bestMove);
        assertNotNull(bestMove.getDiscardTile());
        assertEquals(0, bestMove.getShantenAfterDiscard());
    }

    @Test
    void testGetTopMoves() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> topMoves = suggestionService.getTopMoves(hand, 3);

        assertNotNull(topMoves);
        assertTrue(topMoves.size() <= 3);
    }

    @Test
    void testSuggestMovesForEmptyHand() {
        List<Tile> emptyHand = Arrays.asList();

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(emptyHand);

        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testMoveSuggestionHasReasoning() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (MoveSuggestion suggestion : suggestions) {
            assertNotNull(suggestion.getReasoning());
            assertFalse(suggestion.getReasoning().isEmpty());
        }
    }

    @Test
    void testMoveSuggestionHasConfidence() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8), new Tile(TileType.M9),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (MoveSuggestion suggestion : suggestions) {
            assertTrue(suggestion.getConfidence() >= 0.0);
            assertTrue(suggestion.getConfidence() <= 1.0);
        }
    }

    @Test
    void testExplainMove() {
        // Hand with 4 complete melds + 2 isolated tiles (14 tiles)
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> allSuggestions = suggestionService.suggestMoves(hand);
        MoveSuggestion bestMove = suggestionService.getBestMove(hand);
        String explanation = suggestionService.explainMove(bestMove);

        System.out.println("\n=== Test Hand Analysis ===");
        System.out.println("Hand: M1 M2 M3 M4 M5 M6 P1 P1 P1 S5 S6 S7 EAST WEST");
        System.out.println("\nAll move suggestions:");
        for (MoveSuggestion suggestion : allSuggestions) {
            System.out.println(suggestionService.explainMove(suggestion));
        }
        System.out.println("\nBest move explanation:");
        System.out.println(explanation);
        System.out.println("========================\n");

        assertNotNull(explanation);
        assertFalse(explanation.isEmpty());
        assertTrue(explanation.contains("Discard"));
        assertTrue(explanation.contains("Shanten"));
        
        // Verify optimal moves (EAST or WEST)
        MoveSuggestion eastDiscard = allSuggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst()
            .orElse(null);
        assertNotNull(eastDiscard, "Should have EAST discard suggestion");
        assertEquals(0, eastDiscard.getShantenAfterDiscard(), "Discarding EAST should result in 0-shanten (tenpai)");
        assertEquals(3, eastDiscard.getUkeireCount(), "Discarding EAST should have 3 tiles ukeire (waiting for EAST)");
        
        MoveSuggestion westDiscard = allSuggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST)
            .findFirst()
            .orElse(null);
        assertNotNull(westDiscard, "Should have WEST discard suggestion");
        assertEquals(0, westDiscard.getShantenAfterDiscard(), "Discarding WEST should result in 0-shanten (tenpai)");
        assertEquals(3, westDiscard.getUkeireCount(), "Discarding WEST should have 3 tiles ukeire (waiting for WEST)");
        
        // Verify suboptimal moves (breaking melds)
        // All of these should result in 1-shanten (3 complete melds + 1 pair + 2 isolated)
        // Currently they show 5-shanten due to chiitoitsu calculation dominating
        
        TileType[] meldTiles = {
            TileType.M1, TileType.M2, TileType.M3,  // M1-M2-M3 sequence
            TileType.M4, TileType.M5, TileType.M6,  // M4-M5-M6 sequence
            TileType.P1,                             // P1-P1-P1 triplet
            TileType.S5, TileType.S6, TileType.S7   // S5-S6-S7 sequence
        };
        
        for (TileType tileType : meldTiles) {
            MoveSuggestion discard = allSuggestions.stream()
                .filter(s -> s.getDiscardTile() == tileType)
                .findFirst()
                .orElse(null);
            assertNotNull(discard, "Should have " + tileType + " discard suggestion");
            assertEquals(1, discard.getShantenAfterDiscard(),
                      "Discarding " + tileType + " should result in 1-shanten");
        }
        
    }

    @Test
    void testSuggestionsSortedByQuality() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.M7), new Tile(TileType.M8),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.EAST)
        );

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(hand);

        assertFalse(suggestions.isEmpty());
        
        for (int i = 0; i < suggestions.size() - 1; i++) {
            MoveSuggestion current = suggestions.get(i);
            MoveSuggestion next = suggestions.get(i + 1);
            
            assertTrue(current.getShantenAfterDiscard() <= next.getShantenAfterDiscard());
        }
    }

    @Test
    void testGetBestMoveForNullHand() {
        MoveSuggestion bestMove = suggestionService.getBestMove(null);
        assertNull(bestMove);
    }

    @Test
    void testOpponentMeldTilesCountedAsVisible() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> suggestionsNoMelds = suggestionService.suggestMoves(hand);
        MoveSuggestion eastDiscardNoMelds = suggestionsNoMelds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst().orElse(null);
        assertNotNull(eastDiscardNoMelds);
        assertEquals(3, eastDiscardNoMelds.getUkeireCount(),
            "Without melds, discarding EAST should have 3 ukeire (3 WEST tiles in wall)");

        MeldDTO westPon = new MeldDTO(MeldType.PON,
            List.of(TileType.WEST, TileType.WEST, TileType.WEST));
        PlayerDiscardsDTO opponent = new PlayerDiscardsDTO(
            Wind.SOUTH, Collections.emptyList(), false, List.of(westPon));

        List<MoveSuggestion> suggestionsWithMelds = suggestionService.suggestMoves(hand, List.of(opponent));
        MoveSuggestion eastDiscardWithMelds = suggestionsWithMelds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST)
            .findFirst().orElse(null);
        assertNotNull(eastDiscardWithMelds);
        assertEquals(0, eastDiscardWithMelds.getUkeireCount(),
            "Opponent's WEST pon locks all 3 remaining WEST tiles, so ukeire should be 0");
    }

    @Test
    void testDefenseChangesRankingWhenShantenAndUkeireTie() {
        // 3 melds + ryanmen 23s + three isolated honors. Discarding any honor
        // leaves the same 1s/4s wait, so shanten and ukeire tie.
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.WEST), new Tile(TileType.NORTH)
        );

        var eastDiscard = new com.mahjong.dto.DiscardedTileDTO(TileType.EAST, false);
        PlayerDiscardsDTO opponent = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(eastDiscard), false, List.of());

        List<MoveSuggestion> baseline = suggestionService.suggestMoves(hand);
        List<MoveSuggestion> defended = suggestionService.suggestMoves(hand, List.of(opponent));

        MoveSuggestion east = defended.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        MoveSuggestion west = defended.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST).findFirst().orElseThrow();

        assertEquals(east.getShantenAfterDiscard(), west.getShantenAfterDiscard());
        assertTrue(east.getReasoning().contains("Genbutsu"));
        int baselineGap = indexOf(baseline, TileType.EAST) - indexOf(baseline, TileType.WEST);
        int defendedGap = indexOf(defended, TileType.EAST) - indexOf(defended, TileType.WEST);
        assertTrue(defendedGap < baselineGap || indexOf(defended, TileType.EAST) < indexOf(defended, TileType.WEST),
            "Genbutsu EAST vs SOUTH pond must improve EAST's rank relative to WEST");
    }

    private static int indexOf(List<MoveSuggestion> suggestions, TileType tile) {
        for (int i = 0; i < suggestions.size(); i++) {
            if (suggestions.get(i).getDiscardTile() == tile) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Test
    void testSeatWindKeepsYakuhaiOnTiebreak() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P1), new Tile(TileType.P1), new Tile(TileType.P1),
            new Tile(TileType.S5), new Tile(TileType.S6), new Tile(TileType.S7),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );

        List<MoveSuggestion> withWinds = suggestionService.suggestMoves(
            hand, List.of(), List.of(), Wind.EAST, Wind.EAST);
        MoveSuggestion east = withWinds.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        MoveSuggestion west = withWinds.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST).findFirst().orElseThrow();

        assertEquals(0, east.getShantenAfterDiscard());
        assertEquals(0, west.getShantenAfterDiscard());
        assertTrue(withWinds.indexOf(west) < withWinds.indexOf(east),
            "Seat/round EAST is yakuhai and should be kept when WEST is equally efficient");
        assertTrue(east.getReasoning().contains("Yakuhai"));
    }

    @Test
    void ownMeldTilesCountedAsVisible() {
        List<Tile> openHand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );
        MeldDTO p1Pon = new MeldDTO(MeldType.PON, List.of(TileType.P1, TileType.P1, TileType.P1));

        List<MoveSuggestion> open = suggestionService.suggestMoves(
            openHand, List.of(), List.of(), null, null, List.of(p1Pon), List.of());
        assertFalse(open.isEmpty(), "Open 11-tile + pon must still produce discards");
        assertEquals(0, open.stream().mapToInt(MoveSuggestion::getShantenAfterDiscard).min().orElseThrow(),
            "3 sequences + 2 honors + P1 pon is tenpai after discarding an isolated honor");
    }

    @Test
    void ownMeldReducesUkeireLikeOpponentMeld() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.EAST), new Tile(TileType.WEST)
        );
        MeldDTO westPon = new MeldDTO(MeldType.PON,
            List.of(TileType.WEST, TileType.WEST, TileType.WEST));

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(
            hand, List.of(), List.of(), null, null, List.of(westPon), List.of());
        MoveSuggestion eastDiscard = suggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        assertEquals(0, eastDiscard.getUkeireCount(),
            "Own WEST pon locks remaining WEST copies, so discarding EAST has 0 ukeire");
        assertEquals(0, eastDiscard.getShantenAfterDiscard());
    }

    @Test
    void doraPanelIsDoraAsDisplayedNoTenhouRemap() {
        // 3 melds + ryanmen 23s + three isolated honors. Discarding any honor
        // leaves the same 1s/4s wait, so shanten and ukeire tie — keep-value can fire.
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S2), new Tile(TileType.S3),
            new Tile(TileType.EAST), new Tile(TileType.WEST), new Tile(TileType.NORTH)
        );

        List<MoveSuggestion> baseline = suggestionService.suggestMoves(hand);
        List<MoveSuggestion> withDora = suggestionService.suggestMoves(
            hand, List.of(), List.of(), null, null, List.of(), List.of(TileType.WEST));

        MoveSuggestion west = withDora.stream()
            .filter(s -> s.getDiscardTile() == TileType.WEST).findFirst().orElseThrow();
        MoveSuggestion east = withDora.stream()
            .filter(s -> s.getDiscardTile() == TileType.EAST).findFirst().orElseThrow();
        MoveSuggestion north = withDora.stream()
            .filter(s -> s.getDiscardTile() == TileType.NORTH).findFirst().orElseThrow();

        assertEquals(east.getShantenAfterDiscard(), west.getShantenAfterDiscard());
        assertTrue(west.getReasoning().contains("Dora"));
        assertFalse(north.getReasoning().contains("Dora"),
            "Tenhou +1 would treat a WEST indicator as NORTH dora; Doman does not remap");
        assertTrue(withDora.indexOf(east) < withDora.indexOf(west),
            "Doman panel WEST is the dora itself and should be kept vs equally efficient EAST");
        int baselineGap = indexOf(baseline, TileType.WEST) - indexOf(baseline, TileType.EAST);
        int doraGap = indexOf(withDora, TileType.WEST) - indexOf(withDora, TileType.EAST);
        assertTrue(doraGap > baselineGap || withDora.indexOf(west) > withDora.indexOf(east),
            "WEST as dora must drop in rank relative to EAST");
    }

    @Test
    void honbaAndScoresFoldDangerousUkeireForGenbutsu() {
        // 123m 456m 789p 55s 567p — discard P5/P7 is ryanmen tenpai; S5 is tanki genbutsu.
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S5), new Tile(TileType.S5),
            new Tile(TileType.P5), new Tile(TileType.P6), new Tile(TileType.P7)
        );
        var s5Discard = new com.mahjong.dto.DiscardedTileDTO(TileType.S5, false);
        PlayerDiscardsDTO openRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(s5Discard), false, List.of());
        PlayerDiscardsDTO riichiRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(s5Discard), true, List.of());
        List<PlayerDiscardsDTO> opponents = List.of(riichiRight);

        List<MoveSuggestion> push = suggestionService.suggestMoves(hand, List.of(openRight));
        TableSituation foldTable = TableSituation.from(
            3, 2, 4, 35000, 28000, 27000, 26000, opponents, null, Wind.SOUTH);
        List<MoveSuggestion> fold = suggestionService.suggestMoves(
            hand, opponents, List.of(), null, null, List.of(), List.of(), foldTable);

        MoveSuggestion pushP5 = push.stream()
            .filter(s -> s.getDiscardTile() == TileType.P5).findFirst().orElseThrow();
        MoveSuggestion pushS5 = push.stream()
            .filter(s -> s.getDiscardTile() == TileType.S5).findFirst().orElseThrow();
        assertEquals(pushP5.getShantenAfterDiscard(), pushS5.getShantenAfterDiscard());
        assertTrue(pushP5.getUkeireCount() > pushS5.getUkeireCount(),
            "P5 must be the offensive (higher ukeire) discard vs tanki S5");
        assertTrue(pushP5.getReasoning().contains("Untagged simple"));
        assertTrue(indexOf(push, TileType.S5) < indexOf(push, TileType.P5),
            "P5 has no safety tag, so it ranks after genbutsu S5 even though ukeire is higher and nobody is riichi");

        assertTrue(foldTable.preferDefense());
        assertTrue(indexOf(fold, TileType.S5) < indexOf(fold, TileType.P5),
            "Honba/scores must fold: genbutsu S5 ahead of dangerous P5");
        MoveSuggestion foldS5 = fold.stream()
            .filter(s -> s.getDiscardTile() == TileType.S5).findFirst().orElseThrow();
        assertTrue(foldS5.getReasoning().contains("Fold") || foldS5.getReasoning().contains("Genbutsu"));
    }

    @Test
    void nestedPlayerRiichiUsesSelfDefensePosture() {
        List<Tile> hand = Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S5), new Tile(TileType.S5),
            new Tile(TileType.P5), new Tile(TileType.P6), new Tile(TileType.P7)
        );
        var s5Discard = new com.mahjong.dto.DiscardedTileDTO(TileType.S5, false);
        PlayerDiscardsDTO riichiRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(s5Discard), true, List.of());
        PlayerDiscardsDTO self = new PlayerDiscardsDTO();
        self.setRiichi(true);
        TableSituation already = TableSituation.from(
            null, null, null, null, null, null, null, List.of(riichiRight), self, null);

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(
            hand, List.of(riichiRight), List.of(), null, null, List.of(), List.of(), already);

        assertTrue(already.alreadyRiichi());
        assertTrue(indexOf(suggestions, TileType.S5) < indexOf(suggestions, TileType.P5),
            "Already-riichi must prefer genbutsu over the higher-ukeire deal-in");
        assertTrue(suggestions.get(0).getReasoning().contains("Already riichi")
            || suggestions.stream().anyMatch(s -> s.getReasoning().contains("Already riichi")));
    }

    @Test
    void opponentRiichiRanksDangerAheadOfUkeireWithoutFolding() {
        // Same shape as the honba fold fixture. P5 is ryanmen (higher ukeire and good-shape);
        // S5 is tanki genbutsu against the riichi pond. Old ranking keeps P5 first because
        // one riichi adds only 3 fold pressure and the threshold is 6.
        List<Tile> hand = tenpaiHonorsHand();
        var s5Discard = new com.mahjong.dto.DiscardedTileDTO(TileType.S5, false);
        PlayerDiscardsDTO riichiRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(s5Discard), true, List.of());
        List<PlayerDiscardsDTO> opponents = List.of(riichiRight);

        TableSituation quiet = TableSituation.from(
            0, 0, 1, 25000, 25000, 25000, 25000, opponents, null, Wind.EAST);
        assertEquals(3, quiet.foldPressure());
        assertFalse(quiet.preferDefense());
        assertFalse(quiet.cautiousRiichi());
        assertFalse(quiet.skipNonImprovingCalls());
        assertTrue(quiet.rankDangerBeforeOffense());

        List<MoveSuggestion> defended = suggestionService.suggestMoves(
            hand, opponents, List.of(), null, null, List.of(), List.of(), quiet);
        assertDangerBeforeUkeire(defended);

        List<MoveSuggestion> fromOpponentsOnly = suggestionService.suggestMoves(hand, opponents);
        assertDangerBeforeUkeire(fromOpponentsOnly);
    }

    @Test
    void lastPlaceOpponentRiichiStillRanksDangerFirst() {
        List<Tile> hand = tenpaiHonorsHand();
        var s5Discard = new com.mahjong.dto.DiscardedTileDTO(TileType.S5, false);
        PlayerDiscardsDTO riichiRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(s5Discard), true, List.of());
        TableSituation last = TableSituation.from(
            0, 0, 1, 8000, 25000, 28000, 30000, List.of(riichiRight), null, Wind.EAST);
        assertTrue(last.isLastPlace());
        assertFalse(last.preferDefense(), "Last place still cancels fold pressure");
        assertTrue(last.rankDangerBeforeOffense());

        List<MoveSuggestion> suggestions = suggestionService.suggestMoves(
            hand, List.of(riichiRight), List.of(), null, null, List.of(), List.of(), last);
        assertDangerBeforeUkeire(suggestions);
    }

    @Test
    void unknownSimpleRanksAfterTerminalOrHonorAtSameShanten() {
        // 123m 456m 789p 11s 567p. Discarding P5 or P7 leaves a ryanmen tenpai
        // (higher ukeire). Discarding S1 leaves a tanki. Nobody is riichi, so the
        // old sort throws the untagged simple first.
        List<Tile> terminalHand = ryanmenVersusTanki(TileType.S1);
        List<MoveSuggestion> quiet = suggestionService.suggestMoves(terminalHand);
        assertYaochuBeforeUntaggedSimple(quiet, TileType.S1, TileType.P5);
        assertYaochuBeforeUntaggedSimple(quiet, TileType.S1, TileType.P7);

        List<MoveSuggestion> honorQuiet = suggestionService.suggestMoves(ryanmenVersusTanki(TileType.EAST));
        assertYaochuBeforeUntaggedSimple(honorQuiet, TileType.EAST, TileType.P5);
        assertYaochuBeforeUntaggedSimple(honorQuiet, TileType.EAST, TileType.P7);

        int minShanten = quiet.stream().mapToInt(MoveSuggestion::getShantenAfterDiscard).min().orElseThrow();
        assertEquals(minShanten, quiet.get(0).getShantenAfterDiscard(),
            "Untagged-simple demotion stays behind minimum shanten");
        for (int i = 0; i < quiet.size() - 1; i++) {
            assertTrue(quiet.get(i).getShantenAfterDiscard() <= quiet.get(i + 1).getShantenAfterDiscard());
        }

        // P7 in the pond is genbutsu (a safety tag the danger score already computes).
        // It keeps its ukeire rank. Untagged P5 still follows the terminal.
        var p7Discard = new com.mahjong.dto.DiscardedTileDTO(TileType.P7, false);
        PlayerDiscardsDTO openRight = new PlayerDiscardsDTO(
            Wind.SOUTH, List.of(p7Discard), false, List.of());
        TableSituation quietTable = TableSituation.from(
            0, 0, 1, 25000, 25000, 25000, 25000, List.of(openRight), null, Wind.EAST);
        assertFalse(quietTable.opponentRiichi());
        assertFalse(quietTable.preferDefense());
        assertFalse(quietTable.rankDangerBeforeOffense());
        assertFalse(quietTable.skipNonImprovingCalls());

        List<MoveSuggestion> tagged = suggestionService.suggestMoves(
            terminalHand, List.of(openRight), List.of(), null, null, List.of(), List.of(), quietTable);
        MoveSuggestion taggedP7 = find(tagged, TileType.P7);
        MoveSuggestion taggedS1 = find(tagged, TileType.S1);
        assertEquals(taggedP7.getShantenAfterDiscard(), taggedS1.getShantenAfterDiscard());
        assertTrue(taggedP7.getUkeireCount() > taggedS1.getUkeireCount());
        assertTrue(taggedP7.getReasoning().contains("Genbutsu"));
        assertFalse(taggedP7.getReasoning().contains("Untagged simple"),
            "A genbutsu simple is not an untagged simple");
        assertTrue(indexOf(tagged, TileType.P7) < indexOf(tagged, TileType.S1),
            "A tagged simple still outranks a terminal on ukeire when nobody is riichi");
        assertYaochuBeforeUntaggedSimple(tagged, TileType.S1, TileType.P5);
    }

    @Test
    void oneChanceSimpleRanksAfterTerminalOrHonorAtSameShanten() {
        // 123m 456m 789p 11s 567p, plus a south pon of P2. Three P2s make P5
        // one-chance (2 is the middle of 2-5-8) without putting P5 in the pond.
        // P5 is still the higher-ukeire ryanmen. Today's order treats that
        // one-chance mark as a safety tag and discards P5 ahead of S1.
        PlayerDiscardsDTO south = p2Pon(false);
        List<Tile> terminalHand = ryanmenVersusTanki(TileType.S1);
        List<MoveSuggestion> quiet = suggestionService.suggestMoves(terminalHand, List.of(south));
        MoveSuggestion p5 = find(quiet, TileType.P5);
        assertTrue(p5.getReasoning().contains("One-chance"),
            "The one-chance note stays; it is not a safety tag");
        assertYaochuBeforeUntaggedSimple(quiet, TileType.S1, TileType.P5);

        List<MoveSuggestion> honorQuiet = suggestionService.suggestMoves(
            ryanmenVersusTanki(TileType.EAST), List.of(south));
        MoveSuggestion honorP5 = find(honorQuiet, TileType.P5);
        assertTrue(honorP5.getReasoning().contains("One-chance"));
        assertYaochuBeforeUntaggedSimple(honorQuiet, TileType.EAST, TileType.P5);

        int minShanten = quiet.stream().mapToInt(MoveSuggestion::getShantenAfterDiscard).min().orElseThrow();
        assertEquals(minShanten, quiet.get(0).getShantenAfterDiscard(),
            "One-chance demotion stays behind minimum shanten");
        for (int i = 0; i < quiet.size() - 1; i++) {
            assertTrue(quiet.get(i).getShantenAfterDiscard() <= quiet.get(i + 1).getShantenAfterDiscard());
        }

        // Opponent riichi keeps the danger-before-ukeire comparator. One-chance's
        // old base-1 score would still discard P5 ahead of S1 on that path.
        PlayerDiscardsDTO riichi = p2Pon(true);
        TableSituation riichiTable = TableSituation.from(
            0, 0, 1, 25000, 25000, 25000, 25000, List.of(riichi), null, Wind.EAST);
        assertTrue(riichiTable.rankDangerBeforeOffense());
        assertFalse(riichiTable.preferDefense());
        assertFalse(riichiTable.skipNonImprovingCalls());
        List<MoveSuggestion> defended = suggestionService.suggestMoves(
            terminalHand, List.of(riichi), List.of(), null, null, List.of(), List.of(), riichiTable);
        MoveSuggestion defendedP5 = find(defended, TileType.P5);
        MoveSuggestion defendedS1 = find(defended, TileType.S1);
        assertEquals(defendedP5.getShantenAfterDiscard(), defendedS1.getShantenAfterDiscard());
        assertTrue(defendedP5.getUkeireCount() > defendedS1.getUkeireCount());
        assertTrue(defendedP5.getReasoning().contains("One-chance"));
        assertTrue(indexOf(defended, TileType.S1) < indexOf(defended, TileType.P5),
            "Against riichi, a one-chance simple must not outrank a same-shanten terminal");
    }

    @Test
    void pushComparatorRanksUntaggedSimpleAfterSameShantenYaochu() {
        MoveSuggestion yaochu = new MoveSuggestion(TileType.S1, 0);
        yaochu.setUkeireCount(3);
        MoveSuggestion simple = new MoveSuggestion(TileType.P7, 0);
        simple.setUkeireCount(7);
        var ranked = new java.util.ArrayList<>(List.of(
            new MoveSuggestionService.RankedSuggestion(simple, 7, 6, 0, 0, 1),
            new MoveSuggestionService.RankedSuggestion(yaochu, 0, 4, 0, 0, 0)
        ));
        ranked.sort(MoveSuggestionService.rankComparator(false));
        assertEquals(TileType.S1, ranked.get(0).suggestion().getDiscardTile(),
            "Old push order sorts ukeire ahead of danger and would discard P7");

        MoveSuggestion slowerTerminal = new MoveSuggestion(TileType.M9, 1);
        slowerTerminal.setUkeireCount(0);
        var shantenFirst = new java.util.ArrayList<>(List.of(
            new MoveSuggestionService.RankedSuggestion(slowerTerminal, 0, 4, 0, 0, 0),
            new MoveSuggestionService.RankedSuggestion(simple, 7, 6, 0, 0, 1)
        ));
        shantenFirst.sort(MoveSuggestionService.rankComparator(false));
        assertEquals(TileType.P7, shantenFirst.get(0).suggestion().getDiscardTile(),
            "Untagged-simple penalty stays behind minimum shanten");

        MoveSuggestion saferSimple = new MoveSuggestion(TileType.P7, 0);
        saferSimple.setUkeireCount(3);
        MoveSuggestion widerYaochu = new MoveSuggestion(TileType.S1, 0);
        widerYaochu.setUkeireCount(8);
        var riichiOrder = new java.util.ArrayList<>(List.of(
            new MoveSuggestionService.RankedSuggestion(widerYaochu, 0, 4, 0, 0, 0),
            new MoveSuggestionService.RankedSuggestion(saferSimple, 0, 0, 0, 0, 1)
        ));
        riichiOrder.sort(MoveSuggestionService.rankComparator(true));
        assertEquals(TileType.P7, riichiOrder.get(0).suggestion().getDiscardTile(),
            "Opponent-riichi order still sorts danger ahead of ukeire and ignores the push-only penalty");
    }

    @Test
    void dangerBeforeOffenseStillYieldsToMinimumShanten() {
        MoveSuggestion saferSlower = new MoveSuggestion(TileType.EAST, 2);
        saferSlower.setUkeireCount(0);
        MoveSuggestion dangerousFaster = new MoveSuggestion(TileType.P5, 0);
        dangerousFaster.setUkeireCount(8);
        var ranked = new java.util.ArrayList<>(List.of(
            new MoveSuggestionService.RankedSuggestion(saferSlower, 0, 0, 0, 0, 0),
            new MoveSuggestionService.RankedSuggestion(dangerousFaster, 8, 12, 0, 0, 0)
        ));
        ranked.sort(MoveSuggestionService.rankComparator(true));
        assertEquals(TileType.P5, ranked.get(0).suggestion().getDiscardTile(),
            "Minimum shanten stays ahead of danger even when defense outranks ukeire and good-shape");
    }

    private static PlayerDiscardsDTO p2Pon(boolean riichi) {
        return new PlayerDiscardsDTO(
            Wind.SOUTH,
            List.of(),
            riichi,
            List.of(new MeldDTO(MeldType.PON, List.of(TileType.P2, TileType.P2, TileType.P2))));
    }

    private static List<Tile> ryanmenVersusTanki(TileType tanki) {
        return Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(tanki), new Tile(tanki),
            new Tile(TileType.P5), new Tile(TileType.P6), new Tile(TileType.P7)
        );
    }

    private static void assertYaochuBeforeUntaggedSimple(
            List<MoveSuggestion> suggestions,
            TileType yaochu,
            TileType simple
    ) {
        MoveSuggestion safe = find(suggestions, yaochu);
        MoveSuggestion risky = find(suggestions, simple);
        assertEquals(safe.getShantenAfterDiscard(), risky.getShantenAfterDiscard());
        assertTrue(risky.getUkeireCount() > safe.getUkeireCount(),
            simple + " must have more ukeire than " + yaochu + "; the old sort would keep the simple first");
        assertTrue(indexOf(suggestions, yaochu) < indexOf(suggestions, simple),
            "Untagged " + simple + " must rank after " + yaochu + " at the same shanten when nobody is riichi");
        assertTrue(risky.getReasoning().contains("Untagged simple"));
    }

    private static MoveSuggestion find(List<MoveSuggestion> suggestions, TileType tile) {
        return suggestions.stream()
            .filter(s -> s.getDiscardTile() == tile)
            .findFirst()
            .orElseThrow();
    }

    private static List<Tile> tenpaiHonorsHand() {
        return Arrays.asList(
            new Tile(TileType.M1), new Tile(TileType.M2), new Tile(TileType.M3),
            new Tile(TileType.M4), new Tile(TileType.M5), new Tile(TileType.M6),
            new Tile(TileType.P7), new Tile(TileType.P8), new Tile(TileType.P9),
            new Tile(TileType.S5), new Tile(TileType.S5),
            new Tile(TileType.P5), new Tile(TileType.P6), new Tile(TileType.P7)
        );
    }

    private static void assertDangerBeforeUkeire(List<MoveSuggestion> suggestions) {
        MoveSuggestion p5 = suggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.P5).findFirst().orElseThrow();
        MoveSuggestion s5 = suggestions.stream()
            .filter(s -> s.getDiscardTile() == TileType.S5).findFirst().orElseThrow();
        assertEquals(p5.getShantenAfterDiscard(), s5.getShantenAfterDiscard());
        assertTrue(p5.getUkeireCount() > s5.getUkeireCount(),
            "P5 must stay the higher-ukeire ryanmen; the old sort would keep it first");
        assertTrue(indexOf(suggestions, TileType.S5) < indexOf(suggestions, TileType.P5),
            "Opponent riichi must rank genbutsu S5 ahead of higher-ukeire, better-shape P5");
        int minShanten = suggestions.stream().mapToInt(MoveSuggestion::getShantenAfterDiscard).min().orElseThrow();
        assertEquals(minShanten, suggestions.get(0).getShantenAfterDiscard());
        assertTrue(s5.getReasoning().contains("Opponent riichi") || s5.getReasoning().contains("Genbutsu"));
    }
}

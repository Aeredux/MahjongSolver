package com.mahjong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.dto.CallDecisionRequest;
import com.mahjong.dto.HandRequest;
import com.mahjong.model.CallType;
import com.mahjong.model.TileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MahjongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("Mahjong AI service is running"));
    }

    @Test
    void testSuggestMoveEndpoint() throws Exception {
        List<TileType> hand = Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5, TileType.M6,
            TileType.M7, TileType.M8, TileType.M9,
            TileType.P1, TileType.P1, TileType.P1,
            TileType.S5
        );

        HandRequest request = new HandRequest();
        request.setHand(hand);
        request.setDrawnTile(TileType.S6);

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.current_shanten").exists())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].discard_tile").exists())
            .andExpect(jsonPath("$.suggestions[0].shanten_after_discard").exists())
            .andExpect(jsonPath("$.suggestions[0].confidence").exists())
            .andExpect(jsonPath("$.suggestions[0].reasoning").exists());
    }

    @Test
    void testEvaluateRonCall() throws Exception {
        List<TileType> hand = Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5, TileType.M6,
            TileType.M7, TileType.M8, TileType.M9,
            TileType.P1, TileType.P1, TileType.P1,
            TileType.S5
        );

        CallDecisionRequest request = new CallDecisionRequest();
        request.setHand(hand);
        request.setCalledTile(TileType.S5);
        request.setCallType(CallType.RON);

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("RON"))
            .andExpect(jsonPath("$.should_call").value(true))
            .andExpect(jsonPath("$.confidence").value(1.0))
            .andExpect(jsonPath("$.reasoning").exists());
    }

    @Test
    void testEvaluateRiichiCall() throws Exception {
        List<TileType> tenpaiHand = Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5, TileType.M6,
            TileType.M7, TileType.M8, TileType.M9,
            TileType.P1, TileType.P1, TileType.P1,
            TileType.S5
        );

        CallDecisionRequest request = new CallDecisionRequest();
        request.setHand(tenpaiHand);
        request.setCallType(CallType.RIICHI);
        request.setMenzen(true);
        request.setPlayerScore(25000);

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("RIICHI"))
            .andExpect(jsonPath("$.should_call").value(true))
            .andExpect(jsonPath("$.reasoning").exists());
    }

    @Test
    void testEvaluatePonCall() throws Exception {
        List<TileType> hand = Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5, TileType.M6,
            TileType.P5, TileType.P5,
            TileType.S1, TileType.S2, TileType.S3,
            TileType.EAST, TileType.SOUTH
        );

        CallDecisionRequest request = new CallDecisionRequest();
        request.setHand(hand);
        request.setCalledTile(TileType.P5);
        request.setCallType(CallType.PON);

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("PON"))
            .andExpect(jsonPath("$.should_call").exists())
            .andExpect(jsonPath("$.reasoning").exists());
    }

    @Test
    void testEvaluateChiCall() throws Exception {
        List<TileType> hand = Arrays.asList(
            TileType.M1, TileType.M2, TileType.M3,
            TileType.M4, TileType.M5,
            TileType.P1, TileType.P2,
            TileType.S1, TileType.S2, TileType.S3,
            TileType.EAST, TileType.SOUTH, TileType.WEST
        );

        CallDecisionRequest request = new CallDecisionRequest();
        request.setHand(hand);
        request.setCalledTile(TileType.P3);
        request.setCallType(CallType.CHI);
        request.setSequenceTiles(Arrays.asList(TileType.P1, TileType.P2));

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("CHI"))
            .andExpect(jsonPath("$.should_call").exists())
            .andExpect(jsonPath("$.reasoning").exists());
    }

    @Test
    void testSuggestMoveConsumesSeatAndRoundWind() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M5","M6","P1","P1","P1","S5","S6","S7","EAST"],
              "drawn_tile": "WEST",
              "seat_wind": "EAST",
              "round_wind": "SOUTH"
            }
            """;

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.current_shanten").exists())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].discard_tile").exists())
            .andExpect(jsonPath("$.suggestions[0].ukeire_count").exists());
    }

    @Test
    void testSuggestMoveWithEmptyHand() throws Exception {
        HandRequest request = new HandRequest();
        request.setHand(Arrays.asList());

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.suggestions").isEmpty());
    }

    @Test
    void suggestMoveConsumesDoraOwnMeldsAndAka() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M5","M6","P7","P8","P9","EAST","WEST"],
              "drawn_tile": null,
              "dora": ["WEST"],
              "melds": [{"type": "PON", "tiles": ["P1","P1","P1"]}],
              "discard_tiles": ["M9"],
              "seat_wind": "SOUTH",
              "round_wind": "EAST"
            }
            """;

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.current_shanten").exists())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].discard_tile").exists());
    }

    @Test
    void suggestMoveAcceptsAkaAliasesAndNestedPlayer() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M0","M6","P1","P1","P1","S5","S6","S7","EAST"],
              "drawn_tile": "WEST",
              "dora": ["M0"],
              "player": {
                "wind": "EAST",
                "riichi": false,
                "discards": [{"tile": "S9", "tsumogiri": true}],
                "melds": []
              }
            }
            """;

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.current_shanten").exists())
            .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    void suggestMoveReadsNestedPlayerMelds() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M5","M6","P7","P8","P9","EAST","WEST"],
              "dora": ["EAST"],
              "player": {
                "wind": "EAST",
                "riichi": false,
                "discards": [],
                "melds": [{"type": "PON", "tiles": ["P1","P1","P1"]}]
              }
            }
            """;

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].shanten_after_discard").value(0));
    }

    @Test
    void evaluateCallAcceptsDoraAndOwnMelds() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M5","M6","P5","P5","S1","S2","S3","EAST","SOUTH"],
              "called_tile": "P5",
              "call_type": "PON",
              "dora": ["P5"],
              "melds": []
            }
            """;

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("PON"))
            .andExpect(jsonPath("$.should_call").exists())
            .andExpect(jsonPath("$.reasoning").exists());
    }

    @Test
    void tableMetaZeroHonbaIsKnownNotUnread() throws Exception {
        String json = """
            {
              "honba": 0,
              "riichi_sticks": 0,
              "round_number": 1,
              "round_wind": "EAST",
              "player_score": 25000,
              "right_score": 25000,
              "opposite_score": 25000,
              "left_score": 25000
            }
            """;
        HandRequest request = objectMapper.readValue(json, HandRequest.class);
        assertEquals(0, request.getHonba());
        assertEquals(0, request.getRiichiSticks());
        assertEquals(1, request.getRoundNumber());
        assertEquals(25000, request.getPlayerScore());
        assertEquals(25000, request.getRightScore());
        assertEquals(25000, request.getOppositeScore());
        assertEquals(25000, request.getLeftScore());
        assertFalse(com.mahjong.service.TableSituation.from(request).preferDefense());
    }

    @Test
    void suggestMoveAcceptsHonbaAndTableScores() throws Exception {
        String body = """
            {
              "hand": ["M1","M2","M3","M4","M5","M6","P7","P8","P9","S5","S5","P5","P6"],
              "drawn_tile": "P7",
              "honba": 3,
              "riichi_sticks": 2,
              "round_number": 4,
              "round_wind": "SOUTH",
              "player_score": 35000,
              "right_score": 28000,
              "opposite_score": 27000,
              "left_score": 26000,
              "opponents": [
                {
                  "wind": "SOUTH",
                  "riichi": true,
                  "discards": [{"tile": "S5", "tsumogiri": false}]
                }
              ]
            }
            """;

        mockMvc.perform(post("/api/suggest-move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].discard_tile").exists())
            .andExpect(jsonPath("$.suggestions[0].reasoning").exists());
    }

    @Test
    void evaluateRiichiAcceptsHonbaAndFoldsWithYaku() throws Exception {
        String body = """
            {
              "hand": ["M2","M3","M4","M5","M6","M7","P2","P3","P4","P8","P8","S5","S6"],
              "call_type": "RIICHI",
              "menzen": true,
              "player_score": 35000,
              "honba": 3,
              "riichi_sticks": 2,
              "round_number": 4,
              "round_wind": "SOUTH",
              "right_score": 32000,
              "opposite_score": 28000,
              "left_score": 25000
            }
            """;

        mockMvc.perform(post("/api/evaluate-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.call_type").value("RIICHI"))
            .andExpect(jsonPath("$.should_call").value(false))
            .andExpect(jsonPath("$.reasoning").exists());
    }
}

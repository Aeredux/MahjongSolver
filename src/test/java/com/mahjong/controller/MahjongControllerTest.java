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
            .andExpect(jsonPath("$.currentShanten").exists())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].discardTile").exists())
            .andExpect(jsonPath("$.suggestions[0].shantenAfterDiscard").exists())
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
            .andExpect(jsonPath("$.callType").value("RON"))
            .andExpect(jsonPath("$.shouldCall").value(true))
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
            .andExpect(jsonPath("$.callType").value("RIICHI"))
            .andExpect(jsonPath("$.shouldCall").value(true))
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
            .andExpect(jsonPath("$.callType").value("PON"))
            .andExpect(jsonPath("$.shouldCall").exists())
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
            .andExpect(jsonPath("$.callType").value("CHI"))
            .andExpect(jsonPath("$.shouldCall").exists())
            .andExpect(jsonPath("$.reasoning").exists());
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
}

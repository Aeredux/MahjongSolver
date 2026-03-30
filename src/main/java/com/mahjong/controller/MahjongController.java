package com.mahjong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.dto.*;
import com.mahjong.model.*;
import com.mahjong.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Mahjong AI", description = "AI move suggestions and call decisions for Japanese Riichi Mahjong")
public class MahjongController {
    
    private static final Logger logger = LoggerFactory.getLogger(MahjongController.class);

    @Autowired
    private MoveSuggestionService moveSuggestionService;

    @Autowired
    private CallDecisionService callDecisionService;

    @Autowired
    private ShantenCalculator shantenCalculator;

    @Autowired
    private GameHistoryService gameHistoryService;

    @Autowired
    private ApiCallLogService apiCallLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/suggest-move")
    @Operation(summary = "Get move suggestions", description = "Returns ranked list of tile discard suggestions based on shanten minimization")
    public ResponseEntity<MoveSuggestionResponse> suggestMove(@RequestBody HandRequest request) {
        logger.info("Received move suggestion request for hand with {} tiles",
                   request.getHand() != null ? request.getHand().size() : 0);

        long startTime = System.currentTimeMillis();
        String requestJson = toJson(request);

        try {
            List<Tile> hand = convertToTiles(request.getHand());

            if (request.getDrawnTile() != null) {
                hand.add(new Tile(request.getDrawnTile()));
            }

            int currentShanten = shantenCalculator.calculateShanten(hand);
            List<TileType> ownDiscards = request.getDiscardTiles() != null ? request.getDiscardTiles() : List.of();
            List<MoveSuggestion> suggestions = moveSuggestionService.suggestMoves(hand, request.getOpponents(), ownDiscards);

            MoveSuggestionResponse response = new MoveSuggestionResponse();
            response.setCurrentShanten(currentShanten);
            response.setSuggestions(suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

            logger.info("Returning {} move suggestions, current shanten: {}",
                       suggestions.size(), currentShanten);

            long durationMs = System.currentTimeMillis() - startTime;
            apiCallLogService.log("/api/suggest-move", "POST", requestJson, 200, durationMs, null);
            gameHistoryService.saveGameHistory(
                request.getHand() != null ? request.getHand() : List.of(),
                request.getDrawnTile(),
                currentShanten,
                suggestions,
                request.getDiscardTiles()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing move suggestion request", e);
            long durationMs = System.currentTimeMillis() - startTime;
            apiCallLogService.log("/api/suggest-move", "POST", requestJson, 400, durationMs, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/evaluate-call")
    @Operation(summary = "Evaluate call decision", description = "Evaluates whether to call pon/chi/kan/ron/riichi")
    public ResponseEntity<CallDecisionResponse> evaluateCall(@RequestBody CallDecisionRequest request) {
        logger.info("Received call decision request for type: {}", request.getCallType());

        long startTime = System.currentTimeMillis();
        String requestJson = toJson(request);

        try {
            List<Tile> hand = convertToTiles(request.getHand());
            CallDecision decision;

            switch (request.getCallType()) {
                case RON:
                    Tile ronTile = new Tile(request.getCalledTile());
                    decision = callDecisionService.evaluateRon(hand, ronTile);
                    break;

                case TSUMO:
                    Tile tsumoTile = new Tile(request.getCalledTile());
                    hand.add(tsumoTile);
                    decision = callDecisionService.evaluateTsumo(hand);
                    break;

                case RIICHI:
                    decision = callDecisionService.evaluateRiichi(
                        hand, 
                        request.isMenzen(), 
                        request.getPlayerScore()
                    );
                    break;

                case PON:
                    Tile ponTile = new Tile(request.getCalledTile());
                    decision = callDecisionService.evaluatePon(hand, ponTile);
                    break;

                case CHI:
                    Tile chiTile = new Tile(request.getCalledTile());
                    List<Tile> sequenceTiles = convertToTiles(request.getSequenceTiles());
                    decision = callDecisionService.evaluateChi(hand, chiTile, sequenceTiles);
                    break;

                case KAN:
                    Tile kanTile = new Tile(request.getCalledTile());
                    if (!request.isOpenKan()) {
                        hand.add(kanTile);
                    }
                    decision = callDecisionService.evaluateKan(hand, kanTile, request.isOpenKan());
                    break;

                default:
                    logger.warn("Unknown call type: {}", request.getCallType());
                    return ResponseEntity.badRequest().build();
            }

            CallDecisionResponse response = convertToDTO(decision);

            logger.info("Call decision for {}: {} (confidence: {})",
                       request.getCallType(), decision.isShouldCall(), decision.getConfidence());

            long durationMs = System.currentTimeMillis() - startTime;
            apiCallLogService.log("/api/evaluate-call", "POST", requestJson, 200, durationMs, null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing call decision request", e);
            long durationMs = System.currentTimeMillis() - startTime;
            apiCallLogService.log("/api/evaluate-call", "POST", requestJson, 400, durationMs, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/game-state/{id}")
    @Operation(summary = "Get game state by ID", description = "Retrieves a stored game history record by its ID")
    public ResponseEntity<?> getGameState(@PathVariable Long id) {
        return gameHistoryService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/validate-move")
    @Operation(summary = "Validate a move", description = "Checks whether discarding a tile from the given hand is legal")
    public ResponseEntity<Map<String, Object>> validateMove(@RequestBody ValidateMoveRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        if (request.getHand() == null || request.getHand().isEmpty()) {
            errors.add("Hand must not be empty.");
        }
        if (request.getDiscardTile() == null) {
            errors.add("discardTile must not be null.");
        }

        if (errors.isEmpty()) {
            boolean tileInHand = request.getHand().contains(request.getDiscardTile());
            if (!tileInHand) {
                errors.add("Tile " + request.getDiscardTile() + " is not in the hand.");
            }

            if (request.isRiichi() && request.getDrawnTile() != null
                    && request.getDiscardTile() != request.getDrawnTile()) {
                errors.add("In riichi you may only discard the drawn tile (tsumogiri). "
                        + "Expected " + request.getDrawnTile() + ", got " + request.getDiscardTile() + ".");
            }

            int handSize = request.getHand().size();
            if (handSize != 13 && handSize != 14) {
                errors.add("Hand size must be 13 or 14 (got " + handSize + ").");
            }
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        logger.info("validate-move: discardTile={} valid={} errors={}",
                request.getDiscardTile(), errors.isEmpty(), errors);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    @Operation(summary = "Game history", description = "Returns all past move suggestion requests")
    public ResponseEntity<List<com.mahjong.entity.GameHistory>> getHistory() {
        return ResponseEntity.ok(gameHistoryService.findAll());
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mahjong AI service is running");
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Failed to serialize request to JSON", e);
            return null;
        }
    }

    private List<Tile> convertToTiles(List<TileType> tileTypes) {
        if (tileTypes == null) {
            return new ArrayList<>();
        }
        return tileTypes.stream()
            .map(Tile::new)
            .collect(Collectors.toList());
    }

    private MoveSuggestionResponse.MoveSuggestionDTO convertToDTO(MoveSuggestion suggestion) {
        return new MoveSuggestionResponse.MoveSuggestionDTO(
            suggestion.getDiscardTile(),
            suggestion.getShantenAfterDiscard(),
            suggestion.getConfidence(),
            suggestion.getReasoning(),
            suggestion.getUkeireCount()
        );
    }

    private CallDecisionResponse convertToDTO(CallDecision decision) {
        return new CallDecisionResponse(
            decision.getCallType(),
            decision.isShouldCall(),
            decision.getConfidence(),
            decision.getReasoning(),
            decision.getShantenBefore(),
            decision.getShantenAfter()
        );
    }
}

package com.mahjong.dto;

import com.mahjong.model.CallType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallDecisionResponse {
    private CallType callType;
    private boolean shouldCall;
    private double confidence;
    private String reasoning;
    private int shantenBefore;
    private int shantenAfter;
}

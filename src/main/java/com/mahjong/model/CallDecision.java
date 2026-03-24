package com.mahjong.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallDecision {
    private CallType callType;
    private boolean shouldCall;
    private double confidence;
    private String reasoning;
    private int shantenBefore;
    private int shantenAfter;

    public CallDecision(CallType callType, boolean shouldCall) {
        this.callType = callType;
        this.shouldCall = shouldCall;
        this.confidence = 0.0;
        this.reasoning = "";
        this.shantenBefore = 0;
        this.shantenAfter = 0;
    }
}

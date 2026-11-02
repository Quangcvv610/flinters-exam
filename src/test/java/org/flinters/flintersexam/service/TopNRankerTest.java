package org.flinters.flintersexam.service;

import org.flinters.flintersexam.model.CampaignMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopNRankerTest {

    private TopNRanker ranker;

    @BeforeEach
    void setUp() {
        ranker = new TopNRanker();
    }

    @Test
    @DisplayName("Should rank campaigns by highest CTR in descending order")
    void testGetTopByHighestCTR() {
        List<CampaignMetrics> campaigns = List.of(
                CampaignMetrics.of("CMP001", 1000, 100, 50.0, 10),  // CTR = 0.10
                CampaignMetrics.of("CMP002", 1000, 50, 50.0, 5),    // CTR = 0.05
                CampaignMetrics.of("CMP003", 1000, 200, 100.0, 20), // CTR = 0.20
                CampaignMetrics.of("CMP004", 1000, 75, 75.0, 7)     // CTR = 0.075
        );

        List<CampaignMetrics> topCtr = ranker.getTopByHighestCTR(campaigns, 3);

        assertEquals(3, topCtr.size());
        assertEquals("CMP003", topCtr.get(0).campaignId()); // CTR 0.20
        assertEquals("CMP001", topCtr.get(1).campaignId()); // CTR 0.10
        assertEquals("CMP004", topCtr.get(2).campaignId()); // CTR 0.075
    }

    @Test
    @DisplayName("Should rank campaigns by lowest CPA and exclude zero conversions")
    void testGetTopByLowestCPA() {
        List<CampaignMetrics> campaigns = List.of(
                CampaignMetrics.of("CMP001", 1000, 100, 100.0, 10), // CPA = 10.0
                CampaignMetrics.of("CMP002", 1000, 50, 150.0, 10),  // CPA = 15.0
                CampaignMetrics.of("CMP003", 1000, 200, 50.0, 10),  // CPA = 5.0
                CampaignMetrics.of("CMP004", 1000, 75, 100.0, 0)    // Conversions = 0 (CPA null)
        );

        List<CampaignMetrics> topCpa = ranker.getTopByLowestCPA(campaigns, 10);

        assertEquals(3, topCpa.size()); // CMP004 must be excluded
        assertEquals("CMP003", topCpa.get(0).campaignId()); // Lowest CPA: 5.0
        assertEquals("CMP001", topCpa.get(1).campaignId()); // CPA: 10.0
        assertEquals("CMP002", topCpa.get(2).campaignId()); // CPA: 15.0
    }

    @Test
    @DisplayName("Should handle empty and null collections gracefully")
    void testEmptyAndNullInput() {
        assertTrue(ranker.getTopByHighestCTR(List.of(), 10).isEmpty());
        assertTrue(ranker.getTopByHighestCTR(null, 10).isEmpty());
        assertTrue(ranker.getTopByLowestCPA(List.of(), 10).isEmpty());
        assertTrue(ranker.getTopByLowestCPA(null, 10).isEmpty());
    }

    @Test
    @DisplayName("Should handle tie-breaking correctly")
    void testTieBreaking() {
        // Two campaigns with identical CTR 0.10, but CMP002 has higher impressions
        List<CampaignMetrics> campaigns = List.of(
                CampaignMetrics.of("CMP001", 1000, 100, 50.0, 10),
                CampaignMetrics.of("CMP002", 5000, 500, 250.0, 50)
        );

        List<CampaignMetrics> topCtr = ranker.getTopByHighestCTR(campaigns, 2);
        assertEquals(2, topCtr.size());
        assertEquals("CMP002", topCtr.get(0).campaignId()); // higher impressions ranked first
        assertEquals("CMP001", topCtr.get(1).campaignId());
    }
}


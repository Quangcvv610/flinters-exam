package org.flinters.flintersexam.model;

import java.util.List;

/**
 * Summary statistics of the analytics processing job.
 */
public record AnalyticsSummary(
        long totalLinesRead,
        long validRecords,
        long skippedRecords,
        int uniqueCampaignsCount,
        long overallImpressions,
        long overallClicks,
        double overallSpend,
        long overallConversions,
        double overallCtr,
        double overallCpa,
        List<CampaignMetrics> top10Ctr,
        List<CampaignMetrics> top10Cpa,
        long durationMs
) {}


package org.flinters.flintersexam.model;

/**
 * High-performance mutable accumulator using primitive fields.
 * Aggregates raw impressions, clicks, spend, and conversions in-place
 * without allocating intermediate objects during streaming.
 */
public class CampaignAggregator {
    private long totalImpressions;
    private long totalClicks;
    private double totalSpend;
    private long totalConversions;

    public CampaignAggregator() {
    }

    public CampaignAggregator(long impressions, long clicks, double spend, long conversions) {
        this.totalImpressions = impressions;
        this.totalClicks = clicks;
        this.totalSpend = spend;
        this.totalConversions = conversions;
    }

    public void accumulate(long impressions, long clicks, double spend, long conversions) {
        this.totalImpressions += impressions;
        this.totalClicks += clicks;
        this.totalSpend += spend;
        this.totalConversions += conversions;
    }

    public CampaignMetrics toMetrics(String campaignId) {
        return CampaignMetrics.of(campaignId, totalImpressions, totalClicks, totalSpend, totalConversions);
    }

    public long getTotalImpressions() {
        return totalImpressions;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public long getTotalConversions() {
        return totalConversions;
    }
}


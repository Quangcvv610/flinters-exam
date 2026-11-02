package org.flinters.flintersexam.model;

/**
 * Immutable representation of aggregated campaign analytics with calculated CTR and CPA.
 */
public record CampaignMetrics(
        String campaignId,
        long totalImpressions,
        long totalClicks,
        double totalSpend,
        long totalConversions,
        double ctr,
        Double cpa // null when totalConversions == 0
) {
    /**
     * Factory method to construct CampaignMetrics with calculated CTR and CPA
     */
    public static CampaignMetrics of(String campaignId, long impressions, long clicks, double spend, long conversions) {
        double ctr = impressions > 0 ? (double) clicks / impressions : 0.0;
        Double cpa = conversions > 0 ? spend / conversions : null;
        return new CampaignMetrics(campaignId, impressions, clicks, spend, conversions, ctr, cpa);
    }
}


package org.flinters.flintersexam.model;

/**
 * Represents a single raw record parsed from the input CSV line.
 */
public record CampaignRecord(
        String campaignId,
        String date,
        long impressions,
        long clicks,
        double spend,
        long conversions
) {}


package org.flinters.flintersexam.service;

import org.flinters.flintersexam.model.CampaignMetrics;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service providing top-N ranking algorithms using bounded priority queues (Min/Max Heaps).
 * Time Complexity: O(K log N) where K is unique campaigns, N is top limit (10).
 * Space Complexity: O(N) bounded memory.
 */
@Service
public class TopNRanker {

    /**
     * Get top N campaigns by highest CTR (Click-Through Rate).
     * Tie-breaker: total impressions (descending), then campaignId (ascending).
     */
    public List<CampaignMetrics> getTopByHighestCTR(Collection<CampaignMetrics> campaigns, int n) {
        if (campaigns == null || campaigns.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }

        // Min-Heap: smallest CTR is at the head
        Comparator<CampaignMetrics> minHeapComparator = Comparator
                .comparingDouble(CampaignMetrics::ctr)
                .thenComparingLong(CampaignMetrics::totalImpressions)
                .thenComparing(Comparator.comparing(CampaignMetrics::campaignId).reversed());

        PriorityQueue<CampaignMetrics> minHeap = new PriorityQueue<>(n, minHeapComparator);

        for (CampaignMetrics campaign : campaigns) {
            if (minHeap.size() < n) {
                minHeap.offer(campaign);
            } else if (minHeapComparator.compare(campaign, minHeap.peek()) > 0) {
                minHeap.poll();
                minHeap.offer(campaign);
            }
        }

        // Extract and sort descending
        List<CampaignMetrics> result = new ArrayList<>(minHeap);
        result.sort(Comparator
                .comparingDouble(CampaignMetrics::ctr).reversed()
                .thenComparing(Comparator.comparingLong(CampaignMetrics::totalImpressions).reversed())
                .thenComparing(CampaignMetrics::campaignId));

        return result;
    }

    /**
     * Get top N campaigns by lowest CPA (Cost Per Acquisition).
     * Excludes campaigns with zero conversions (or null CPA).
     * Tie-breaker: total conversions (descending), then campaignId (ascending).
     */
    public List<CampaignMetrics> getTopByLowestCPA(Collection<CampaignMetrics> campaigns, int n) {
        if (campaigns == null || campaigns.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }

        // Max-Heap: largest CPA is at the head
        Comparator<CampaignMetrics> maxHeapComparator = Comparator
                .comparingDouble((CampaignMetrics c) -> c.cpa() != null ? c.cpa() : Double.MAX_VALUE)
                .reversed()
                .thenComparingLong(CampaignMetrics::totalConversions)
                .thenComparing(Comparator.comparing(CampaignMetrics::campaignId).reversed());

        PriorityQueue<CampaignMetrics> maxHeap = new PriorityQueue<>(n, maxHeapComparator);

        for (CampaignMetrics campaign : campaigns) {
            // Exclude campaigns with zero conversions or null CPA
            if (campaign.totalConversions() <= 0 || campaign.cpa() == null) {
                continue;
            }

            if (maxHeap.size() < n) {
                maxHeap.offer(campaign);
            } else if (campaign.cpa() < maxHeap.peek().cpa()) {
                maxHeap.poll();
                maxHeap.offer(campaign);
            }
        }

        // Extract and sort ascending (lowest CPA first)
        List<CampaignMetrics> result = new ArrayList<>(maxHeap);
        result.sort(Comparator
                .comparingDouble(CampaignMetrics::cpa)
                .thenComparing(Comparator.comparingLong(CampaignMetrics::totalConversions).reversed())
                .thenComparing(CampaignMetrics::campaignId));

        return result;
    }
}


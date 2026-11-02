package org.flinters.flintersexam.util;

import org.flinters.flintersexam.model.CampaignMetrics;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Utility to write campaign metrics reports to CSV files.
 */
@Component
public class CsvReportWriter {

    private static final String CSV_HEADER = "campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA";

    /**
     * Writes campaign metrics list to the target CSV file path.
     *
     * @param targetPath Path to output CSV file
     * @param metricsList List of campaigns to write
     * @throws IOException If writing fails
     */
    public void writeReport(Path targetPath, List<CampaignMetrics> metricsList) throws IOException {
        if (targetPath.getParent() != null && !Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (CampaignMetrics c : metricsList) {
                String cpaStr = c.cpa() != null ? String.format(Locale.US, "%.2f", c.cpa()) : "N/A";
                String line = String.format(
                        Locale.US,
                        "%s,%d,%d,%.2f,%d,%.4f,%s",
                        c.campaignId(),
                        c.totalImpressions(),
                        c.totalClicks(),
                        c.totalSpend(),
                        c.totalConversions(),
                        c.ctr(),
                        cpaStr
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }
}


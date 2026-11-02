package org.flinters.flintersexam.processor;

import org.flinters.flintersexam.model.AnalyticsSummary;
import org.flinters.flintersexam.model.CampaignAggregator;
import org.flinters.flintersexam.model.CampaignMetrics;
import org.flinters.flintersexam.service.TopNRanker;
import org.flinters.flintersexam.util.CsvReportWriter;
import org.flinters.flintersexam.util.FastCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main analytics processing service.
 * Coordinates streaming CSV ingestion, metric aggregation, top-N calculation, and report generation.
 */
@Service
public class AnalyticsProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsProcessor.class);

    private final FastCsvReader csvReader;
    private final TopNRanker topNRanker;
    private final CsvReportWriter reportWriter;

    @Value("${analytics.top-n:10}")
    private int topN = 10;

    @Value("${analytics.output.ctr-filename:top10_ctr.csv}")
    private String ctrFilename = "top10_ctr.csv";

    @Value("${analytics.output.cpa-filename:top10_cpa.csv}")
    private String cpaFilename = "top10_cpa.csv";

    public AnalyticsProcessor(FastCsvReader csvReader, TopNRanker topNRanker, CsvReportWriter reportWriter) {
        this.csvReader = csvReader;
        this.topNRanker = topNRanker;
        this.reportWriter = reportWriter;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public void setCtrFilename(String ctrFilename) {
        this.ctrFilename = ctrFilename;
    }

    public void setCpaFilename(String cpaFilename) {
        this.cpaFilename = cpaFilename;
    }

    /**
     * Executes the end-to-end analytics workflow.
     *
     * @param inputPath Path to the input CSV file
     * @param outputDir Path to the output directory
     * @return Execution summary with aggregated metrics
     * @throws IOException If file I/O operations fail
     */
    public AnalyticsSummary process(Path inputPath, Path outputDir) throws IOException {
        long startTime = System.currentTimeMillis();

        log.info("Starting CSV Analytics processing for: {}", inputPath);

        // 1. Streaming read and aggregate in a single pass O(N)
        FastCsvReader.StreamingResult streamResult = csvReader.readAndAggregate(inputPath);
        Map<String, CampaignAggregator> aggMap = streamResult.aggregationMap();

        log.info("Processed {} lines. Found {} unique campaigns (valid records: {}, skipped: {})",
                streamResult.totalLines(), aggMap.size(), streamResult.validRecords(), streamResult.skippedRecords());

        // 2. Convert to list of immutable CampaignMetrics and compute overall totals
        List<CampaignMetrics> allMetrics = new ArrayList<>(aggMap.size());
        long totalImpressions = 0;
        long totalClicks = 0;
        double totalSpend = 0.0;
        long totalConversions = 0;

        for (Map.Entry<String, CampaignAggregator> entry : aggMap.entrySet()) {
            CampaignAggregator agg = entry.getValue();
            CampaignMetrics metrics = agg.toMetrics(entry.getKey());
            allMetrics.add(metrics);

            totalImpressions += agg.getTotalImpressions();
            totalClicks += agg.getTotalClicks();
            totalSpend += agg.getTotalSpend();
            totalConversions += agg.getTotalConversions();
        }

        double overallCtr = totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0;
        double overallCpa = totalConversions > 0 ? totalSpend / totalConversions : 0.0;

        // 3. Rank Top N CTR and Top N CPA using bounded PriorityQueue
        List<CampaignMetrics> topCtr = topNRanker.getTopByHighestCTR(allMetrics, topN);
        List<CampaignMetrics> topCpa = topNRanker.getTopByLowestCPA(allMetrics, topN);

        // 4. Write output files
        Path ctrOutputPath = outputDir.resolve(ctrFilename);
        Path cpaOutputPath = outputDir.resolve(cpaFilename);

        reportWriter.writeReport(ctrOutputPath, topCtr);
        log.info("Top {} CTR results exported to: {}", topN, ctrOutputPath);

        reportWriter.writeReport(cpaOutputPath, topCpa);
        log.info("Top {} CPA results exported to: {}", topN, cpaOutputPath);

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Processing completed successfully in {} ms", durationMs);

        return new AnalyticsSummary(
                streamResult.totalLines(),
                streamResult.validRecords(),
                streamResult.skippedRecords(),
                aggMap.size(),
                totalImpressions,
                totalClicks,
                totalSpend,
                totalConversions,
                overallCtr,
                overallCpa,
                topCtr,
                topCpa,
                durationMs
        );
    }
}

package org.flinters.flintersexam.processor;

import org.flinters.flintersexam.model.AnalyticsSummary;
import org.flinters.flintersexam.service.TopNRanker;
import org.flinters.flintersexam.util.CsvReportWriter;
import org.flinters.flintersexam.util.FastCsvReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsProcessorTest {

    private final AnalyticsProcessor processor = new AnalyticsProcessor(
            new FastCsvReader(),
            new TopNRanker(),
            new CsvReportWriter()
    );

    @Test
    @DisplayName("Should run end-to-end processing and generate top10_ctr.csv and top10_cpa.csv")
    void testEndToEndProcessing(@TempDir Path tempDir) throws IOException {
        Path inputCsv = tempDir.resolve("input.csv");
        Path outputDir = tempDir.resolve("output");

        String csvData = """
                campaign_id,date,impressions,clicks,spend,conversions
                CMP001,2025-01-01,1000,100,50.0,10
                CMP002,2025-01-01,1000,50,50.0,5
                CMP003,2025-01-01,1000,200,100.0,20
                CMP004,2025-01-01,1000,10,20.0,0
                """;
        Files.writeString(inputCsv, csvData);

        AnalyticsSummary summary = processor.process(inputCsv, outputDir);

        assertEquals(4, summary.uniqueCampaignsCount());
        assertEquals(4000, summary.overallImpressions());
        assertEquals(360, summary.overallClicks());
        assertEquals(220.0, summary.overallSpend(), 0.001);
        assertEquals(35, summary.overallConversions());

        Path ctrFile = outputDir.resolve("top10_ctr.csv");
        Path cpaFile = outputDir.resolve("top10_cpa.csv");

        assertTrue(Files.exists(ctrFile), "top10_ctr.csv must exist");
        assertTrue(Files.exists(cpaFile), "top10_cpa.csv must exist");

        List<String> ctrLines = Files.readAllLines(ctrFile);
        assertEquals(5, ctrLines.size()); // 1 header + 4 campaigns
        assertTrue(ctrLines.get(1).startsWith("CMP003"), "CMP003 has highest CTR (0.20)");

        List<String> cpaLines = Files.readAllLines(cpaFile);
        assertEquals(4, cpaLines.size()); // 1 header + 3 campaigns (CMP004 with 0 conversions excluded)
    }
}


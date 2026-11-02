package org.flinters.flintersexam.runner;

import org.flinters.flintersexam.model.AnalyticsSummary;
import org.flinters.flintersexam.model.CampaignMetrics;
import org.flinters.flintersexam.processor.AnalyticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Command line runner for CSV Analytics processing.
 */
@Component
public class AnalyticsCommandLineRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsCommandLineRunner.class);

    private final AnalyticsProcessor processor;
    private int exitCode = 0;

    public AnalyticsCommandLineRunner(AnalyticsProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void run(String... args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage: java -jar flinters-exam.jar <input-csv-path> <output-directory>");
            System.err.println("Example: java -jar flinters-exam.jar data/input.csv output/");
            // If running without args (e.g. default spring context load during test), keep exit code 0
            return;
        }

        Path inputPath = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);

        if (!Files.exists(inputPath)) {
            System.err.println("[ERROR] Input file not found: " + inputPath.toAbsolutePath());
            this.exitCode = 1;
            return;
        }

        try {
            AnalyticsSummary summary = processor.process(inputPath, outputDir);
            printSummary(summary);
        } catch (Exception e) {
            log.error("Fatal error during processing: {}", e.getMessage(), e);
            System.err.println("[ERROR] Processing failed: " + e.getMessage());
            this.exitCode = 1;
        }
    }

    private void printSummary(AnalyticsSummary s) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("                    ANALYTICS SUMMARY                         ");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf(Locale.US, "Total records parsed : %,d (Valid: %,d, Skipped: %,d)%n",
                s.totalLinesRead(), s.validRecords(), s.skippedRecords());
        System.out.printf(Locale.US, "Total unique campaigns: %,d%n", s.uniqueCampaignsCount());
        System.out.printf(Locale.US, "Total impressions    : %,d%n", s.overallImpressions());
        System.out.printf(Locale.US, "Total clicks         : %,d%n", s.overallClicks());
        System.out.printf(Locale.US, "Total spend          : $%,.2f%n", s.overallSpend());
        System.out.printf(Locale.US, "Total conversions    : %,d%n", s.overallConversions());
        System.out.printf(Locale.US, "Overall CTR          : %.4f%n", s.overallCtr());
        System.out.printf(Locale.US, "Overall CPA          : $%.2f%n", s.overallCpa());
        System.out.println("───────────────────────────────────────────────────────────────");

        String topCtrStr = s.top10Ctr().isEmpty() ? "N/A" :
                s.top10Ctr().get(0).campaignId() + " (CTR: " + String.format(Locale.US, "%.4f", s.top10Ctr().get(0).ctr()) + ")";
        String topCpaStr = s.top10Cpa().isEmpty() ? "N/A" :
                s.top10Cpa().get(0).campaignId() + " (CPA: $" + String.format(Locale.US, "%.2f", s.top10Cpa().get(0).cpa()) + ")";

        System.out.println("Top Campaign by CTR  : " + topCtrStr);
        System.out.println("Top Campaign by CPA  : " + topCpaStr);
        System.out.printf(Locale.US, "Processing Time      : %,d ms%n", s.durationMs());
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println();
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}


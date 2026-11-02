package org.flinters.flintersexam.util;

import org.flinters.flintersexam.model.CampaignAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Ultra-fast streaming CSV reader that accumulates data in a single pass O(N)
 * without holding raw rows in memory.
 */
@Component
public class FastCsvReader {

    private static final Logger log = LoggerFactory.getLogger(FastCsvReader.class);
    private static final int BUFFER_SIZE = 64 * 1024; // 64KB buffer for optimal disk streaming
    private static final int MAX_WARNINGS_TO_LOG = 10;

    public record StreamingResult(
            Map<String, CampaignAggregator> aggregationMap,
            long totalLines,
            long validRecords,
            long skippedRecords
    ) {}

    /**
     * Reads CSV file line by line and aggregates data on the fly.
     *
     * @param csvPath Path to the CSV file
     * @return Aggregation result and processing counters
     * @throws IOException If file reading fails
     */
    public StreamingResult readAndAggregate(Path csvPath) throws IOException {
        Map<String, CampaignAggregator> aggregationMap = new HashMap<>(1024);

        long totalLines = 0;
        long validRecords = 0;
        long skippedRecords = 0;
        int loggedWarnings = 0;

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Read header line
            if (line == null) {
                return new StreamingResult(aggregationMap, 0, 0, 0);
            }
            totalLines++; // Header counted

            // Buffer for split tokens
            String[] tokens = new String[6];

            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.isBlank()) {
                    skippedRecords++;
                    continue;
                }

                int parsedCount = parseSimpleCsvLine(line, tokens);
                if (parsedCount < 6) {
                    skippedRecords++;
                    if (loggedWarnings++ < MAX_WARNINGS_TO_LOG) {
                        log.warn("Line {}: Malformed record (insufficient columns: {})", totalLines, parsedCount);
                    }
                    continue;
                }

                try {
                    String campaignId = tokens[0].trim();
                    // tokens[1] is date
                    long impressions = Long.parseLong(tokens[2].trim());
                    long clicks = Long.parseLong(tokens[3].trim());
                    double spend = Double.parseDouble(tokens[4].trim());
                    long conversions = Long.parseLong(tokens[5].trim());

                    // Validation rules
                    if (impressions < 0 || clicks < 0 || spend < 0 || conversions < 0) {
                        skippedRecords++;
                        if (loggedWarnings++ < MAX_WARNINGS_TO_LOG) {
                            log.warn("Line {}: Negative value detected", totalLines);
                        }
                        continue;
                    }

                    if (clicks > impressions) {
                        skippedRecords++;
                        if (loggedWarnings++ < MAX_WARNINGS_TO_LOG) {
                            log.warn("Line {}: Clicks ({}) > Impressions ({})", totalLines, clicks, impressions);
                        }
                        continue;
                    }

                    // Accumulate directly into Map
                    CampaignAggregator aggregator = aggregationMap.get(campaignId);
                    if (aggregator == null) {
                        aggregator = new CampaignAggregator();
                        aggregationMap.put(campaignId, aggregator);
                    }
                    aggregator.accumulate(impressions, clicks, spend, conversions);
                    validRecords++;

                } catch (NumberFormatException e) {
                    skippedRecords++;
                    if (loggedWarnings++ < MAX_WARNINGS_TO_LOG) {
                        log.warn("Line {}: Number format error: {}", totalLines, e.getMessage());
                    }
                }
            }
        }

        if (skippedRecords > MAX_WARNINGS_TO_LOG) {
            log.warn("Total malformed/skipped records: {}", skippedRecords);
        }

        return new StreamingResult(aggregationMap, totalLines, validRecords, skippedRecords);
    }

    /**
     * Fast CSV delimiter splitter without regex overhead.
     */
    private int parseSimpleCsvLine(String line, String[] tokens) {
        int tokenIndex = 0;
        int start = 0;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            if (line.charAt(i) == ',') {
                if (tokenIndex < tokens.length) {
                    tokens[tokenIndex++] = line.substring(start, i);
                }
                start = i + 1;
            }
        }

        if (tokenIndex < tokens.length) {
            tokens[tokenIndex++] = line.substring(start);
        }

        return tokenIndex;
    }
}


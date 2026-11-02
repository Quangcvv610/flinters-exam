package org.flinters.flintersexam.util;

import org.flinters.flintersexam.model.CampaignAggregator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FastCsvReaderTest {

    private final FastCsvReader reader = new FastCsvReader();

    @Test
    @DisplayName("Should aggregate multiple lines of the same campaign on the fly")
    void testReadAndAggregateValidLines(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sample.csv");
        String content = """
                campaign_id,date,impressions,clicks,spend,conversions
                CMP001,2025-01-01,1000,50,100.0,5
                CMP001,2025-01-02,2000,100,150.0,10
                CMP002,2025-01-01,3000,90,200.0,15
                """;
        Files.writeString(csvFile, content);

        FastCsvReader.StreamingResult result = reader.readAndAggregate(csvFile);

        assertEquals(4, result.totalLines());
        assertEquals(3, result.validRecords());
        assertEquals(0, result.skippedRecords());

        Map<String, CampaignAggregator> aggMap = result.aggregationMap();
        assertEquals(2, aggMap.size());

        CampaignAggregator cmp001 = aggMap.get("CMP001");
        assertNotNull(cmp001);
        assertEquals(3000, cmp001.getTotalImpressions());
        assertEquals(150, cmp001.getTotalClicks());
        assertEquals(250.0, cmp001.getTotalSpend(), 0.001);
        assertEquals(15, cmp001.getTotalConversions());
    }

    @Test
    @DisplayName("Should skip malformed, negative, or invalid click rows")
    void testReadAndAggregateWithInvalidLines(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("invalid.csv");
        String content = """
                campaign_id,date,impressions,clicks,spend,conversions
                CMP001,2025-01-01,1000,50,100.0,5
                CMP002,2025-01-01,100,200,50.0,1
                CMP003,2025-01-01,-1000,10,50.0,1
                CMP004,2025-01-01,1000,10,invalid_number,1
                CMP005,not_enough_columns
                """;
        Files.writeString(csvFile, content);

        FastCsvReader.StreamingResult result = reader.readAndAggregate(csvFile);

        assertEquals(1, result.validRecords());
        assertEquals(4, result.skippedRecords());
        assertEquals(1, result.aggregationMap().size());
        assertTrue(result.aggregationMap().containsKey("CMP001"));
    }
}


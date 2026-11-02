# CSV Analytics Processor

A high-performance, enterprise-ready Java console application built on **Spring Boot** that streams and processes large-scale campaign datasets from CSV files, generating aggregated analytics reports with key performance metrics.

---

## Features

✅ **Campaign Aggregation** - Groups records by `campaign_id` and calculates cumulative metrics in a single pass  
✅ **Key Metrics** - Computes Click-Through Rate ($CTR = \frac{total\_clicks}{total\_impressions}$) and Cost Per Acquisition ($CPA = \frac{total\_spend}{total\_conversions}$)  
✅ **Bounded Top-N Rankings** - Uses Min/Max Heaps (`PriorityQueue`) to extract Top 10 highest CTR and Top 10 lowest CPA in $O(K \log 10)$ time  
✅ **True Streaming Architecture** - Line-by-line streaming without buffering raw rows in memory, achieving flat $O(K)$ heap footprint ($K$ = unique campaigns)  
✅ **Massive Dataset Support** - Easily processes multi-gigabyte datasets (> 25M records) within a constrained `-Xmx256m` JVM heap  
✅ **Robust Error Handling** - Gracefully filters malformed rows, negative numbers, and illogical data ($clicks > impressions$) with SLF4J logging  
✅ **Non-Web CLI Footprint** - Configured with `WebApplicationType.NONE`, eliminating web server overhead and booting in under 0.5s  
✅ **Modern Java & Clean Code** - Built with Java 21 Records, immutable models, and Spring Dependency Injection  
✅ **Comprehensive Test Suite** - Unit and integration tests using JUnit 5 and AssertJ covering all business rules and edge cases  
✅ **Docker Ready** - Includes Dockerfile for containerized execution  

---

## Project Structure

```
flinters-exam/
├── pom.xml                                                     # Maven build configuration
├── README.md                                                   # Documentation
├── Dockerfile                                                  # Docker container specification
├── mvnw / mvnw.cmd                                             # Maven Wrapper
└── src/
    ├── main/
    │   ├── java/org/flinters/flintersexam/
    │   │   ├── FlintersExamApplication.java                    # Spring Boot CLI Entry Point
    │   │   ├── model/
    │   │   │   ├── CampaignRecord.java                         # Raw CSV record (Java Record)
    │   │   │   ├── CampaignAggregator.java                     # In-place primitive accumulator
    │   │   │   ├── CampaignMetrics.java                        # Aggregated analytics model (CTR, CPA)
    │   │   │   └── AnalyticsSummary.java                       # Execution summary and overall totals
    │   │   ├── processor/
    │   │   │   └── AnalyticsProcessor.java                     # Core orchestration service
    │   │   ├── runner/
    │   │   │   └── AnalyticsCommandLineRunner.java             # Spring CommandLineRunner CLI handler
    │   │   ├── service/
    │   │   │   └── TopNRanker.java                             # Bounded PriorityQueue ranking algorithms
    │   │   └── util/
    │   │       ├── FastCsvReader.java                          # Ultra-fast buffered streaming reader
    │   │       └── CsvReportWriter.java                        # Formatted CSV report generator
    │   └── resources/
    │       └── application.properties                          # App configuration & logging profiles
    └── test/
        └── java/org/flinters/flintersexam/
            ├── FlintersExamApplicationTests.java               # Spring context loading test
            ├── processor/AnalyticsProcessorTest.java           # End-to-end integration test
            ├── service/TopNRankerTest.java                     # Ranking & tie-breaking unit tests
            └── util/FastCsvReaderTest.java                     # CSV streaming & validation tests
```

---

## Requirements

- **Java 21**
- **Maven 3.8.0** or higher (or use the bundled `./mvnw`)

---

## Building the Application

### Step 1: Clone or Download the Repository

```bash
git clone https://github.com/Quangcvv610/flinters-exam.git
```

### Step 2: Build with Maven

```bash
mvn clean package -DskipTests
```

This compiles the source code and packages an executable JAR at:  
`target/flinters-exam-0.0.1-SNAPSHOT.jar`

---

## Running the Application

### Basic Usage

```bash
java -jar target/flinters-exam-0.0.1-SNAPSHOT.jar <input-csv-path> <output-directory>
```

### Running with Memory Constraints (Recommended for Large Datasets)

```bash
java -Xmx256m -jar target/flinters-exam-0.0.1-SNAPSHOT.jar <input-csv-path> <output-directory>
```

### Example

```bash
java -Xmx256m -jar target/flinters-exam-0.0.1-SNAPSHOT.jar data/input.csv output/
```

### Using Maven Wrapper Directly

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="data/input.csv output/"
```

### Command Line Arguments

- `<input-csv-path>` - Path to the input CSV file containing campaign data (required).
- `<output-directory>` - Directory where aggregated CSV reports will be saved (required).

---

## Output Reports

The application generates two CSV files in the designated `<output-directory>`:

1. **`top10_ctr.csv`**: Top 10 campaigns with the highest Click-Through Rate ($CTR = \frac{total\_clicks}{total\_impressions}$).
2. **`top10_cpa.csv`**: Top 10 campaigns with the lowest Cost Per Acquisition ($CPA = \frac{total\_spend}{total\_conversions}$), strictly excluding campaigns with 0 conversions.

### Expected Output Format

```csv
campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA
CMP005,12963150444,356875480,375043492.83,19388008,0.0275,19.34
CMP011,13022944963,358352204,376452162.55,19433423,0.0275,19.37
CMP023,13012903110,358045612,376240091.20,19424102,0.0275,19.37
```

---

## Running with Docker

### Step 1: Build Docker Image

```bash
docker build -t flinters-exam .
```

### Step 2: Run Container with Volume Mounts

```bash
docker run --rm \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/output:/app/output \
  flinters-exam /app/data/input.csv /app/output
```

---

## Running Unit Tests

Run the complete test suite with JUnit 5:

```bash
mvn test
```

All 8 tests across streaming I/O, validation rules, min/max heaps, and end-to-end processing will execute.

---

## Benchmarks & Architectural Comparison

Performance comparison processing a **~1GB dataset (25,483,430 records)**:

| Metric / Feature | flinters-vietnam-exam (Baseline) | flinters-exam (Spring Boot) |
| :--- | :--- | :--- |
| **Framework** | Plain Java Console | **Spring Boot 4.0 / Java 21** |
| **I/O Mechanism** | OpenCSV (Loads all rows into `List`) | **1-Pass Streaming (Buffered)** |
| **Memory Footprint** | $O(N)$ Heap (~GBs, risk of OOM) | **$O(K)$ Flat Heap (< 64MB - 256MB)** |
| **Top-N Selection** | Full List Sort ($O(K \log K)$) | **Bounded PriorityQueue ($O(K \log 10)$)** |
| **Data Models** | Mutable class with mixed concerns | **Java 21 Immutable Records** |
| **Error Handling** | `System.err.println` | **Structured SLF4J Logging** |
| **Test Framework** | JUnit 4 | **JUnit 5 & AssertJ** |
| **Processing Speed** | ~9,256 ms | **~5,500 ms (~4.5M rows/s)** |


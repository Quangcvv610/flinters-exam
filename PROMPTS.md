# AI Coding Assistants - Prompts Log

In compliance with challenge instructions, the raw, unedited prompts used during development are documented below.

---

## Prompt 1

```
# FLINTERS VIETNAM PERFORMANCE AGGREGATOR

### 1. Project Overview

You are a Senior Java Backend Engineer responsible for designing and implementing a command-line application named **Flinters Vietnam Performance Aggregator**.

The application processes advertising campaign performance data from a large CSV file (approximately 1GB) and generates aggregated analytics reports.

The primary objectives are:
- Correctly aggregate advertising performance data.
- Calculate campaign performance metrics (CTR and CPA).
- Identify the Top 10 campaigns based on CTR and CPA.
- Process large datasets efficiently with optimized memory usage.
- Deliver a maintainable, testable, and well-documented Java application.

The solution must strictly follow the business requirements and technical constraints defined below.

### 2. Technology Stack

Use the following technologies:

- Java 21.
- Spring Boot.
- Apache Commons CSV for CSV parsing and writing.
- Maven for dependency management and project build.
- JUnit 5 for unit testing.
- Lombok only when it provides meaningful benefits.

Spring Boot should only be used for dependency injection and application lifecycle management.

The application must run as a CLI application without starting an HTTP server.

**Restrictions:**
- No REST API.
- No database.
- No JPA or Hibernate.
- No repository layer.
- No unnecessary architectural complexity.

### 3. Input Data

The application receives a CSV file containing advertising campaign performance records.

Example:

​```csv
campaign_id,date,impressions,clicks,spend,conversions
CMP001,2025-01-01,12000,300,45.50,12
CMP002,2025-01-01,8000,120,28.00,4
CMP001,2025-01-02,15000,450,60.00,18
CMP003,2025-01-01,5000,0,15.00,0
​```

Input fields:

| Field | Data Type |
|---|---|
| campaign_id | String |
| date | YYYY-MM-DD |
| impressions | Integer |
| clicks | Integer |
| spend | Decimal |
| conversions | Integer |

Each row represents the performance of a campaign on a particular date.

### 4. Business Requirements

#### 4.1. Data Aggregation

Aggregate all valid records by `campaign_id`.

Calculate the following metrics:

- `total_impressions`: Sum of impressions.
- `total_clicks`: Sum of clicks.
- `total_spend`: Sum of spend.
- `total_conversions`: Sum of conversions.

Calculate the performance indicators after aggregation:

**Click-Through Rate (CTR)**

CTR = total_clicks / total_impressions

If total_impressions equals zero, CTR must be zero.

**Cost Per Acquisition (CPA)**

CPA = total_spend / total_conversions

If total_conversions equals zero, CPA must be null and the campaign must be excluded from the Top 10 CPA ranking.

Use appropriate numeric types to avoid unnecessary precision loss, especially when handling monetary values.

#### 4.2. Generate Output Files

Generate two CSV files.

**File 1: top10_ctr.csv**

Rank the top 10 campaigns with the highest CTR in descending order.

**File 2: top10_cpa.csv**

Rank the top 10 campaigns with the lowest CPA in ascending order.

Exclude campaigns with zero conversions.

Both files must contain the following columns:

​```csv
campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA
​```

If fewer than 10 eligible campaigns exist, output all available campaigns.

### 5. Data Processing and Memory Optimization

The input file may reach approximately 1GB. Therefore, memory efficiency is a critical requirement.

**Mandatory processing approach: Sequential Streaming Processing.**

The application must process records sequentially without loading the entire input file into memory.

Use Apache Commons CSV streaming parser and a HashMap-based aggregation strategy.

Expected processing workflow:

1. Open the input CSV file.
2. Read and validate the header.
3. Parse one record at a time.
4. Validate and convert the record into a domain object.
5. Aggregate values using `Map<String, CampaignAnalytics>`.
6. Release references to processed records when they are no longer needed.
7. Calculate CTR and CPA after completing aggregation.
8. Identify the Top 10 campaigns.
9. Write the results to the corresponding output CSV files.

Do not retain the original CSV records in memory.

**Concurrency restrictions:**

The solution must use sequential processing only.

Do not use:
- CompletableFuture.
- ExecutorService.
- ThreadPoolExecutor.
- Future or Callable.
- BlockingQueue.
- parallelStream().
- ForkJoinPool.
- Any multithreading or asynchronous processing mechanism.

The implementation must prioritize predictable memory usage and straightforward data consistency.

### 6. Top 10 Ranking Strategy

Analyze and compare two approaches:

**Approach A: Full Sorting**
- Sort all aggregated campaigns.
- Select the first 10 elements.

**Approach B: PriorityQueue**
- Maintain a bounded priority queue containing at most 10 campaigns.
- Update the queue while iterating through aggregated campaign results.

Compare their:
- Time complexity.
- Space complexity.
- Implementation complexity.
- Performance implications.

Select the most appropriate approach and explain the technical reasoning.

### 7. Error Handling and Data Validation

The application must handle the following scenarios:

- Input file does not exist.
- Invalid input or output path.
- Output directory does not exist.
- Empty CSV file.
- Missing or invalid headers.
- Missing fields.
- Invalid numeric values.
- Invalid date format.
- Empty campaign ID.
- Negative or otherwise invalid values, according to the applicable business rules.
- Zero impressions.
- Zero conversions.
- Duplicate campaign records.
- Invalid CSV records.
- No valid campaigns.
- Fewer than 10 eligible campaigns.

Clearly distinguish between:
- Fatal errors that must terminate execution.
- Invalid records that can be skipped.
- Conditions that should generate warnings or logs.

Do not silently ignore errors or invent business rules that are not specified in the requirements. Identify any ambiguous validation rules before implementation.

### 8. Project Structure

Use the following package structure:

​```text
org.system.flintersvietnamperformanceaggregator/
│
├── FlintersVietnamPerformanceAggregatorApplication.java
│
├── controller/
│   └── AnalyticsController.java
│
├── domain/
│   └── model/
│       ├── CampaignRecord.java
│       └── CampaignAnalytics.java
│
├── service/
│   ├── AnalyticsService.java
│   └── impl/
│       └── AnalyticsServiceImpl.java
│
└── utils/
    ├── CsvReaderUtil.java
    └── CsvWriterUtil.java
​```

Each component must have a clearly defined responsibility.

Avoid introducing additional packages or unnecessary abstraction layers unless there is a strong technical justification.

### 9. Testing Requirements

Use JUnit 5 to implement unit tests.

At minimum, cover:

1. Aggregating multiple records belonging to the same campaign.
2. Correct CTR calculation.
3. Correct CPA calculation.
4. CTR when impressions equal zero.
5. CPA when conversions equal zero.
6. Top 10 CTR ranking.
7. Top 10 CPA ranking.
8. Excluding zero-conversion campaigns from CPA ranking.
9. Empty CSV input.
10. Invalid CSV records.
11. Missing input file.
12. Correct output CSV headers and values.

Tests must verify both business correctness and important edge cases.

### 10. Performance Analysis

Provide a technical analysis of the implementation, including:

- Time complexity of CSV processing.
- Space complexity of aggregation.
- HashMap memory overhead.
- CSV parsing overhead.
- Sorting versus PriorityQueue.
- Impact of the number of unique campaigns on memory consumption.
- JVM heap considerations.
- Potential bottlenecks when processing a 1GB CSV file.
- Limitations of sequential processing.

Do not claim that the application can process 1GB within a specific time or memory limit without actual benchmark results.

If possible, perform benchmarking with representative datasets and document:
- Input file size.
- Number of records.
- Number of unique campaigns.
- Execution time.
- Peak memory usage.
- JVM configuration.

### 11. CLI Execution

The application must support execution through command-line arguments.

For example:

​```bash
java -jar flinters-exam.jar input.csv output/
​```

Expected output:

​```text
output/
├── top10_ctr.csv
└── top10_cpa.csv
​```

Validate command-line arguments and provide clear error messages when execution fails.

### 12. Documentation

Provide a README.md containing:

- Project overview.
- Technology stack.
- Environment requirements.
- Build instructions.
- CLI execution instructions.
- Input and output formats.
- Architecture and processing workflow.
- Memory optimization strategy.
- Time and space complexity.
- Testing instructions.
- Benchmark results, if available.

If AI tools are used during implementation, maintain a PROMPTS.md file containing the actual prompts used.

### 13. Implementation Workflow

Implement the application incrementally through the following phases:

**Phase 1 – Requirements Analysis**
- Review the complete business requirements.
- Identify functional and non-functional requirements.
- Identify constraints and edge cases.
- Highlight ambiguities requiring clarification.

**Phase 2 – Architecture Design**
- Propose the package structure.
- Define component responsibilities.
- Design the processing workflow.
- Analyze streaming and aggregation strategies.
- Evaluate Top 10 selection approaches.
- Estimate memory and time complexity.

**Phase 3 – Domain Model**
- Implement CampaignRecord.
- Implement CampaignAnalytics.
- Define aggregation and metric calculation responsibilities.

**Phase 4 – CSV Processing**
- Implement CSV streaming reader.
- Implement CSV writer.
- Define parsing and validation strategies.

**Phase 5 – Business Logic**
- Implement AnalyticsService.
- Implement aggregation.
- Calculate CTR and CPA.
- Implement Top 10 ranking.

**Phase 6 – CLI Integration**
- Implement AnalyticsController.
- Integrate command-line arguments.
- Configure Spring Boot to run in non-web mode.

**Phase 7 – Testing**
- Implement unit tests.
- Validate business logic.
- Test edge cases and error handling.

**Phase 8 – Performance Review**
- Evaluate memory consumption.
- Analyze execution time.
- Identify potential bottlenecks.
- Optimize without violating the requirements.

**Phase 9 – Final Review**
- Review the complete source code.
- Verify compliance with business requirements.
- Review architecture and code quality.
- Complete project documentation.

### 14. Expected Engineering Standards

The implementation should demonstrate the following engineering qualities:

- Clean and maintainable Java code.
- Clear separation of responsibilities.
- Appropriate object-oriented design.
- Efficient memory management.
- Correct numerical calculations.
- Robust error handling.
- Meaningful unit test coverage.
- Appropriate use of Java 21 features.
- No unnecessary abstractions or dependencies.
- Technical decisions supported by clear reasoning.

### 15. Collaboration and Review Process

Work incrementally rather than generating the entire application at once.

For each phase:

1. Explain the objective and technical approach.
2. Describe important design decisions and trade-offs.
3. Present the implementation for the current phase only.
4. Explain important classes and methods.
5. Discuss performance and memory implications.
6. Identify potential production issues.
7. Wait for approval before proceeding to the next phase.

**Important:** Start with Phase 1 and Phase 2 only.

Do not generate source code during these initial phases.

Present the requirements analysis and architecture proposal for review. Wait for explicit approval before implementing Phase 3.

The final solution must satisfy the stated business requirements and technical constraints without introducing unnecessary features or changing the scope of the assignment.
```
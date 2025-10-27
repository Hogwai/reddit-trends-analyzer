# Reddit Analytics with Micronaut & DynamoDB

A lightweight analytics tool to fetch posts from a subreddit, extract keywords, and identify trends.

Built with Micronaut and a local DynamoDB database.

## Tech Stack

- **Framework:** Micronaut
- **Language:** Java 21
- **Database:** AWS DynamoDB (Local)
- **Build Tool:** Gradle

## Prerequisites

- Java 21
- Docker & Docker Compose
- AWS CLI

## Quick Start

### Start DynamoDB

```bash
docker-compose up -d
```

### Configure a local profile

```shell
aws configure --profile local
```

### Create credentials

```shell
AWS Access Key ID : DUMMYKEY
AWS Secret Access Key : DUMMYSECRET
Default region name: eu-west-3
Default output format: json
```

### Create the table
```shell
aws dynamodb create-table \
    --table-name reddit-posts \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --endpoint-url http://localhost:8000 \
	--region eu-west-3 \
	--profile local
```

Or run the shell:

```shell
create-table.sh
```

### Run the application:

```bash
./gradlew run
```

The app will be available at `http://localhost:8080`.

## API Endpoints

- Fetch Posts

Fetches and saves posts from Reddit.

```bash
curl -X POST "http://localhost:8080/reddit/fetch?subreddit=socialmedia&limit=50"
```

- Get Top Keywords

Returns the most frequent keywords for a given subreddit.

```bash
curl "http://localhost:8080/analytics/top-keywords?subreddit=socialmedia&days=30"
```

- Compare Keywords

Compares the frequency of specific keywords.

```bash
curl "http://localhost:8080/analytics/compare-keywords?subreddit=socialmedia&terms=tiktok,instagram"
```

- Get top flairs

Returns the most frequent flairs used for a given subreddit.

```bash
curl "http://localhost:8080/analytics/top-flairs?subreddit=socialmedia"
```

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

### Configuring a local profile

```shell
aws configure --profile local
```

### Creating credentials

```shell
AWS Access Key ID : DUMMYKEY
AWS Secret Access Key : DUMMYSECRET
Default region name: eu-west-3
Default output format: json
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

- Compare Terms

Compares the frequency of specific terms.

```bash
curl "http://localhost:8080/analytics/compare?subreddit=socialmedia&terms=tiktok,instagram"
```

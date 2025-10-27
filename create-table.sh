#!/usr/bin/env bash

aws dynamodb create-table \
    --table-name reddit-posts \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --endpoint-url http://localhost:8000 \
	--region eu-west-3 \
	--profile local
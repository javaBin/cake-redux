#/bin/bash

GREEN='\033[0;32m'
NC='\033[0m' # No Color




REGION="eu-central-1"

echo -e "${GREEN}Fetching your active buckets in region $REGION...${NC}"
aws s3 ls --profile javabin | grep cake
echo ""
echo -e "${GREEN}Type bucket name where you want to deploy frontend:${NC}"
read BUCKET_NAME
echo -e "${GREEN}Deploying frontend to $BUCKET_NAME in $REGION${NC}"

npm run build

aws s3 sync ./dist/ "s3://$BUCKET_NAME" --region="$REGION" --profile javabin --delete

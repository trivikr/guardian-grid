#!/usr/bin/env bash
set -e -x

ID=$2
BUCKET=$1

echo "Processing $ID in $BUCKET"

grid image:download -d=. $ID
grid image:get --hydrate $ID > $ID.json
sips --setProperty format png $ID --out $ID
exiftool -all= $ID
yarn update $ID
# yarn s3 $ID $BUCKET

echo "DONE"

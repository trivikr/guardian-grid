import getCredentials from "./getCredentials";
import fetch from "node-fetch";
import AWS from "aws-sdk";

interface MediaAPICredentials {
  baseUrl: string;
  "X-Gu-Media-Key": string;
}

const getImageCount = async (
  credentials: MediaAPICredentials
): Promise<number> => {
  const response = await fetch(credentials.baseUrl + "/images", {
    headers: {
      "X-Gu-Media-Key": credentials["X-Gu-Media-Key"]
    }
  });
  const images: { total: number } = await response.json();
  return images.total;
};

const metric = (value: number) => ({
  MetricData: [
    {
      MetricName: "ImageCount",
      Unit: "Count",
      Value: value
    }
  ],
  Namespace: `${process.env.STAGE}/MediaApi`
});

const handler = async (): Promise<{ statusCode: number; body: string }> => {
  try {
    // get credentials
    const credentials = await getCredentials();

    // query media api with credentials
    const images = await getImageCount(credentials);

    // post it to CW as metric
    const cloudwatch = new AWS.CloudWatch({ region: "eu-west-1" });
    await cloudwatch
      .putMetricData(metric(images))
      .promise()

    // return happy lambda response to caller
    return { statusCode: 200, body: "Metric sent" };
  } catch (err) {
    console.error(err);
    return { statusCode: 500, body: `Error sending metric: ${err.message}` };
  }
};

const fns = { getCredentials, handler, getImageCount };

export default fns;
import type { NextConfig } from "next";

const isSageMaker = process.env.SAGEMAKER === "1";
const basePath = isSageMaker ? "/codeeditor/default/absports/3000" : "";

const nextConfig: NextConfig = {
  basePath,
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:8080/api/:path*",
      },
    ];
  },
};

export default nextConfig;

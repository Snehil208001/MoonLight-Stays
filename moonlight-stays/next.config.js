/** @type {import('next').NextConfig} */
const apiUrl = process.env.NEXT_PUBLIC_API_URL;
const backendOrigin = apiUrl && /^https?:\/\//.test(apiUrl)
  ? new URL(apiUrl).origin
  : 'http://localhost:8080';

const nextConfig = {
  output: "standalone",
  images: {
    remotePatterns: [
      { protocol: 'http', hostname: 'localhost', pathname: '/**' },
      { protocol: 'http', hostname: 'moonlight-stays.ap-south-1.elasticbeanstalk.com', pathname: '/**' },
      { protocol: 'https', hostname: 'moonlight-stays.ap-south-1.elasticbeanstalk.com', pathname: '/**' },
    ],
  },
  async rewrites() {
    return [
      { source: '/api/v1/:path*', destination: `${backendOrigin}/api/v1/:path*` },
      { source: '/images/:path*', destination: `${backendOrigin}/images/:path*` },
    ];
  },
};

module.exports = nextConfig;

import http from "node:http";

const PROXY_PORT = 3000;
const NEXT_PORT = 3001;
const PREFIX = "/codeeditor/default";

const server = http.createServer((req, res) => {
  const url = PREFIX + req.url;
  const options = {
    hostname: "127.0.0.1",
    port: NEXT_PORT,
    path: url,
    method: req.method,
    headers: req.headers,
  };

  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res, { end: true });
  });

  proxyReq.on("error", (err) => {
    console.error("Proxy error:", err.message);
    res.writeHead(502);
    res.end("Bad Gateway");
  });

  req.pipe(proxyReq, { end: true });
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`SageMaker proxy listening on :${PROXY_PORT} -> next :${NEXT_PORT}`);
});

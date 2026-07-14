import http from "node:http";
import httpProxy from "http-proxy";

const LISTEN_PORT = 3000;
const NEXT_PORT = 3001;
const PREFIX = "/codeeditor/default";

const proxy = httpProxy.createProxyServer({
  target: `http://127.0.0.1:${NEXT_PORT}`,
  ws: true,
});

proxy.on("error", (err, _req, res) => {
  console.error("[proxy] error:", err.message);
  if (res.writeHead) {
    res.writeHead(502, { "Content-Type": "text/plain" });
    res.end("Bad Gateway");
  }
});

const server = http.createServer((req, res) => {
  req.url = PREFIX + req.url;
  proxy.web(req, res);
});

server.on("upgrade", (req, socket, head) => {
  req.url = PREFIX + req.url;
  proxy.ws(req, socket, head);
});

server.listen(LISTEN_PORT, "0.0.0.0", () => {
  console.log(`[sagemaker-proxy] :${LISTEN_PORT} -> :${NEXT_PORT} (prefix ${PREFIX})`);
});

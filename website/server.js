const fs = require("fs");
const https = require("https");
const WebSocket = require("ws");
const { execSync } = require("child_process");
const path = require("path");

const PORT = 8080;

async function setupSSL() {
  try {
    console.log("Checking for certbot...");
    try {
      execSync("which certbot");
      console.log("Certbot is already installed");
    } catch (error) {
      console.log("Installing certbot...");
      execSync("sudo apt-get update");
      execSync("sudo apt-get install -y certbot");
    }

    const domain = "https://neo-room.vercel.app/";

    const certPath = `/etc/letsencrypt/live/${domain}/fullchain.pem`;
    const keyPath = `/etc/letsencrypt/live/${domain}/privkey.pem`;

    let certsExist = false;
    try {
      fs.accessSync(certPath);
      fs.accessSync(keyPath);
      certsExist = true;
      console.log("SSL certificates already exist");
    } catch (error) {
      console.log("SSL certificates not found, obtaining new ones...");
    }

    if (!certsExist) {
      console.log(`Obtaining Let's Encrypt certificate for ${domain}...`);
      execSync(
        `sudo certbot certonly --standalone --agree-tos --non-interactive --email your-email@example.com -d ${domain}`
      );
      console.log("Certificate obtained successfully");
    }

    return {
      cert: fs.readFileSync(certPath),
      key: fs.readFileSync(keyPath),
    };
  } catch (error) {
    console.error("Error setting up SSL:", error);
    console.log("Using self-signed certificate as fallback...");

    const certDir = path.join(__dirname, "certs");
    if (!fs.existsSync(certDir)) {
      fs.mkdirSync(certDir);
    }

    const keyPath = path.join(certDir, "key.pem");
    const certPath = path.join(certDir, "cert.pem");

    if (!fs.existsSync(keyPath) || !fs.existsSync(certPath)) {
      console.log("Generating self-signed certificate...");
      execSync(
        `openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost" -keyout ${keyPath} -out ${certPath}`
      );
    }

    return {
      cert: fs.readFileSync(certPath),
      key: fs.readFileSync(keyPath),
    };
  }
}

const http = require("http");
const httpServer = http.createServer((req, res) => {
  const domain = req.headers.host;
  res.writeHead(301, { Location: `https://${domain}${req.url}` });
  res.end();
});
httpServer.listen(80, () =>
  console.log("HTTP redirect server running on port 80")
);

async function startServer() {
  try {
    const sslOptions = await setupSSL();

    const server = https.createServer(sslOptions);

    const wss = new WebSocket.Server({ server });

    console.log(`Secure WebSocket server starting on port ${PORT}`);

    const clients = new Map();

    wss.on("connection", (ws) => {
      let username = null;

      console.log("New connection established");

      ws.on("message", (message) => {
        try {
          const data = JSON.parse(message);

          if (data.type === "connect") {
            username = data.username;
            clients.set(ws, username);

            broadcastSystemMessage(`${username} has joined the chat`);
            console.log(`User connected: ${username}`);

            broadcastUserCount();
            return;
          }

          if (data.type === "message" && username) {
            broadcast({
              type: "message",
              username: username,
              content: data.content,
            });

            console.log(`Message from ${username}: ${data.content}`);
          }
        } catch (e) {
          if (!username) {
            username = message.toString();
            clients.set(ws, username);

            broadcastSystemMessage(`${username} has joined the chat`);
            console.log(`User connected: ${username}`);

            broadcastUserCount();
          } else {
            broadcast({
              type: "message",
              username: username,
              content: message.toString(),
            });

            console.log(`Message from ${username}: ${message.toString()}`);
          }
        }
      });

      ws.on("close", () => {
        if (username) {
          console.log(`User disconnected: ${username}`);
          clients.delete(ws);

          broadcastSystemMessage(`${username} has left the chat`);

          broadcastUserCount();
        }
      });

      ws.on("error", (error) => {
        console.error(`WebSocket error:`, error);
        clients.delete(ws);
        broadcastUserCount();
      });
    });

    function broadcast(data) {
      const message = JSON.stringify(data);

      wss.clients.forEach((client) => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(message);
        }
      });
    }

    function broadcastSystemMessage(content) {
      broadcast({
        type: "system",
        content: content,
      });
    }

    function broadcastUserCount() {
      const count = clients.size;
      broadcast({
        type: "userCount",
        count: count,
        content: `Online users: ${count}`,
      });
    }

    const express = require("express");
    const app = express();

    app.use(express.static("public"));

    server.on("request", app);

    process.on("SIGINT", () => {
      console.log("Shutting down server...");

      wss.clients.forEach((client) => {
        client.close();
      });

      server.close(() => {
        console.log("Server closed");
        process.exit(0);
      });
    });

    server.listen(PORT, () => {
      const domain = process.env.DOMAIN || "your-domain.com";
      console.log(
        `Secure WebSocket server is running on wss://${domain}:${PORT}`
      );
      console.log(
        `Static files are being served from https://${domain}:${PORT}`
      );
    });
  } catch (error) {
    console.error("Failed to start server:", error);
    process.exit(1);
  }
}

startServer();

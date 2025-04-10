const fs = require("fs");
const https = require("https");
const WebSocket = require("ws");

// SSL certificate configuration
// NOTE: You'll need to replace these with your actual certificate paths
const SSL_OPTIONS = {
  key: fs.readFileSync("/path/to/private-key.pem"),
  cert: fs.readFileSync("/path/to/certificate.pem"),
  // If you have a CA certificate chain:
  // ca: fs.readFileSync('/path/to/ca-certificate.pem')
};

const PORT = 8080;
const server = https.createServer(SSL_OPTIONS);

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

  // Handle errors
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
  console.log(
    `Secure WebSocket server is running on wss://your-domain:${PORT}`
  );
});

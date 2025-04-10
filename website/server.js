const WebSocket = require('ws');

const PORT = 8080;
const server = new WebSocket.Server({ port: PORT });

console.log(`WebSocket server started on port ${PORT}`);

const clients = new Map(); 

server.on('connection', (ws) => {
  let username = null;

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      
      if (data.type === 'connect') {
        username = data.username;
        clients.set(ws, username);
        
        broadcastSystemMessage(`${username} has joined the chat`);
        console.log(`User connected: ${username}`);
        
        broadcastUserCount();
        return;
      }
      
      if (data.type === 'message' && username) {
        broadcast({
          type: 'message',
          username: username,
          content: data.content
        });
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
          type: 'message',
          username: username,
          content: message.toString()
        });
      }
    }
  });

  ws.on('close', () => {
    if (username) {
      console.log(`User disconnected: ${username}`);
      clients.delete(ws);
      
      broadcastSystemMessage(`${username} has left the chat`);
      
      broadcastUserCount();
    }
  });

  ws.on('error', (error) => {
    console.error(`WebSocket error:`, error);
    clients.delete(ws);
    broadcastUserCount();
  });
});

function broadcast(data) {
  const message = JSON.stringify(data);
  
  server.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

function broadcastSystemMessage(content) {
  broadcast({
    type: 'system',
    content: content
  });
}

function broadcastUserCount() {
  broadcast({
    type: 'system',
    content: `Online users: ${clients.size}`
  });
}

process.on('SIGINT', () => {
  console.log('Shutting down server...');
  
  server.clients.forEach((client) => {
    client.close();
  });
  
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});
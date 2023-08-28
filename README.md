## Megaspell Server Registry ##

Simple service that game uses to discover online servers.

### Key features ###

- Persistent and supports instancing.
- Servers should regularly send keepalive requests to avoid being removed from online registry.
- Host-based trust - host can only register its own servers.  
  **Important:** registry is validating host by using X-Real-IP header. Load balancer should be configured
  to set this header.
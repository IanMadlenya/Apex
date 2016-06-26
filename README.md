# Apex
Apex is a TCP reverse proxy server with load balancing capabilities.

Apex is mainly build to act as a load balancer and to route traffic to the desired backend server with the capability to select between multiple balancing algorithms. Through the simplicity of the config file you can set it up really quick and it just works.

# Features

- built on top of [netty](https://github.com/netty/netty)
- high performance
- load balancing
- multiple strategies (round robin, random, least connections)
- health check (ping probe, interval configurable in ms)
- offline/online server management (removing and adding back to the LB)
- configurable threads (recommended value is cpu cores * 2)
- simple but powerful

# Apex config

Very simple but neat config format based on my project [Cope](https://jackwhite20.github.io/Cope/).

Available balance strategies are:

| Strategy  | Description |
| --------- | ----------- |
| ROUND_ROBIN | Typical round robin algorithm |
| RANDOM | Random based selection |
| LEAST_CON | The one with the least amount of connections |

```ini
# The first timeout value is read timeout
# and the second one is write timeout
# Both are in seconds
general:
    server 0.0.0.0 80
    backlog 100
    threads 4
    timeout 300 300
    balance ROUND_ROBIN
    probe 5000

# Here are all your backend servers
backend:
    api-01 172.16.0.10 8080
    api-02 172.16.0.11 8080
    api-03 172.16.0.12 8080
    api-04 172.16.0.13 8080
```

### License

Licensed under the GNU General Public License, Version 3.0.

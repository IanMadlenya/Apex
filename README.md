# Apex
Apex is a TCP reverse proxy server with load balancing capabilities.

Apex is mainly build to act as a load balancer and to route traffic to the desired backend server with the capability to select between multiple balancing algorithms. Through the simplicity of the config file you can set it up really quick and it just works.

A cool feature is that you can add or remove backend server through a very simple RESTful API and that these servers are automatically removed or added live to the load balancer
and are directly accessible and usable.

# Features

- built on top of [netty](https://github.com/netty/netty)
- high performance
- load balancing
- multiple strategies (round robin, random, least connections, fastest)
- dynamic server adding/removing/listing (simple RESTful API)
- health check (ping probe, interval configurable in ms)
- offline/online server management (removing and adding back to the LB)
- configurable boss threads (1 should be fine for most normal use cases)
- configurable worker threads (recommended value is cpu cores * 2)
- logging (debug logging configurable)
- simple but powerful

# Installation

First of all make sure you have Java 8 installed.

Download the latest Apex version from the [release page](https://github.com/JackWhite20/Apex/releases) and start it like this:

```
java -jar apex-1.3.0.jar
```

Stop it by typing ```end``` followed by an enter press. Configure the config.cope file in the same directory to fit your needs and restart Apex.

# Apex config

Very simple but neat config format based on my project [Cope](https://jackwhite20.github.io/Cope/).

Available balance strategies are:

| Strategy  | Description |
| --------- | ----------- |
| ROUND_ROBIN | Typical round robin algorithm |
| RANDOM | Random based selection |
| LEAST_CON | The one with the least amount of connections |
| FASTEST | The one with the fastest connection time |

```ini
# The first timeout value is read timeout
# and the second one is write timeout
# Both are in seconds
#
# The boss value is the amount of threads to accept connections
# The worker value is the amount of threads to handle events
#
# If stats is true Apex will collect traffic stats that are 
# accessible through the RESTful API
general:
    debug true
    server 0.0.0.0 80
    backlog 100
    boss 1
    worker 4
    timeout 30 30
    balance ROUND_ROBIN
    probe 5000
    stats true

# How the RESTful API should be accessible
# It is recommended to not bind this to 0.0.0.0
# or to a specific external interface
rest:
    server localhost 6000

# Here are all your backend servers
backend:
    api-01 172.16.0.10 8080
    api-02 172.16.0.11 8080
    api-03 172.16.0.12 8080
    api-04 172.16.0.13 8080
```

# Apex RESTful API

The API consists of three simple GET paths with path variables.

| Path | Example | Description |
| --------- | ----------- | ----------- |
| /apex/add/{name}/{ip}/{port} | /apex/add/web-01/172.16.0.50/80 | Adds the given backend server to the load balancer |
| /apex/remove/{name} | /apex/remove/web-01 | Removes the given backend server from the load balancer |
| /apex/list | /apex/list | Lists the current backend servers which are in the load balancer |
| /apex/stats | /apex/stats | Live traffic stats from Apex |

_Responses:_

| Path | Success | Error |
| --------- | ----------- | ----------- |
| /apex/add/{name}/{ip}/{port} | ```{"status":"OK","message":"Successfully added server"}``` | ```{"status":"SERVER_ALREADY_ADDED","message":"Server was already added"}``` |
| /apex/remove/{name} | ```{"status":"OK","message":"Successfully removed server"}``` | ```{"status":"SERVER_NOT_FOUND","message":"Server not found"}``` |
| /apex/list | ```{"backendInfo":[{"name":"api-01","host":"172.16.0.10","port":8080,"connectTime":125.0}],"status":"OK","message":"List received"}``` | ```{"status":"ERROR","message":"Unable to get the balancing strategy"}``` |
| /apex/stats | ```{"connections":360,"onlineBackendServers":3,"currentReadBytes":500,"currentWrittenBytes":25356,"lastReadThroughput":36,"lastWriteThroughput":39864,"totalReadBytes":929,"totalWrittenBytes":705887}``` | ```{"connections":-1,"onlineBackendServers":-1,"currentReadBytes":-1,"currentWriteBytes":-1,"lastReadThroughput":-1,"lastWriteThroughput":-1,"totalReadBytes":-1,"totalWrittenBytes":-1,"status":"ERROR","message":"Stats are disabled"}``` |

### License

Licensed under the GNU General Public License, Version 3.0.

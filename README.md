
# Talker Pattern with ICE - Client-Server Concurrent Requests

## Overview

This project implements a **Talker pattern** using **ICE (Internet Communications Engine)** in a client-server environment to manage concurrent requests. The goal was to evaluate the server's performance under varying client loads, focusing on latency, responsiveness, and concurrency handling.

## Key Features

- **Concurrent Request Handling:** The server successfully handled over 500 concurrent clients, showing a gradual increase in latency but no failure in concurrency management.
- **OutOfMemoryError:** Large Fibonacci calculations triggered an "OutOfMemoryError" due to memory limitations on the server.
- **Real Concurrency:** Real-time concurrency was demonstrated by monitoring CPU core usage and observing how threads were distributed across the system's physical resources.

## Environment

- **Server:** xhgrid6
- **Clients:** xhgrid7, xhgrid8, xhgrid9
- **Java Version:** 18

## Performance Insights

- **Latency:** The server’s response time increased as more clients were added, but it continued to function without concurrency issues.
- **Memory Limitations:** The "OutOfMemoryError" was observed during Fibonacci calculations with very large numbers, highlighting the memory constraints.
- **Concurrency Monitoring:** The distribution of threads across multiple CPU cores confirmed the real concurrency of the system under load.

This project was built to test the limits of concurrency and memory in a server-client architecture using ICE.

---

This version is a bit more formal and structured for a GitHub README. Let me know if you'd like further changes!

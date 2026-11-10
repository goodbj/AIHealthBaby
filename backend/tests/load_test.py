"""颐康宝 M4 压测脚本：/api/health 与 /api/chat 双端点，目标 ≥100 QPS。

用 asyncio + httpx 并发压测，统计 RPS / P50 / P95 / P99 延迟 / 错误率。
输出 JSON 供压测报告落库使用。
"""
from __future__ import annotations

import asyncio
import json
import statistics
import sys
import time

import httpx

BASE = "http://127.0.0.1:8000"
CONCURRENCY = 100      # 并发连接数
DURATION_S = 15        # 每端点压测时长（秒）


async def _worker(client: httpx.AsyncClient, method: str, path: str,
                  payload: dict | None, deadline: float, latencies: list,
                  errors: list) -> None:
    """单并发 worker：到 deadline 前循环发请求，记录延迟/错误。"""
    while time.perf_counter() < deadline:
        t0 = time.perf_counter()
        try:
            if method == "GET":
                r = await client.get(path)
            else:
                r = await client.post(path, json=payload)
            dt = time.perf_counter() - t0
            if r.status_code == 200:
                latencies.append(dt * 1000)  # ms
            else:
                errors.append((r.status_code, path))
        except Exception as exc:  # noqa: BLE001 — 压测统计需要兜住网络层异常
            errors.append((str(type(exc).__name__), path))


async def bench(path: str, method: str = "GET", payload: dict | None = None) -> dict:
    """对一个端点压测 DURATION_S 秒，返回统计摘要。"""
    limits = httpx.Limits(max_connections=CONCURRENCY, max_keepalive_connections=CONCURRENCY)
    async with httpx.AsyncClient(base_url=BASE, limits=limits, timeout=10.0) as client:
        latencies: list[float] = []
        errors: list = []
        deadline = time.perf_counter() + DURATION_S
        tasks = [
            asyncio.create_task(
                _worker(client, method, path, payload, deadline, latencies, errors)
            )
            for _ in range(CONCURRENCY)
        ]
        await asyncio.gather(*tasks)

    latencies.sort()
    n = len(latencies)
    total = n + len(errors)
    elapsed = DURATION_S

    def pct(p: float) -> float:
        return latencies[min(int(n * p), n - 1)] if n else 0.0

    return {
        "endpoint": path,
        "method": method,
        "total_requests": total,
        "ok_requests": n,
        "errors": len(errors),
        "error_rate_pct": round(len(errors) / total * 100, 3) if total else 0.0,
        "rps": round(n / elapsed, 1),
        "latency_ms": {
            "avg": round(statistics.fmean(latencies), 2) if n else 0.0,
            "p50": round(pct(0.50), 2),
            "p95": round(pct(0.95), 2),
            "p99": round(pct(0.99), 2),
            "max": round(latencies[-1], 2) if n else 0.0,
        },
    }


async def main() -> None:
    # 预热
    async with httpx.AsyncClient(base_url=BASE) as c:
        await c.get("/api/health")

    results = []
    results.append(await bench("/api/health", "GET"))
    results.append(await bench("/api/chat", "POST", {"prompt": "压测探测：请给出一条健康建议"}))

    # 验证压测期间响应正确性抽样
    async with httpx.AsyncClient(base_url=BASE) as c:
        h = await c.get("/api/health")
        ch = await c.post("/api/chat", json={"prompt": "抽样验证"})
        correctness = {
            "health_ok": h.status_code == 200 and h.json()["code"] == 0,
            "chat_ok": ch.status_code == 200 and ch.json()["code"] == 0,
        }

    out = {
        "config": {"base_url": BASE, "concurrency": CONCURRENCY, "duration_s": DURATION_S},
        "correctness_sample": correctness,
        "results": results,
        "meets_100qps_target": all(r["rps"] >= 100 for r in results),
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
    }
    print(json.dumps(out, ensure_ascii=False, indent=2))
    sys.exit(0 if correctness["health_ok"] and correctness["chat_ok"] else 1)


if __name__ == "__main__":
    asyncio.run(main())

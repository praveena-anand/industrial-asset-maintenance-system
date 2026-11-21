"""
Sensor gateway simulator for the Industrial Asset & Maintenance Management API.

Logs in with a SENSOR account and posts a reading every few seconds.

Modes:
  normal       - healthy machine, readings vary slightly around normal values
  overheating  - engine temperature climbs steadily
  wear         - vibration rises steadily (e.g. a worn bearing)

Usage:
  python simulator/sensor_simulator.py --equipment-id 1 --mode normal
Credentials come from the SENSOR_EMAIL and SENSOR_PASSWORD environment variables.
"""

import argparse
import json
import os
import random
import sys
import time
import urllib.error
import urllib.request


def http_post(url, payload, token=None):
    """Send a JSON POST request and return (status_code, response_body)."""
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, data=json.dumps(payload).encode("utf-8"),
                                     headers=headers, method="POST")
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return response.status, response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode("utf-8")


def login(base_url, email, password):
    status, body = http_post(f"{base_url}/api/auth/login", {"email": email, "password": password})
    if status != 200:
        sys.exit(f"Login failed ({status}): {body}")
    return json.loads(body)["accessToken"]


def next_reading(mode, tick, hours):
    """Build one reading. Values vary randomly around a base that depends on the mode."""
    base_temperature = 80.0
    base_vibration = 3.0

    if mode == "overheating":
        base_temperature = min(125.0, 80.0 + tick * 1.5)    # +1.5 C per reading, capped
    elif mode == "wear":
        base_vibration = min(15.0, 3.0 + tick * 0.25)       # +0.25 mm/s per reading, capped

    return {
        "temperatureCelsius": round(random.gauss(base_temperature, 1.0), 1),
        "vibrationMmPerSec": round(max(0.0, random.gauss(base_vibration, 0.3)), 2),
        "oilPressureBar": round(max(0.0, random.gauss(4.2, 0.1)), 2),
        "operatingHours": round(hours, 1),
    }


def main():
    parser = argparse.ArgumentParser(description="Simulate a machine's sensor gateway.")
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--equipment-id", type=int, default=1)
    parser.add_argument("--mode", choices=["normal", "overheating", "wear"], default="normal")
    parser.add_argument("--interval", type=float, default=5.0, help="seconds between readings")
    parser.add_argument("--start-hours", type=float, default=1250.0,
                        help="operating hours for the first reading (must not be below the last one)")
    parser.add_argument("--hours-per-reading", type=float, default=0.5,
                        help="simulated running hours added per reading (speeds up time)")
    args = parser.parse_args()

    email = os.environ.get("SENSOR_EMAIL")
    password = os.environ.get("SENSOR_PASSWORD")
    if not email or not password:
        sys.exit("Please set the SENSOR_EMAIL and SENSOR_PASSWORD environment variables.")

    readings_url = f"{args.base_url}/api/equipment/{args.equipment_id}/readings"
    token = login(args.base_url, email, password)
    print(f"Logged in. Sending '{args.mode}' readings for equipment {args.equipment_id} "
          f"every {args.interval}s. Press Ctrl+C to stop.")

    hours = args.start_hours
    tick = 0
    try:
        while True:
            tick += 1
            reading = next_reading(args.mode, tick, hours)
            status, body = http_post(readings_url, reading, token)

            if status == 401:                       # token expired: log in again and retry once
                token = login(args.base_url, email, password)
                status, body = http_post(readings_url, reading, token)

            if status == 201:
                print(f"#{tick:>3}  temp={reading['temperatureCelsius']:>6} C  "
                      f"vib={reading['vibrationMmPerSec']:>5} mm/s  "
                      f"oil={reading['oilPressureBar']:>4} bar  hours={reading['operatingHours']}  -> 201")
            else:
                print(f"#{tick:>3}  rejected ({status}): {body}")

            hours += args.hours_per_reading
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nSimulator stopped.")


if __name__ == "__main__":
    main()
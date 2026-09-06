import json
import time
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed

API_URL = "https://pray-proxy.reflex-game.workers.dev/"
GATEWAY_SECRET = "prayer-app-secret-key-2026"
INPUT_FILE = "test/prayer_requests_stress_test.json"
OUTPUT_FILE = "test/stress_test_responses.json"
MAX_WORKERS = 3
MAX_RETRIES = 3

def process_item(item):
    item_id = item["id"]
    payload = json.dumps({"user_input": item["input_text"]}).encode("utf-8")
    headers = {
        "Content-Type": "application/json",
        "X-Prayer-Gateway-Secret": GATEWAY_SECRET,
        "User-Agent": "PrayerStressTester/1.0"
    }

    start_time = time.time()
    last_error = None

    for attempt in range(1, MAX_RETRIES + 1):
        try:
            req = urllib.request.Request(API_URL, data=payload, headers=headers)
            with urllib.request.urlopen(req, timeout=30) as resp:
                elapsed_ms = int((time.time() - start_time) * 1000)
                resp_text = resp.read().decode("utf-8")
                try:
                    resp_json = json.loads(resp_text)
                    valid_json = True
                except Exception:
                    resp_json = None
                    valid_json = False

                record = {
                    "id": item_id,
                    "name": item["name"],
                    "category_tag": item.get("category_tag"),
                    "perspective": item.get("perspective"),
                    "theology_focus": item.get("theology_focus"),
                    "wordiness": item.get("wordiness"),
                    "word_count": item.get("word_count"),
                    "expected_root": item.get("expected_root"),
                    "expected_group": item.get("expected_group"),
                    "stress_test_dimension": item.get("stress_test_dimension"),
                    "input_text": item["input_text"],
                    "http_status": resp.status,
                    "latency_ms": elapsed_ms,
                    "valid_json": valid_json,
                    "response": resp_json if valid_json else resp_text,
                }

                # Extract analytical metrics
                if valid_json and isinstance(resp_json, dict):
                    candidates = resp_json.get("candidate_prayer_points") or []
                    cand_metrics = []
                    for c in candidates:
                        t = c.get("title", "")
                        d = c.get("description", "")
                        t_words = len(t.split()) if t else 0
                        d_words = len(d.split()) if d else 0
                        cand_metrics.append({
                            "title": t,
                            "title_words": t_words,
                            "title_valid_len": (2 <= t_words <= 4),
                            "description": d,
                            "description_words": d_words,
                            "description_valid_len": (d_words <= 25),
                            "suggested_root": c.get("suggested_root"),
                            "suggested_group": c.get("suggested_group")
                        })
                    record["metrics"] = {
                        "skip_question": resp_json.get("skip_question"),
                        "clarifying_question": resp_json.get("clarifying_question"),
                        "candidate_count": len(candidates),
                        "candidates": cand_metrics,
                        "root_match": any(c.get("suggested_root") == item.get("expected_root") for c in candidates) if candidates else None
                    }

                return record

        except urllib.error.HTTPError as e:
            last_error = f"HTTP {e.code}: {e.reason}"
            time.sleep(1.5 * attempt)
        except Exception as e:
            last_error = str(e)
            time.sleep(1.5 * attempt)

    elapsed_ms = int((time.time() - start_time) * 1000)
    return {
        "id": item_id,
        "name": item["name"],
        "category_tag": item.get("category_tag"),
        "expected_root": item.get("expected_root"),
        "expected_group": item.get("expected_group"),
        "input_text": item["input_text"],
        "http_status": 0,
        "latency_ms": elapsed_ms,
        "valid_json": False,
        "error": last_error,
        "response": None
    }

def main():
    print(f"Loading {INPUT_FILE}...")
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        items = json.load(f)

    # Check for existing partial progress
    results_map = {}
    try:
        with open(OUTPUT_FILE, "r", encoding="utf-8") as f:
            existing = json.load(f)
            for r in existing:
                if r.get("valid_json"):
                    results_map[r["id"]] = r
        print(f"Found {len(results_map)} existing valid results in {OUTPUT_FILE}.")
    except Exception:
        pass

    to_process = [it for it in items if it["id"] not in results_map]
    print(f"Items to process: {len(to_process)} of {len(items)} total.")

    if to_process:
        completed_count = len(results_map)
        with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
            future_to_id = {executor.submit(process_item, it): it["id"] for it in to_process}
            for future in as_completed(future_to_id):
                res = future.result()
                results_map[res["id"]] = res
                completed_count += 1
                status = "OK" if res.get("valid_json") else f"FAIL ({res.get('error')})"
                cand_count = res.get("metrics", {}).get("candidate_count", 0) if res.get("valid_json") else 0
                print(f"[{completed_count}/{len(items)}] {res['id']}: {status} (cands={cand_count}, {res['latency_ms']}ms)")

                # Periodic save every 5 items
                if completed_count % 5 == 0 or completed_count == len(items):
                    ordered_results = [results_map[it["id"]] for it in items if it["id"] in results_map]
                    with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
                        json.dump(ordered_results, out, indent=2, ensure_ascii=False)

    # Final ordered save
    ordered_results = [results_map[it["id"]] for it in items if it["id"] in results_map]
    with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
        json.dump(ordered_results, out, indent=2, ensure_ascii=False)

    print(f"\nAll {len(ordered_results)} items saved to {OUTPUT_FILE}.")

if __name__ == "__main__":
    main()

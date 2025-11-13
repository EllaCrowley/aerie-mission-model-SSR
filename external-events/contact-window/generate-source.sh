#!/usr/bin/env python3

import json
from datetime import datetime, timedelta
import uuid

def generate_event_key():
    """Generate a unique key for an event"""
    return uuid.uuid4().hex

def generate_events(start_time, end_time, duration_minutes=10, interval_minutes=300):
    """Generate ground contact events with a 10 minute duration spaced at 5-hour intervals"""
    events = []
    current_time = datetime.fromisoformat(start_time)
    end = datetime.fromisoformat(end_time)
    
    while current_time < end:
        event = {
            "attributes": {
                "passage_id": uuid.uuid4().hex,
                "passage_result_id": "200",
                "satellite_id": uuid.uuid4().hex,
                "ground_station_id": uuid.uuid4().hex,
                "max_elevation": 1.2,
                "aos": current_time.strftime("%Y-%m-%dT%H:%M:%S.000Z"),
                "tca": current_time.strftime("%Y-%m-%dT%H:%M:%S.000Z"),
                "los": (current_time + timedelta(minutes=duration_minutes)).strftime("%Y-%m-%dT%H:%M:%S.000Z"),
                "passage_status": "scheduled",
                "source": "scheduler",
                "keyhole_bypass": True
            },
            "duration": f"00:{duration_minutes:02d}:00",
            "event_type_name": "GroundContactWindow",
            "key": generate_event_key(),
            "start_time": current_time.strftime("%Y-%m-%dT%H:%M:%S.000Z")
        }
        events.append(event)
        current_time += timedelta(minutes=interval_minutes)
    
    return events

def main():
    start_time = "2026-08-01T01:00:00"
    end_time = "2026-08-04T00:00:00"

    data = {
        "source": {
            "attributes": {"version": 2, "serviceVersion": 1},
            "derivation_group_name": "GroundContact",
            "period": {
                "start_time": start_time,
                "end_time": end_time
            },
            "key": "GroundContact_Element1_20260801T000000Z",
            "source_type_name": "GroundContact",
            "valid_at": start_time
        },
        "events": generate_events(start_time, end_time)
    }

    with open("GROUND_CONTACT_source.json", "w") as f:
        json.dump(data, f, indent=2)

    print(f"Generated {len(data['events'])} events in GROUND_CONTACT_source.json")

if __name__ == "__main__":
    main()
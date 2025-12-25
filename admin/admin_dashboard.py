# admin_dashboard.py
import os
import json
import io
import zipfile
import datetime
from collections import Counter

import streamlit as st
import pandas as pd
import altair as alt

# -----------------------
# CONFIG - Hard-coded admin
# -----------------------
ADMIN_EMAIL = "admin@mindscape.com"
ADMIN_PASSWORD = "Admin@123"   # change this before deploying

# -----------------------
# FILE PATHS
# -----------------------
BASE_DIR = os.path.dirname(__file__)
DATA_DIR = os.path.join(BASE_DIR, "data")
USERS_FILE = os.path.join(DATA_DIR, "users.json")
POSTS_FILE = os.path.join(DATA_DIR, "posts.json")
ROOMS_FILE = os.path.join(BASE_DIR, "rooms.json")
VITALS_FILE = os.path.join(BASE_DIR, "vitals.json")
SETTINGS_FILE = os.path.join(DATA_DIR, "settings.json")

os.makedirs(DATA_DIR, exist_ok=True)

# ...existing code...

if __name__ == "__main__":
    main()

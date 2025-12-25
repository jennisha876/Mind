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


# -----------------------
# HELPERS: load / save
# -----------------------
def read_json(path, default):
    if os.path.exists(path):
        try:
            with open(path, "r") as f:
                return json.load(f)
        except Exception:
            return default
    return default


def write_json(path, data):
    with open(path, "w") as f:
        json.dump(data, f, indent=2, default=str)


def load_users():
    return read_json(USERS_FILE, [])


def save_users(users):
    write_json(USERS_FILE, users)


def load_posts():
    return read_json(POSTS_FILE, [])


def save_posts(posts):
    write_json(POSTS_FILE, posts)


def load_rooms():
    return read_json(ROOMS_FILE, {"rooms": []}).get("rooms", [])


def load_vitals():
    return read_json(VITALS_FILE, {})


def save_vitals(vitals):
    write_json(VITALS_FILE, vitals)


def load_settings():
    return read_json(
        SETTINGS_FILE,
        {"posting_enabled": True, "maintenance_mode": False}
    )


def save_settings(settings):
    write_json(SETTINGS_FILE, settings)


# -----------------------
# AUTH
# -----------------------
def admin_login():
    st.title("Mindscape — Admin Login")
    email = st.text_input("Email")
    password = st.text_input("Password", type="password")
    if st.button("Log in"):
        if email == ADMIN_EMAIL and password == ADMIN_PASSWORD:
            st.session_state["admin_authenticated"] = True
            st.success("Logged in as admin.")
            st.rerun()
        else:
            st.error("Invalid admin credentials.")


def logout():
    if 'user' in st.session_state:
        del st.session_state['user']
    st.session_state.page = 'Login'
    st.rerun()

    st.title('Mindscape Admin Dashboard')
    # Dynamic greeting
    now = datetime.datetime.now()
    hour = now.hour
    if hour < 12:
        greeting = 'Good morning'
    elif hour < 17:
        greeting = 'Good afternoon'
    elif hour < 21:
        greeting = 'Good evening'
    else:
        greeting = 'Good night'
    name = st.session_state.user.get('name', st.session_state.user['email'])
    st.write(f'{greeting}, {name}!')


# -----------------------
# OVERVIEW PAGE
# -----------------------
def overview_page():
    st.title("Overview")
    users = load_users()
    posts = load_posts()
    rooms = load_rooms()
    settings = load_settings()

    total_users = len(users)
    total_posts = len(posts)
    total_rooms = len(rooms)
    posting_enabled = settings.get("posting_enabled", True)
    maintenance_mode = settings.get("maintenance_mode", False)

    col1, col2, col3, col4 = st.columns(4)
    col1.metric("Total users", total_users)
    col2.metric("Total posts", total_posts)
    col3.metric("Rooms", total_rooms)
    col4.metric("Posting enabled", "Yes" if posting_enabled else "No")

    st.write("Maintenance mode:", "ON" if maintenance_mode else "OFF")

    # Small chart: posts per day (last 30 days)
    if posts:
        df_posts = pd.DataFrame(posts)
        if "timestamp" in df_posts.columns:
            df_posts["timestamp"] = pd.to_datetime(df_posts["timestamp"])
            df_posts["date"] = df_posts["timestamp"].dt.date
            last_30 = df_posts[
                df_posts["timestamp"] >= (
                    pd.Timestamp.now(tz="UTC") - pd.Timedelta(days=30)
                )
            ]
            posts_by_day = (
                last_30.groupby("date")
                .size()
                .reset_index(name="count")
            )
            chart = (
                alt.Chart(posts_by_day)
                .mark_bar()
                .encode(x="date:T", y="count:Q")
                .properties(title="Posts in last 30 days")
            )
            st.altair_chart(chart, use_container_width=True)
        else:
            st.info("No timestamps in posts to build charts.")
    else:
        st.info("No posts yet — charts will appear once there is activity.")


# -----------------------
# USERS PAGE
# -----------------------
def users_page():
    st.title("Users Management")
    posts = load_posts()
    vitals = load_vitals()

    st.write(f"Total users: {len(users)}")
    if not users:
        st.info("No users found.")
        return

    # Table of users
    df = pd.DataFrame(users)
    if df.empty:
        st.write("No users to display.")
    else:
        display_cols = [c for c in df.columns if c != "password"]
        st.dataframe(df[display_cols].astype(str))

    st.subheader("User actions")
    email_to_manage = st.selectbox(
        "Select a user (by email)",
        [u["email"] for u in users]
    )
    selected_user = next(
        (u for u in users if u["email"] == email_to_manage), None
    )

    if selected_user:
        st.write("Selected user:", email_to_manage)
        col1, col2, col3 = st.columns(3)
        with col1:
            if st.button("Delete user account"):
                # remove user
                users = [u for u in users if u["email"] != email_to_manage]
                save_users(users)
                # remove user's posts
                posts = [
                    p for p in posts
                    if p.get("author") != email_to_manage
                ]
                save_posts(posts)
                # remove user's vitals (if stored keyed by id or email)
                # attempt both styles: if vitals keyed by user id/email
                vitals_changed = False
                if email_to_manage in vitals:
                    vitals.pop(email_to_manage, None)
                    vitals_changed = True
                else:
                    # if vitals keyed by id and user has id field
                    uid = selected_user.get("id")
                    if uid and uid in vitals:
                        vitals.pop(uid, None)
                        vitals_changed = True
                if vitals_changed:
                    save_vitals(vitals)
                st.success(
                    f"Deleted user {email_to_manage} and their posts/vitals "
                    f"(if present)."
                )
                st.rerun()
        with col2:
            if st.button("Delete user's posts"):
                before = len(posts)
                posts = [
                    p for p in posts
                    if p.get("author") != email_to_manage
                ]
                save_posts(posts)
                st.success(
                    f"Removed {before - len(posts)} posts by "
                    f"{email_to_manage}."
                )
                st.rerun()
        with col3:
            if st.button("Generate & download report (CSV)"):
                # Build a small CSV report: user's posts + vitals
                report_items = []
                for p in posts:
                    if p.get("author") == email_to_manage:
                        report_items.append({
                            "type": "post",
                            "timestamp": p.get("timestamp"),
                            "content": p.get("content")
                        })
                # vitals entries: try keyed by email or user id
                user_vitals = []
                if email_to_manage in vitals:
                    user_vitals = vitals[email_to_manage].get("vitals", [])
                else:
                    uid = selected_user.get("id")
                    if uid and uid in vitals:
                        user_vitals = vitals[uid].get("vitals", [])
                for v in user_vitals:
                    report_items.append({
                        "type": "vital",
                        "timestamp": v.get("timestamp"),
                        "heartRate": v.get("heartRate")
                    })
                if not report_items:
                    st.info(
                        "No posts or vitals for this user to include in the "
                        "report."
                    )
                else:
                    df_report = (
                        pd.DataFrame(report_items).sort_values("timestamp")
                    )
                    csv_bytes = df_report.to_csv(index=False).encode("utf-8")
                    st.download_button(
                        label="Download report (CSV)",
                        data=csv_bytes,
                        file_name=(
                            (
                                f"{email_to_manage}_report_"
                                f"{datetime.datetime.now().date()}.csv"
                            )
                        ),
                        mime="text/csv"
                    )

    st.markdown("---")
    st.subheader("All posts (admin can delete individually)")
    if posts:
        for i, p in enumerate(posts):
            cols = st.columns([8, 1])
            with cols[0]:
                author = p.get("author", "unknown")
                content = p.get("content", "")
                timestamp = p.get("timestamp", "")
                st.markdown(f"**{author}**: {content}  \n*{timestamp}*")
            with cols[1]:
                if st.button("Delete", key=f"delpost_{i}"):
                    posts.pop(i)
                    save_posts(posts)
                    st.success("Post deleted.")
                    st.rerun()
    else:
        st.write("No posts to display.")


# -----------------------
# ANALYTICS PAGE
# -----------------------
def analytics_page():
    st.title("Analytics")
    users = load_users()
    posts = load_posts()
    vitals = load_vitals()

    # POSTS PER DAY
    st.subheader("Posts per day (last 60 days)")
    if posts:
        dfp = pd.DataFrame(posts)
        if "timestamp" in dfp.columns:
            dfp["timestamp"] = pd.to_datetime(dfp["timestamp"])
            dfp["date"] = dfp["timestamp"].dt.date
            window = dfp[
                dfp["timestamp"] >= (
                    pd.Timestamp.now(tz="UTC") - pd.Timedelta(days=60)
                )
            ]
            daily = window.groupby("date").size().reset_index(name="count")
            chart = (
                alt.Chart(daily)
                .mark_line(point=True)
                .encode(x="date:T", y="count:Q")
                .properties(width=700, height=300)
            )
            st.altair_chart(chart, use_container_width=True)
        else:
            st.info(
                "Posts don't include timestamps — "
                "add timestamps to enable charts."
            )
    else:
        st.info("No posts yet.")

    # DAILY ACTIVE BASED ON POSTS & VITALS
    st.subheader("Daily Active Users (approx.)")
    dau_counter = Counter()
    if posts:
        dfp = pd.DataFrame(posts)
        if "timestamp" in dfp.columns:
            dfp["timestamp"] = pd.to_datetime(dfp["timestamp"])
            dfp["date"] = dfp["timestamp"].dt.date
            for _, row in dfp.iterrows():
                dau_counter[row["date"]] += 1
    # vitals activity
    if isinstance(vitals, dict):
        for key, val in vitals.items():
            for v in val.get("vitals", []):
                try:
                    d = pd.to_datetime(v.get("timestamp")).date()
                    dau_counter[d] += 1
                except Exception:
                    pass
    if dau_counter:
        dau_df = pd.DataFrame(
            sorted(dau_counter.items()), columns=["date", "activity_count"]
        )
        chart = (
            alt.Chart(dau_df)
            .mark_bar()
            .encode(x="date:T", y="activity_count:Q")
            .properties(height=300)
        )
        st.altair_chart(chart, use_container_width=True)
    else:
        st.info("No activity recorded yet for DAU chart.")

    # HEART RATE SUMMARY
    st.subheader("Heart Rate Summary (sampled across users)")
    hr_values = []
    if isinstance(vitals, dict):
        for key, val in vitals.items():
            for v in val.get("vitals", []):
                hr = v.get("heartRate")
                try:
                    hr_values.append(float(hr))
                except Exception:
                    pass
    if hr_values:
        s = pd.Series(hr_values)
        st.metric("Avg heart rate", f"{s.mean():.1f}")
        st.metric("Min HR", int(s.min()))
        st.metric("Max HR", int(s.max()))

        hr_df = pd.DataFrame({"heartRate": hr_values})
        chart = (
            alt.Chart(hr_df)
            .mark_boxplot(size=50)
            .encode(y="heartRate:Q")
            .properties(height=160)
        )
        st.altair_chart(chart, use_container_width=True)
    else:
        st.info("No vitals/heart rate data yet.")


# -----------------------
# SETTINGS PAGE
# -----------------------
def settings_page():
    st.title("Settings")
    settings = load_settings()

    st.subheader("Feature toggles")
    posting_enabled = st.checkbox(
        "Enable user posting",
        value=settings.get("posting_enabled", True)
    )
    maintenance_mode = st.checkbox(
        "Maintenance mode (lock app)",
        value=settings.get("maintenance_mode", False)
    )

    if st.button("Save settings"):
        settings["posting_enabled"] = posting_enabled
        settings["maintenance_mode"] = maintenance_mode
        save_settings(settings)
        st.success("Settings saved.")

    st.markdown("---")
    st.subheader("Export / Backup")

    if st.button("Download all data (zip)"):
        # prepare zip of JSON files in memory
        mem_zip = io.BytesIO()
        with zipfile.ZipFile(
            mem_zip, mode="w", compression=zipfile.ZIP_DEFLATED
        ) as zf:
            for p in [
                USERS_FILE,
                POSTS_FILE,
                ROOMS_FILE,
                VITALS_FILE,
                SETTINGS_FILE
            ]:
                if os.path.exists(p):
                    arcname = os.path.basename(p)
                    zf.write(p, arcname=arcname)
        mem_zip.seek(0)
        st.download_button(
            "Download data ZIP",
            data=mem_zip,
            file_name=f"mindscape_data_{datetime.datetime.now().date()}.zip"
        )

    st.markdown("---")
    st.subheader("Danger zone")
    if st.button("Delete ALL posts"):
        save_posts([])
        st.success("All posts removed.")
    if st.button("Delete ALL users (except admin)"):
        filtered_users = [
            u for u in load_users()
            if u.get("email") == ADMIN_EMAIL or u.get("role") == "admin"
        ]
        save_users(filtered_users)
        st.success("All non-admin users removed.")


# -----------------------
# MAIN
# -----------------------
def main():
    st.set_page_config(page_title="Mindscape Admin", layout="wide")
    if "admin_authenticated" not in st.session_state:
        st.session_state["admin_authenticated"] = False

    if not st.session_state["admin_authenticated"]:
        admin_login()
        return

    # Top greeting and user info
    now = datetime.datetime.now()
    hour = now.hour
    if hour < 12:
        greeting = 'Good morning'
    elif hour < 17:
        greeting = 'Good afternoon'
    elif hour < 21:
        greeting = 'Good evening'
    else:
        greeting = 'Good night'
    st.write(f'{greeting}, admin!')

    # Sidebar nav
    st.sidebar.title("Admin Dashboard")
    st.sidebar.markdown("Logged in as **admin**")
    page = st.sidebar.radio(
        "Navigation",
        ["Overview", "Users", "Analytics", "Settings"]
    )

    # Render selected page
    if page == "Overview":
        users_page()
        overview_page()
    elif page == "Users":
        users_page()
    elif page == "Analytics":
        analytics_page()
    elif page == "Settings":
        settings_page()

    # Logout button at the end
    if st.sidebar.button("Logout"):
        st.session_state["admin_authenticated"] = False
        st.success("Logged out.")
        st.rerun()


if __name__ == "__main__":
    main()

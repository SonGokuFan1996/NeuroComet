#!/usr/bin/env python3
"""
Script to replace German strings at the end of localized strings.xml files
with English fallback strings.
"""

import os
import re

# The German block to find and replace
GERMAN_BLOCK = '''    <string name="feedback_votes">%d votes</string>
    <string name="comments_empty">Noch keine Kommentare</string>
    <string name="comments_load_more">Mehr laden</string>
    <string name="comments_replies">%d Antworten</string>
    <string name="comments_reply">Antworten</string>
    <string name="community_members">%d Mitglieder</string>
    <string name="community_posts">%d Beiträge</string>
    <string name="create_post_add_link">Link hinzufügen</string>
    <string name="create_post_add_poll">Umfrage hinzufügen</string>
    <string name="create_post_discard">Verwerfen</string>
    <string name="create_post_discard_message">Dein Beitrag geht verloren.</string>
    <string name="create_post_discard_title">Beitrag verwerfen?</string>
    <string name="explore_bipolar">Bipolar</string>
    <string name="explore_communities">Gemeinschaften</string>
    <string name="explore_depression">Depression</string>
    <string name="explore_dyslexia">Legasthenie</string>
    <string name="explore_lgbtq">LGBTQ+</string>
    <string name="explore_nonbinary">Nicht-binär</string>
    <string name="explore_ocd">Zwangsstörung</string>
    <string name="explore_people">Menschen</string>
    <string name="explore_ptsd">PTBS</string>
    <string name="explore_topics">Themen</string>
    <string name="explore_trans">Transgender</string>
    <string name="feed_empty_subtitle">Folge Menschen und Gemeinschaften</string>
    <string name="feed_empty_title">Noch leer</string>
    <string name="feed_error">Ladefehler</string>
    <string name="feed_loading">Wird geladen...</string>
    <string name="feed_retry">Erneut versuchen</string>
    <string name="feedback_category">Kategorie</string>
    <string name="feedback_description">Beschreibung</string>
    <string name="feedback_description_placeholder">Beschreibe im Detail...</string>
    <string name="feedback_severity">Schweregrad</string>
    <string name="feedback_thanks">Danke für dein Feedback!</string>
    <string name="neuro_category_dyslexia">Legasthenie</string>
    <string name="notification_commented">hat deinen Beitrag kommentiert</string>
    <string name="notification_followed">folgt dir jetzt</string>
    <string name="notification_liked_post">hat deinen Beitrag geliked</string>
    <string name="notification_mentioned">hat dich erwähnt</string>
    <string name="notification_replied">hat auf deinen Kommentar geantwortet</string>
    <string name="notifications_mark_read">Als gelesen markieren</string>
    <string name="notifications_settings">Benachrichtigungseinstellungen</string>
    <string name="post_comment">Kommentieren</string>
    <string name="post_copy_link">Link kopieren</string>
    <string name="post_edit">Bearbeiten</string>
    <string name="post_like">Gefällt mir</string>
    <string name="post_unlike">Gefällt mir nicht mehr</string>
    <string name="settings_about">Über die App</string>
    <string name="settings_contact">Kontaktiere uns</string>
    <string name="settings_help">Hilfe &amp; Support</string>
    <string name="settings_rate_app">App bewerten</string>
    <string name="settings_share_app">App teilen</string>
    <string name="settings_terms_of_service">Nutzungsbedingungen</string>
    <string name="time_months_ago">vor %d Monat(en)</string>
    <string name="time_weeks_ago">vor %d Woche(n)</string>
    <string name="time_years_ago">vor %d Jahr(en)</string>'''

# English replacement (fallback)
ENGLISH_BLOCK = '''    <string name="feedback_votes">%d votes</string>
    <string name="comments_empty">No comments yet</string>
    <string name="comments_load_more">Load more</string>
    <string name="comments_replies">%d replies</string>
    <string name="comments_reply">Reply</string>
    <string name="community_members">%d members</string>
    <string name="community_posts">%d posts</string>
    <string name="create_post_add_link">Add link</string>
    <string name="create_post_add_poll">Add poll</string>
    <string name="create_post_discard">Discard</string>
    <string name="create_post_discard_message">Your post will be lost.</string>
    <string name="create_post_discard_title">Discard post?</string>
    <string name="explore_bipolar">Bipolar</string>
    <string name="explore_communities">Communities</string>
    <string name="explore_depression">Depression</string>
    <string name="explore_dyslexia">Dyslexia</string>
    <string name="explore_lgbtq">LGBTQ+</string>
    <string name="explore_nonbinary">Non-binary</string>
    <string name="explore_ocd">OCD</string>
    <string name="explore_people">People</string>
    <string name="explore_ptsd">PTSD</string>
    <string name="explore_topics">Topics</string>
    <string name="explore_trans">Transgender</string>
    <string name="feed_empty_subtitle">Follow people and communities</string>
    <string name="feed_empty_title">Nothing here yet</string>
    <string name="feed_error">Error loading</string>
    <string name="feed_loading">Loading…</string>
    <string name="feed_retry">Try again</string>
    <string name="feedback_category">Category</string>
    <string name="feedback_description">Description</string>
    <string name="feedback_description_placeholder">Describe in detail…</string>
    <string name="feedback_severity">Severity</string>
    <string name="feedback_thanks">Thank you for your feedback!</string>
    <string name="neuro_category_dyslexia">Dyslexia</string>
    <string name="notification_commented">commented on your post</string>
    <string name="notification_followed">is now following you</string>
    <string name="notification_liked_post">liked your post</string>
    <string name="notification_mentioned">mentioned you</string>
    <string name="notification_replied">replied to your comment</string>
    <string name="notifications_mark_read">Mark as read</string>
    <string name="notifications_settings">Notification settings</string>
    <string name="post_comment">Comment</string>
    <string name="post_copy_link">Copy link</string>
    <string name="post_edit">Edit</string>
    <string name="post_like">Like</string>
    <string name="post_unlike">Unlike</string>
    <string name="settings_about">About the app</string>
    <string name="settings_contact">Contact us</string>
    <string name="settings_help">Help &amp; Support</string>
    <string name="settings_rate_app">Rate app</string>
    <string name="settings_share_app">Share app</string>
    <string name="settings_terms_of_service">Terms of Service</string>
    <string name="time_months_ago">%d months ago</string>
    <string name="time_weeks_ago">%d weeks ago</string>
    <string name="time_years_ago">%d years ago</string>'''

def fix_file(filepath):
    """Fix a single strings.xml file by replacing German strings with English."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        if 'Noch keine Kommentare' in content:
            # Replace the German block with English
            new_content = content.replace(GERMAN_BLOCK, ENGLISH_BLOCK)

            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)

            print(f"Fixed: {filepath}")
            return True
        else:
            print(f"Skipped (no German strings): {filepath}")
            return False
    except Exception as e:
        print(f"Error processing {filepath}: {e}")
        return False

def main():
    base_path = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')

    fixed_count = 0

    # Process all values-* folders
    for folder in os.listdir(base_path):
        if folder.startswith('values-') and folder != 'values-de':  # Skip German
            strings_path = os.path.join(base_path, folder, 'strings.xml')
            if os.path.exists(strings_path):
                if fix_file(strings_path):
                    fixed_count += 1

    print(f"\nTotal files fixed: {fixed_count}")

if __name__ == '__main__':
    main()


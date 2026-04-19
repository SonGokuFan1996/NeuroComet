import json
import re

with open('missing_keys.json', encoding='utf-16') as f:
    keys = json.load(f)

def camel_to_title(name):
    if name == 'postsCount': return '{count} Posts'
    if name == 'entriesCount': return '{count} Entries'
    if name == 'exploringHashtag': return 'Exploring #{hashtag}'
    if name == 'youHaveUnreadMessages': return 'You have {count} unread messages'
    if name == 'minutesAgo': return '{n} minutes ago'
    if name == 'hoursAgo': return '{n} hours ago'
    if name == 'daysAgo': return '{n} days ago'
    if name == 'tapToPlaceStone': return 'Tap to place {stone}'
    if name == 'whatsOnYourMind': return 'What\'s on your mind?'

    name = re.sub('(.)([A-Z][a-z]+)', r'\1 \2', name)
    name = re.sub('([a-z0-9])([A-Z])', r'\1 \2', name)
    words = name.title().split()
    lowers = {'and', 'to', 'your', 'for', 'from', 'the', 'a', 'an', 'in', 'on', 'with', 'of', 'at'}
    res = [words[0]]
    for w in words[1:]:
        res.append(w.lower() if w.lower() in lowers else w)
    return ' '.join(res)

with open('fix_en.txt', 'w', encoding='utf-8') as out:
    for k in keys:
        display = camel_to_title(k).replace("'", "\\'")
        out.write(f"      '{k}': '{display}',\n")

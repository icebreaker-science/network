from langdetect import detect_langs

from data.core_data import CoreDataEntry


def detect_language(entry: CoreDataEntry) -> None:
    try:
        s = entry.abstract or entry.title
        if not s:
            return
        langs = detect_langs(s)
        entry.language_detected_probabilities = [(lang.lang, lang.prob) for lang in langs]
        if len(entry.language_detected_probabilities) > 0:
            entry.language_detected_most_likely = entry.language_detected_probabilities[0][0]
    except:
        print("Unexpected error during language detection...")

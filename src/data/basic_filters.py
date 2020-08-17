from typing import Callable, List, Iterator

from data.data_models import CoreDataEntry


def apply_filters(entries: Iterator[CoreDataEntry], filters: List[Callable[[CoreDataEntry], bool]]) \
        -> Iterator[CoreDataEntry]:
    """
    This function iterates through the entries and only returns those which pass all the filters.
    """
    for entry in entries:
        skip_entry = False
        for f in filters:
            if not f(entry):
                skip_entry = True
                break
        if skip_entry:
            continue
        yield entry


def filter_is_chemistry(entry: CoreDataEntry) -> bool:
    """
    Returns true if the entry seems to be a chemistry publication; otherwise false.
    """
    keywords = ['chemistry', 'chemical', 'chemist']
    s = entry.json_raw_string.lower()
    for keyword in keywords:
        if keyword in s:
            return True
    return False


def filter_has_abstract(entry: CoreDataEntry) -> bool:
    """
    Returns true if the entry contains an abstract; otherwise false
    """
    return entry.abstract is not None and len(entry.abstract.strip()) > 0

"""Classes and functions to work with the CORE dataset

This is especially designed to work with full text dataset from 2018-03-01.

See: https://core.ac.uk/documentation/dataset/
"""
import json
import lzma
import os
import sys
from dataclasses import dataclass
from typing import List, Any, Iterator, Tuple
from tqdm import tqdm


@dataclass
class CoreDataEntry:
    """
    An object of this class represents a line in the dataset.
    """

    # --- The original json string ---
    json_raw_string: str

    # --- The given data ---
    # Attention: The type annotations is not entirely correct. For example, journals are (at least not always) provided
    #   as a list of strings even though the documentation states it: https://core.ac.uk/documentation/dataset/
    doi: str
    core_id: str
    oai: str
    identifiers: List[str]
    title: str
    authors: List[str]
    enrichments: Any
    contributors: List[str]
    date_published: str
    abstract: str
    download_url: str
    full_text_identifier: str
    pdfHashValue: str
    publisher: str
    raw_record_xml: str
    journals: List[str]
    language: str
    relations: List[Any]
    year: int
    topics: List[str]
    subjects: List[str]
    full_text: str

    # --- The following information are computed by us. ---
    id = None
    language_detected_most_likely: str = None
    # The most probable languages with their probabilities
    language_detected_probabilities: List[Tuple[str, float]] = None


def to_json(entry: CoreDataEntry) -> str:
    obj = {
        'doi': entry.doi,
        'coreId': entry.core_id,
        'oai': entry.oai,
        'identifiers': entry.identifiers,
        'title': entry.title,
        'authors': entry.authors,
        'enrichments': entry.enrichments,
        'contributors': entry.contributors,
        'datePublished': entry.date_published,
        'abstract': entry.abstract,
        'downloadUrl': entry.download_url,
        'fullTextIdentifier': entry.full_text_identifier,
        'pdfHashValue': entry.pdfHashValue,
        'publisher': entry.publisher,
        'rawRecordXml': entry.raw_record_xml,
        'journals': entry.journals,
        'language': entry.language,
        'relations': entry.relations,
        'year': entry.year,
        'topics': entry.topics,
        'subjects': entry.subjects,
        'fullText': entry.full_text,

        'id': entry.id,
        'languageDetectedMostLikely': entry.language_detected_most_likely,
        'languageDetectedProbabilities': entry.language_detected_probabilities
    }
    return json.dumps(obj)


def from_json(json_str: str) -> CoreDataEntry:
    obj = json.loads(json_str)
    obj['json_raw_string'] = json_str

    # Convert camel case to underscores
    obj['core_id'] = obj.pop('coreId')
    obj['date_published'] = obj.pop('datePublished')
    obj['download_url'] = obj.pop('downloadUrl')
    obj['full_text_identifier'] = obj.pop('fullTextIdentifier')
    obj['raw_record_xml'] = obj.pop('rawRecordXml')
    obj['full_text'] = obj.pop('fullText')

    obj['language_detected_most_likely'] = obj.pop('languageDetectedMostLikely', None)
    obj['language_detected_probabilities'] = obj.pop('languageDetectedProbabilities', None)

    return CoreDataEntry(**obj)


def read_all(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to the directory containing the unpacked .tar.gz, i.e., to a directory with .xz files
    :return:
    """

    files = [os.path.join(path, f) for f in os.listdir(path)
             if os.path.isfile(os.path.join(path, f)) and f.endswith('.xz')]
    for f in tqdm(files, desc='Processed files', total=len(files)):
        try:
            for entry in read_from_xz(f):
                yield entry
        except:
            print("Unexpected error processing the file", f)
            print(sys.exc_info()[0])


def read_from_xz(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to a single .xz file
    :return:
    """
    with lzma.open(path, mode='rt') as f:
        for line in f:
            yield from_json(line)

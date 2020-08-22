"""Classes and functions to work with the CORE dataset

This is especially designed to work with full text dataset from 2018-03-01.

See: https://core.ac.uk/documentation/dataset/
"""
import json
import lzma
import os
import sys
import psycopg2
from dataclasses import dataclass
from typing import List, Any, Iterator, Tuple, Callable
from tqdm import tqdm


@dataclass
class BasicDataEntry:
    """
    This class contains those information from the original dataset that is interesting for us as well as some
    extracted/computed data. It does not contain the full text
    """

    # --- Our ID ---
    icebreaker_id: int

    # --- Copied from the original dataset ---
    doi: str
    core_id: str
    title: str
    abstract: str
    has_full_text: bool
    year: int
    topics: List[str]
    subjects: List[str]

    # --- Computed values ---
    language_detected_most_likely: str = None
    # The most probable languages with their probabilities
    language_detected_probabilities: List[Tuple[str, float]] = None

    def to_json(self) -> str:
        return json.dumps(self, default=lambda o: o.__dict__)


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
    icebreaker_id: int = None
    language_detected_most_likely: str = None
    # The most probable languages with their probabilities
    language_detected_probabilities: List[Tuple[str, float]] = None

    def to_json(self) -> str:
        obj = {
            'doi': self.doi,
            'coreId': self.core_id,
            'oai': self.oai,
            'identifiers': self.identifiers,
            'title': self.title,
            'authors': self.authors,
            'enrichments': self.enrichments,
            'contributors': self.contributors,
            'datePublished': self.date_published,
            'abstract': self.abstract,
            'downloadUrl': self.download_url,
            'fullTextIdentifier': self.full_text_identifier,
            'pdfHashValue': self.pdfHashValue,
            'publisher': self.publisher,
            'rawRecordXml': self.raw_record_xml,
            'journals': self.journals,
            'language': self.language,
            'relations': self.relations,
            'year': self.year,
            'topics': self.topics,
            'subjects': self.subjects,
            'fullText': self.full_text,

            'icebreaker_id': self.icebreaker_id,
            'languageDetectedMostLikely': self.language_detected_most_likely,
            'languageDetectedProbabilities': self.language_detected_probabilities
        }
        return json.dumps(obj)

    def to_basic_data_entry(self) -> BasicDataEntry:
        return BasicDataEntry(self.icebreaker_id, self.doi, self.core_id, self.title, self.abstract,
                              self.full_text is not None, self.year, self.topics, self.subjects,
                              self.language_detected_most_likely, self.language_detected_probabilities)


def basic_from_json(json_str: str) -> BasicDataEntry:
    obj = json.loads(json_str)
    return BasicDataEntry(**obj)


def core_from_json(json_str: str) -> CoreDataEntry:
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

    # I made the mistake to name the field "id" at the beginning without knowing the id is a keyword in python.
    obj['icebreaker_id'] = obj.pop('id', None)

    return CoreDataEntry(**obj)


def core_read_all(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to the directory containing the unpacked .tar.gz, i.e., to a directory with .xz files
    :return:
    """

    files = [os.path.join(path, f) for f in os.listdir(path)
             if os.path.isfile(os.path.join(path, f)) and f.endswith('.xz')]
    for f in tqdm(files, desc='Processed files', total=len(files)):
        try:
            for entry in core_read_from_xz(f):
                yield entry
        except:
            print("Unexpected error processing the file", f)
            print(sys.exc_info()[0])


def basic_read_from_xz(path: str) -> Iterator[BasicDataEntry]:
    """

    :param path: The path to a single .xz file
    :param parser: A function that parses json
    :return:
    """
    for o in _read_from_xz(path, basic_from_json):
        yield o


def core_read_from_xz(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to a single .xz file
    :param parser: A function that parses json
    :return:
    """
    for o in _read_from_xz(path, core_from_json):
        yield o


def write_basics_to_database(entries: Iterator[BasicDataEntry], dbhost, dbport, dbname, dbuser, dbpassword,
                             table='papers'):
    """
    Writes the basics dataset into a PostgreSQL database.
    """
    conn = psycopg2.connect("dbname='{}' user='{}' host='{}' port='{}' password='{}'"
                            .format(dbname, dbuser, dbhost, dbport, dbpassword))
    cur = conn.cursor()
    for entry in tqdm(entries, total=7836565):
        try:
            cur.execute("""
                insert into {}(icebreaker_id, doi, core_id, title, abstract, has_full_text, year, topics, subjects,
                  language_detected_most_likely, language_detected_probabilities)
                values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """.format(table), (entry.icebreaker_id, entry.doi, entry.core_id, entry.title, entry.abstract,
                                entry.has_full_text, entry.year, json.dumps(entry.topics), json.dumps(entry.subjects),
                                entry.language_detected_most_likely, json.dumps(entry.language_detected_probabilities)))
            conn.commit()
        except:
            print('Unexpected error..')
    cur.close()
    conn.close()


def read_basics_from_database(dbhost, dbport, dbname, dbuser, dbpassword, table='papers') -> Iterator[BasicDataEntry]:
    conn = psycopg2.connect("dbname='{}' user='{}' host='{}' port='{}' password='{}'"
                            .format(dbname, dbuser, dbhost, dbport, dbpassword))
    cur = conn.cursor('my_named_cursor')  # Read https://stackoverflow.com/a/27940340
    cur.execute(
        """select icebreaker_id, doi, core_id, title, abstract, has_full_text, year, topics, subjects,
             language_detected_most_likely, language_detected_probabilities
           from {};""".format(table))
    while True:
        records = cur.fetchmany()
        if not records:
            break
        for r in records:
            entry = BasicDataEntry(r[0], r[1], r[2], r[3], r[4], r[5], r[6],
                                   r[7], r[8], r[9], r[10])
            yield entry
    cur.close()
    conn.close()


def _read_from_xz(path: str, parser: Callable[[str], Any]) -> Iterator[Any]:
    """

    :param path: The path to a single .xz file
    :param parser: A function that parses json
    :return:
    """
    with lzma.open(path, mode='rt') as f:
        for line in f:
            yield parser(line)

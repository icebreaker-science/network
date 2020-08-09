"""Classes and functions to work with the CORE dataset

This is especially designed to work with full text dataset from 2018-03-01.

See: https://core.ac.uk/documentation/dataset/
"""
import json
import lzma
import os
from dataclasses import dataclass
from typing import List, Any, Iterator


@dataclass
class CoreDataEntry:
    """
    An object of this class represents a line in the dataset.
    """

    json_raw_string: str

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


def read_all(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to the directory containing the unpacked .tar.gz, i.e., to a directory with .xz files
    :return:
    """

    files = [os.path.join(path, f) for f in os.listdir(path)
             if os.path.isfile(os.path.join(path, f)) and f.endswith('.xz')]
    for f in files:
        for entry in read_from_xz(f):
            yield entry


def read_from_xz(path: str) -> Iterator[CoreDataEntry]:
    """

    :param path: The path to a single .xz file
    :return:
    """
    with lzma.open(path, mode='rt') as f:
        for line in f:
            obj = json.loads(line)
            obj['json_raw_string'] = line

            # Convert camel case to underscores
            obj['core_id'] = obj.pop('coreId')
            obj['date_published'] = obj.pop('datePublished')
            obj['download_url'] = obj.pop('downloadUrl')
            obj['full_text_identifier'] = obj.pop('fullTextIdentifier')
            obj['raw_record_xml'] = obj.pop('rawRecordXml')
            obj['full_text'] = obj.pop('fullText')

            yield CoreDataEntry(**obj)

import lzma

from data import basic_filters, core_data


def main():
    data_entries = core_data.read_all('../data/00-raw/core_2018-03-01_fulltext/')
    chemistry_entries_with_abstracts = basic_filters.apply_filters(data_entries, [
        basic_filters.filter_is_chemistry,
        basic_filters.filter_has_abstract
    ])

    with lzma.open('../data/10-filtered/core_chemistry_with_abstracts.json.xz', mode='at') as f:
        for entry in chemistry_entries_with_abstracts:
            f.write(entry.json_raw_string)


if __name__ == '__main__':
    main()

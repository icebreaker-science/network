import lzma
from tqdm import tqdm

from data import basic_filters, core_data, basic_preprocessing


def main():
    # ---------------------------
    # Step 1: Apply basic filters

    # data_entries = core_data.read_all('../data/00-raw/core_2018-03-01_fulltext/')
    # chemistry_entries_with_abstracts = basic_filters.apply_filters(data_entries, [
    #     basic_filters.filter_is_chemistry,
    #     basic_filters.filter_has_abstract
    # ])
    #
    # with lzma.open('../data/10-filtered/core_chemistry_abstracts.json.xz', mode='at') as f:
    #     for entry in tqdm(chemistry_entries_with_abstracts, desc='Found entries'):
    #         f.write(entry.json_raw_string)

    # ------------------------
    # Step 2: Detect languages

    data_entries = core_data.read_from_xz('../data/10-filtered/core_chemistry_abstracts.json.xz')
    with lzma.open('../data/10-filtered/core_chemistry_abstracts_langdetected.json.xz', mode='at') as f:
        for entry in tqdm(data_entries, desc='Processed entries'):
            try:
                basic_preprocessing.detect_language(entry)
                json_str = core_data.to_json(entry) + '\n'
                f.write(json_str)
            except:
                print("Unexpected error...")


if __name__ == '__main__':
    main()

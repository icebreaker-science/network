import lzma
from tqdm import tqdm

from data import basic_filters, data_models, basic_preprocessing
from data.data_models import BasicDataEntry


def main():
    # ---------------------------
    # Step 1: Apply basic filters

    # data_entries = data_models.core_read_all('../data/00-raw/core_2018-03-01_fulltext/')
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

    # data_entries = data_models.core_read_from_xz('../data/10-filtered/core_chemistry_abstracts.json.xz')
    # with lzma.open('../data/10-filtered/core_chemistry_abstracts_langdetected.json.xz', mode='at') as f:
    #     for i, entry in tqdm(enumerate(data_entries), desc='Processed entries'):
    #         try:
    #             basic_preprocessing.detect_language(entry)
    #             entry.id = i
    #             json_str = entry.to_json() + '\n'
    #             f.write(json_str)
    #         except:
    #             print("Very unexpected error in the main method...")

    # ---------------------------------
    # Step 3: Only keep selected fields
    core_entries = data_models.core_read_from_xz('../data/10-filtered/core_chemistry_abstracts_langdetected.json.xz')
    with lzma.open('../data/11-basic/basics.json.xz', mode='at') as f:
        for core in tqdm(core_entries):
            basic_entry: BasicDataEntry = core.to_basic_data_entry()
            f.write(basic_entry.to_json() + '\n')


if __name__ == '__main__':
    main()

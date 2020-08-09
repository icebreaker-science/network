import src.data.core_data as core_data


def main():
    data_entries = core_data.read_from_xz('../data/00-raw/core_2018-03-01_fulltext/158.json.xz')
    for e in data_entries:
        pass


if __name__ == "__main__":
    main()

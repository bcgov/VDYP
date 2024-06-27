import os


def collect_source_files():
    folders = ['C:/source/vdyp/VDYP_Master/Source']
    source_files = []
    while len(folders) > 0:
        current_directory = folders.pop()
        entities = os.scandir(current_directory)
        for e in entities:
            if e.is_dir():
                folders.append(e)
            elif e.is_file():
                if e.name.endswith('.for'):
                    source_files.append(e)

    print('Saw ' + str(len(source_files)) + ' source files')

    return source_files

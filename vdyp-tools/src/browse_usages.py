import os
import sys
import re
import util

keywords = ['INTEGER', 'REAL', 'CHARACTER', 'LOGICAL', 'DIMENSION']


def browse_usages(block_name, member_name):

    print('searching for usages of block ' + block_name + ' and member ' + member_name)

    source_files = util.collect_source_files()

    common_re = re.compile(r'^\s+COMMON.*/\s*' + block_name + r'\s*/')
    keyword_re = re.compile(r'^\s+([A-Z]+)(\*[0-9]+|\s)')
    assignment_re = re.compile(r'[^A-Z0-9_]' + member_name.upper() + r'([^A-Z0-9_].*=|=)')
    usage_re = re.compile(r'[^A-Z0-9_]' + member_name.upper() + r'[^A-Z0-9_]')

    for s in source_files:
        sf = open(s)
        block_declarations = []
        usages = []
        assignments = []
        for line in sf.readlines():
            line = line.upper()
            if line[0] == 'C' or line[0] == '#':
                continue
            if common_re.search(line):
                block_declarations.append(line)
            else:
                m = re.search(keyword_re, line)
                if m is not None and m.group(1) in keywords:
                    pass
                elif assignment_re.search(line):
                    assignments.append(line)
                elif line.strip().startswith('&'):
                    pass
                elif usage_re.search(line):
                    usages.append(line)

        if len(block_declarations) > 0 and (len(assignments) > 0 or len(usages) > 0):
            print(s.name)
            if len(assignments) > 0:
                for l in assignments:
                    print('   A    ' + l.strip())
            if len(usages) > 0:
                for l in usages:
                    print('   U    ' + l.strip())

        sf.close()


if __name__ == '__main__':

    if len(sys.argv) != 3:
        print('usage: browse_usages <common block name> <member name>')
        sys.exit(0)

    browse_usages(sys.argv[1], sys.argv[2])

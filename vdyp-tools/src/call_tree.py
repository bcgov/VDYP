import os
import sys
import re
import commons
import argparse
import util

subroutine_declaration_re = re.compile(r'^\s+SUBROUTINE\s+([A-Z][A-Z0-9_]*)\s*\(')
end_subroutine_declaration_re = re.compile(r'^\s+END SUBROUTINE')
token_re = re.compile(r'CALL\s+([A-Z][A-Z0-9_]*)')

subroutines = {}


def collect_symbols(source_files):

    for s in source_files:
        commons_details = commons.gather_commons_details(s)
        sf = open(s)
        for line in sf.readlines():
            line = line.upper()
            if line[0] == 'C':
                continue
            m = re.search(subroutine_declaration_re, line)
            if m is not None:
                routine_name = m.group(1)
                subroutines[routine_name] = {
                    "name": routine_name,
                    "file": s,
                    "has_been_traversed": False,
                    "commons_usages": commons.gather_commons_usages(s, commons_details, routine_name),
                    "callees": set()
                }

        sf.close()


def generate_call_tree(routines_to_scan):
    if len(routines_to_scan) == 0:
        return

    routine_name = routines_to_scan.pop()

    if subroutines[routine_name]["has_been_traversed"]:
        # already scanned
        return

    subroutine = subroutines[routine_name]

    subroutine["has_been_traversed"] = True

    file = subroutine["file"]
    sf = open(file)

    in_routine = False
    for line in sf.readlines():
        line = line.upper()
        if len(line.strip()) == 0 or line[0] == 'C' or line[0] == '#':
            continue
        m = re.search(subroutine_declaration_re, line)
        if m is not None and m.group(1) == routine_name:
            in_routine = True
        else:
            m = re.search(end_subroutine_declaration_re, line)
            if m is not None:
                in_routine = False
            elif in_routine:
                # tokenize the line, looking for nested calls
                m = re.search(token_re, line)
                while m is not None:
                    token = m.group(1)
                    if token in subroutines:
                        subroutine["callees"].add(token)
                        if not subroutines[token]["has_been_traversed"]:
                            routines_to_scan.append(token)
                    line = line[m.end():]
                    m = re.search(token_re, line)


ignored_commons = {'UNITS', 'UNITS3', 'UNITS4', 'LIBCOMMON'}


def uses_only_ignored_commons(commons_usages, args):
    for a in commons_usages["block_assignments"]:
        if a not in ignored_commons:
            return False
    if not args.assignments_only:
        for u in commons_usages["block_usages"]:
            if u not in ignored_commons:
                return False

    return True


def has_commons_usages(subroutine, args):
    for callee in subroutine["callees"]:
        if has_commons_usages(subroutines[callee], args):
            return True
    commons_usages = subroutine["commons_usages"]
    if uses_only_ignored_commons(commons_usages, args):
        return False
    return True


def print_call_tree(subroutine, args, indent):
    name = subroutine["name"]
    commons_usages = subroutine["commons_usages"]

    if args.exclude_no_commons_usages and not has_commons_usages(subroutine, args):
        return

    assignments = ""
    assignments_per_block = {}
    for c in commons_usages["block_assignments"]:
        if c not in ignored_commons:
            assignments_per_block[c] = []
    for m in commons_usages["member_assignments"]:
        c = m[0:m.find('.')]
        if c not in ignored_commons:
            assignments_per_block[c].append(m[len(c) + 1:])
    for c in assignments_per_block.keys():
        assignments += c + '('
        if not args.blocks_only:
            for m in assignments_per_block[c]:
                assignments += m + ', '
            assignments = assignments[0:len(assignments) - 2]
        assignments += '), '
    assignments = ' assignments: ' + assignments[0:len(assignments) - 2] if len(assignments) > 0 else ''

    usages = ""
    if not args.assignments_only:
        usages_per_block = {}
        for c in commons_usages["block_usages"]:
            if c not in ignored_commons:
                usages_per_block[c] = []
        for m in commons_usages["member_usages"]:
            c = m[0:m.find('.')]
            if c not in ignored_commons:
                usages_per_block[c].append(m[len(c) + 1:])
        for c in usages_per_block.keys():
            usages += c + '('
            if not args.blocks_only:
                for m in usages_per_block[c]:
                    usages += m + ', '
                usages = usages[0:len(usages) - 2]
            usages += '), '
        usages = ' usages: ' + usages[0:len(usages) - 2] if len(usages) > 0 else ''

    if len(assignments) > 60 or len(usages) > 60 or len(assignments + usages) > 90:
        print((' ' * indent if indent > 0 else '') + name)
        if len(assignments) > 0:
            print((' ' * (indent + 7)) + assignments)
        if len(usages) > 0:
            print((' ' * (indent + 7)) + usages)
    else:
        print((' ' * indent if indent > 0 else '') + name + assignments + usages)

    for callee in subroutine["callees"]:
        print_call_tree(subroutines[callee], args, indent + 4)


def call_tree(routine_name, args):

    routine_name = routine_name.upper()

    source_files = util.collect_source_files()
    collect_symbols(source_files)

    if routine_name not in subroutines:
        print('Subroutine "' + routine_name + '" not found in the source code')
        return

    print('Listing call tree rooted at ' + routine_name)

    routines_to_scan = [routine_name]

    while len(routines_to_scan) > 0:
        generate_call_tree(routines_to_scan)

    print_call_tree(subroutines[routine_name], args, 0)


if __name__ == '__main__':

    parser = argparse.ArgumentParser('Lists the call tree rooted at the given subroutine name'
                                     ' and, for each method, common block usage')
    parser.add_argument('-b', '--blocks-only', action='store_true', help='display block level information only')
    parser.add_argument('-a', '--assignments-only', action='store_true', help='display assignment information only')
    parser.add_argument('-x', '--exclude-no-commons-usages', action='store_true'
                        , help="exclude from call tree subroutines whose call tree doesn't use commons")
    parser.add_argument('subroutine_name', nargs=1)

    params = parser.parse_args(sys.argv[1:])

    call_tree(params.subroutine_name[0], params)

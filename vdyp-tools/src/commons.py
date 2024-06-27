import os
import sys
import re

declaration_keywords = ['INTEGER', 'REAL', 'CHARACTER', 'LOGICAL', 'DIMENSION']
common_re = re.compile(r'^\s*COMMON.*/\s*([A-Z][A-Z0-9_]*)\s*/(.*)')
declaration_re = re.compile(r"^\s*([A-Z]+)(\*[0-9]+|\s)")
member_name_re = re.compile(r'(^\s*[A-Z][A-Z0-9_]*)(\([^)]+\))?\s*(,|$)')
subroutine_declaration_re = re.compile(r'^\s+SUBROUTINE\s+([A-Z][A-Z0-9_]*)\s*\(')
end_subroutine_declaration_re = re.compile(r'^\s+END SUBROUTINE')
token_re = re.compile(r'([A-Z][A-Z0-9_]*)')


def find_file(file_name):

    file_name = file_name.lower()
    if not file_name.endswith('.for'):
        file_name = file_name + '.for'

    folders = ['C:/source/vdyp/VDYP_Master/Source']
    source_file = None
    while source_file is None and len(folders) > 0:
        current_directory = folders.pop()
        entities = os.scandir(current_directory)
        for e in entities:
            if e.is_dir():
                folders.append(e)
            elif e.is_file():
                if e.name == file_name:
                    return e


def gather_commons_details(file):

    commons_details = {
        "block_members": [],
        "member_block": {},
        "common_blocks": {}
    }

    sf = open(file)
    common_block_name = None
    for line in sf.readlines():
        line = line.upper()
        lstripped_line = line.lstrip()
        if len(lstripped_line) == 0 or line[0] == 'C' or line[0] == '#':
            continue
        is_continuation_line = lstripped_line.startswith('&')

        m = common_re.search(line)
        members = None
        if m is not None:
            common_block_name = m.group(1)
            commons_details["common_blocks"][common_block_name] = []
            members = find_members(m.group(2))
        elif is_continuation_line and common_block_name is not None:
            members = find_members(lstripped_line[1:])
        else:
            common_block_name = None

        if members is not None:
            commons_details["common_blocks"][common_block_name].extend(members)
            commons_details["block_members"].extend(members)
            for m in members:
                commons_details["member_block"][m] = common_block_name

    sf.close()

    return commons_details


def gather_commons_usages(file_name, commons_details, routine_name):

    block_usages = set()
    block_assignments = set()
    member_usages = set()
    member_assignments = set()

    commons_usages = {
        "block_usages": block_usages,
        "block_assignments": block_assignments,
        "member_usages": member_usages,
        "member_assignments": member_assignments
    }

    in_routine = routine_name is None
    in_commons_declaration = False

    sf = open(file_name)
    for line in sf.readlines():
        line = line.upper()
        lstripped_line = line.lstrip()
        if len(lstripped_line) == 0 or line[0] == 'C' or line[0] == '#':
            continue

        if not in_routine:
            m = re.search(subroutine_declaration_re, line)
            if m is not None and m.group(1) == routine_name:
                in_routine = True
        else:
            if in_routine and routine_name is not None:
                m = re.search(end_subroutine_declaration_re, line)
                if m is not None:
                    break

            if common_re.search(line) is not None:
                in_commons_declaration = True
            elif in_commons_declaration and lstripped_line.startswith('&'):
                continue
            else:
                m = re.search(declaration_re, line)
                if m is not None and m.group(1) in declaration_keywords:
                    continue

                if line.find('CALL DBG') >= 0:
                    continue

                    # we're in the routine (if one is specified) and this is not a comment line
                # or a commons block declaration. Tokenize the line and look for assignments
                # and usages. Also, ignore usages during calls to DBG subroutines.
                assignment_seen_this_line = False

                m = re.search(token_re, line)
                while m is not None:
                    token = m.group(1)
                    line = line[m.end():]
                    if token in commons_details["member_block"]:
                        if not assignment_seen_this_line and line.find('=') >= 0:
                            block_assignments.add(commons_details["member_block"][token])
                            member_assignments.add(commons_details["member_block"][token] + '.' + token)
                            assignment_seen_this_line = True
                        else:
                            block_usages.add(commons_details["member_block"][token])
                            member_usages.add(commons_details["member_block"][token] + '.' + token)
                    m = re.search(token_re, line)
        sf.close()

    return commons_usages


def print_commons_usages(commons_usages):

    block_assignments = commons_usages["block_assignments"]
    block_usages = commons_usages["block_usages"]
    member_assignments = commons_usages["member_assignments"]
    member_usages = commons_usages["member_usages"]

    if len(commons_usages["member_usages"]) == 0 and len(member_assignments) == 0:
        print('No usages or assignments of common block variables')
    else:
        print('Common blocks assigned: ' + str(sorted(block_assignments)))
        assignment_list = sorted(member_assignments)
        while len(assignment_list) > 0:
            print('Assignments: ' + str(assignment_list[0:8]))
            assignment_list = assignment_list[8:]

        print('Common blocks used: ' + str(sorted(block_usages)))
        usages_list = sorted(member_usages)
        while len(usages_list) > 0:
            print('Usages: ' + str(usages_list[0:8]))
            usages_list = usages_list[8:]


def find_members(s):
    members = []

    s = s.strip()
    m = re.search(member_name_re, s)
    while m is not None:
        members.append(m.group(1))
        s = s[m.end():].strip()
        m = re.search(member_name_re, s)

    return members


def execute(file_name, routine_name):

    print('Listing common usages in ' + file_name)

    file = find_file(file_name)
    commons_details = gather_commons_details(file)
    commons_usages = gather_commons_usages(file, commons_details, routine_name)
    print_commons_usages(commons_usages)


if __name__ == '__main__':

    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print('usage: list_common_usages <file name> [<routine_name>]')
        print('    where file_name is the plain name of the file, with or without ".for"')
        print('      and routine_name (optional) is the name of a subroutine in that file')
        sys.exit(0)

    execute(sys.argv[1], sys.argv[2] if len(sys.argv) == 3 else None)

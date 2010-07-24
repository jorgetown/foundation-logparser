TYPE_AXIS = ["static", "dynamic", "latent", "manifest", "weak", "strong", "nominal", "structural"]

INVALID_PAIRS = [("static", "dynamic"),
                 ("latent", "manifest"),
                 ("weak", "strong"),
                 ("nominal", "structural")]

# keep permutations of size 4
RETAIN_SIZE = 4

# returns a list with 2^len(l) elements (definition of powerset)
def powerset(l):
    r = []
    if l:
        head = l[:1]
        tail = l[1:]
        for item in powerset(tail):
            r.append(item)
            r.append(head + item)
    else:
        r.append([])
    return r

# filter list keeping only n-tuples where n=size
def len_filter(l, size=1):
    return [item for item in l if len(item) == size]

# retain n-tuples that don't contain any of the invalid pairs
def invalidpairs_filter(l):
    return [item for item in l if not is_invalid(INVALID_PAIRS, item)]

# returns True if any of the invalid tuples is contained in the given n-tuple,
# False otherwise
def is_invalid(invalid_pairs, ntuple):
    s = set(ntuple)
    for invalid_pair in invalid_pairs:
        si = set(invalid_pair)
        if si.issubset(s):
            return True
    return False

def show_result(l):
    if l:
        l.sort()
        for item in l:
            item.sort()
            print item


print "TYPES: ", (TYPE_AXIS)
print "\n\r"

print "INVALID PAIRS: "
for invalid in INVALID_PAIRS:
    print invalid
print "\n\r"

print "SIZE OF LIST OF TYPES: %d" % len(TYPE_AXIS)
print "RETAIN SIZE: %d" % (RETAIN_SIZE, )

ps = powerset(TYPE_AXIS)
print "POWERSET (2pow(%d)): %d" % (len(TYPE_AXIS), len(ps), )

f = len_filter(ps, RETAIN_SIZE)
print "COMBINATIONS OF %d (ONLY LISTS OF %d ELEMENTS): %d" % (RETAIN_SIZE, RETAIN_SIZE, len(f), )

nr = invalidpairs_filter(f)
print "FINAL COMBO SIZE: %d" % len(nr)

print "\n\r"

# print to console
show_result(nr)


# arguments must be lists, e.g.: cartesian([1, 2], [3, 4])
def cartesian(list1, list2):
    result = []
    for el in list1:
        for item in list2:
            if isinstance(el, list):
                result.append(el + [item])
            else:
                result.append([el] + [item])
    return result

#@todo: add robustness checks
def types_cartesian(aList):
    head = aList[0]
    tail = aList[1:]
    for item in tail:
        head = cartesian(head, item) #AxBxC = (AxB)xC
    return head

print "A=[1, 2] x B=[3, 4] = "
print cartesian([1, 2], [3, 4])

print "\r\n"

print "A=[1, 2, 3] x B=[4, 5, 6] ="
print cartesian([1, 2, 3], [4, 5, 6])

print "\r\n"

print "S={['static', 'dynamic'],['weak', 'strong'],['latent', 'manifest'],['nominal', 'structural']}"
print "S1 x S2 x ... Sn = \n"
t = types_cartesian([['static', 'dynamic'], ['weak', 'strong'],['latent', 'manifest'],['nominal', 'structural']])

for item in t:
    print item

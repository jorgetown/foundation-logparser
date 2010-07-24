import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RecursivePowerset<T> {

    public Set<Set<T>> get(Set<T> theSet) {
        Set<Set<T>> powerset = new HashSet<Set<T>>(); // powerset is set of all (sub)sets

        // stopping condition for recursion; powerset of the empty set is the set containing the empty set
        if (theSet.isEmpty()) {
            powerset.add(new HashSet<T>(theSet));
            return powerset;
        }

        // let 'element' be any single element of 'theSet'
        T element = theSet.iterator().next();
        // theSet is now subset of set not containing 'element'
        theSet.remove(element);

        for (Set<T> subset : get(theSet)) {
            powerset.add(subset); // all the subsets not containing 'element'
            Set<T> tmpSet = new HashSet<T>();
            tmpSet.add(element); // all the subsets containing 'element'
            tmpSet.addAll(subset);
            powerset.add(tmpSet);
        }
        return powerset;
    }

    public static void main(String[] args) {
        RecursivePowerset<Integer> rp = new RecursivePowerset<Integer>();
        Set<Integer> input = new HashSet<Integer>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(4);
        Set<Set<Integer>> output = rp.get(input);
        Iterator<Set<Integer>> it = output.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
